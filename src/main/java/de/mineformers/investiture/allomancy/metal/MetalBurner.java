package de.mineformers.investiture.allomancy.metal;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.network.Message;
import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Set;

/**
 * Provides metal burning capabilities for both pure and impure metals.
 */
public class MetalBurner extends MetalStorage
{
    /**
     * Gets the metal burner associated with a given entity, should it be using the facilities provided by this class.
     *
     * @param entity the entity
     * @return the entity's metal burner if it has one, <code>null</code> otherwise
     */
    public static MetalBurner from(Entity entity)
    {
        return (MetalBurner) entity.getExtendedProperties(Allomancy.NBT.BURNER_ID);
    }

    private final TObjectIntMap<Metal> burningMetals = new TObjectIntHashMap<>();
    public int burnTime = 20;

    /**
     * @param metal the metal to check
     * @return true if the metal is burning, false otherwise
     */
    public boolean isBurning(Metal metal)
    {
        return burningMetals.containsKey(metal) && burningMetals.get(metal) >= 0;
    }

    /**
     * Starts burning a metal.
     *
     * @param metal the metal to burn
     * @return true if the metal burns now or was already burning, false otherwise
     */
    public boolean startBurning(Metal metal)
    {
        if (isBurning(metal)) return true;
        int storedMetal = get(metal);
        int storedImpurity = getImpurity(metal);
        if (storedMetal <= 0 && storedImpurity <= 0) return false;
        burningMetals.put(metal, 0);
        markDirty();
        return true;
    }

    /**
     * Updates the time a metal has burned, decreasing the stored amount of the metal and optionally applying side effects of burning an impure metal.
     *
     * @param entity the entity burning the metal, can be <code>null</code> if no entity is involved
     * @param metal  the burning metal
     * @return true if the metal storage was decreased, false otherwise
     */
    public boolean updateBurnTimer(Entity entity, Metal metal)
    {
        if (!isBurning(metal)) return false;
        burningMetals.increment(metal);
        if (burningMetals.get(metal) == burnTime)
        {
            burningMetals.put(metal, 0);
            if (getImpurity(metal) > 0)
            {
                removeImpurity(metal, 1);
                if (entity != null) metal.applyImpurityEffects(entity);
            }
            else if (!remove(metal, 1) || get(metal) == 0) stopBurning(metal);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Stops burning a metal.
     *
     * @param metal the metal to stop burnign
     */
    public void stopBurning(Metal metal)
    {
        if (isBurning(metal))
        {
            burningMetals.remove(metal);
            markDirty();
        }
    }

    /**
     * @return an unmodifiable view of all burning metals
     */
    public Set<Metal> burningMetals()
    {
        return FluentIterable.from(burningMetals.keySet()).filter(this::isBurning).toSet();
    }

    /**
     * @return an unmodifiable view of all burning metals with their respective timers
     */
    public TObjectIntMap<Metal> burnTimers()
    {
        return TCollections.unmodifiableMap(burningMetals);
    }

    /**
     * Copies the contents of a given storage or burner to this one.
     *
     * @param from the storage or burner to copy the contents from
     */
    @Override
    public void copy(MetalStorage from)
    {
        super.copy(from);
        if (from instanceof MetalBurner)
        {
            MetalBurner burner = (MetalBurner) from;
            burningMetals.clear();
            burningMetals.putAll(burner.burnTimers());
        }
    }

    /**
     * Implementation detail that allows subclasses to set burn timers directly without affecting stored metals.
     * Can be used for persisting the data.
     *
     * @param metal the metal the burn timer is associated with
     * @param value the value of the burn timer
     */
    protected void setBurnTimer(Metal metal, int value)
    {
        burningMetals.put(metal, value);
    }

    /**
     * Translates a metal burner to and from a byte buffer
     */
    public static class Translator implements Message.Translator<MetalBurner>
    {
        @Override
        public void serialiseImpl(MetalBurner value, ByteBuf buffer)
        {
            buffer.writeInt(value.consumedMetals().size());
            value.consumedMetals().forEachEntry((metal, amount) -> {
                ByteBufUtils.writeUTF8String(buffer, metal.id());
                buffer.writeInt(amount);
                return true;
            });

            buffer.writeInt(value.burnTimers().size());
            value.burnTimers().forEachEntry((metal, timer) -> {
                ByteBufUtils.writeUTF8String(buffer, metal.id());
                buffer.writeInt(timer);
                return true;
            });

            buffer.writeInt(value.impurities().size());
            value.impurities().forEachEntry((metal, amount) -> {
                ByteBufUtils.writeUTF8String(buffer, metal.id());
                buffer.writeInt(amount);
                return true;
            });
        }

        @Override
        public MetalBurner deserialiseImpl(ByteBuf buffer)
        {
            MetalBurner burner = new MetalBurner();

            int consumedCount = buffer.readInt();
            for (int i = 0; i < consumedCount; i++)
            {
                Optional<Metal> metal = Metals.get(ByteBufUtils.readUTF8String(buffer));
                burner.store(metal.get(), buffer.readInt());
            }

            int burningCount = buffer.readInt();
            for (int i = 0; i < burningCount; i++)
            {
                Optional<Metal> metal = Metals.get(ByteBufUtils.readUTF8String(buffer));
                burner.setBurnTimer(metal.get(), buffer.readInt());
            }

            int impurityCount = buffer.readInt();
            for (int i = 0; i < impurityCount; i++)
            {
                Optional<Metal> metal = Metals.get(ByteBufUtils.readUTF8String(buffer));
                burner.storeImpurity(metal.get(), buffer.readInt());
            }

            return burner;
        }
    }

    /**
     * Provides a non-invasive way of making an entity a metal burner.
     */
    public static class EntityMetalBurner extends MetalBurner implements IExtendedEntityProperties
    {
        private Entity entity;

        /**
         * @return the entity this burner is associated with
         */
        public Entity entity()
        {
            return this.entity;
        }

        @Override
        public void init(Entity entity, World world)
        {
            this.entity = entity;
        }

        @Override
        public void saveNBTData(NBTTagCompound compound)
        {
            NBTTagCompound root = new NBTTagCompound();

            NBTTagCompound storage = new NBTTagCompound();
            consumedMetals().forEachEntry((metal, value) -> {
                storage.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("Metals", storage);

            NBTTagCompound timers = new NBTTagCompound();
            burnTimers().forEachEntry((metal, value) -> {
                timers.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("Timers", timers);

            NBTTagCompound impurities = new NBTTagCompound();
            impurities().forEachEntry((metal, value) -> {
                impurities.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("Impurities", impurities);

            compound.setTag(Allomancy.NBT.BURNER_ID, root);
        }

        @Override
        public void loadNBTData(NBTTagCompound compound)
        {
            NBTTagCompound root = compound.getCompoundTag(Allomancy.NBT.BURNER_ID);

            NBTTagCompound storage = root.getCompoundTag("Metals");
            for (String id : storage.getKeySet())
            {
                Optional<Metal> metal = Metals.get(id);
                if (metal.isPresent()) store(metal.get(), storage.getInteger(id));
            }

            NBTTagCompound timers = root.getCompoundTag("Timers");
            for (String id : timers.getKeySet())
            {
                Optional<Metal> metal = Metals.get(id);
                if (metal.isPresent()) setBurnTimer(metal.get(), timers.getInteger(id));
            }

            NBTTagCompound impurities = root.getCompoundTag("Impurities");
            for (String id : impurities.getKeySet())
            {
                Optional<Metal> metal = Metals.get(id);
                if (metal.isPresent()) storeImpurity(metal.get(), impurities.getInteger(id));
            }
        }

        @Override
        protected void markDirty()
        {
            sync();
        }

        /**
         * Synchronizes this burner's data with all clients.
         */
        public void sync()
        {
            if (entity != null && !entity.worldObj.isRemote)
            {
                Investiture.net().sendToAll(new EntityMetalBurnerUpdate(entity.getEntityId(), this));
            }
        }
    }
}