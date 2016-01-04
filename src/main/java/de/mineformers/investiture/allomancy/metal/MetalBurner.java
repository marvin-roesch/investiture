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
 * MetalStorage
 *
 * @author PaleoCrafter
 */
public class MetalBurner extends MetalStorage
{
    public static MetalBurner from(Entity entity)
    {
        return (MetalBurner) entity.getExtendedProperties(Allomancy.NBT.BURNER_ID);
    }

    private final TObjectIntMap<AllomanticMetal> _burningMetals = new TObjectIntHashMap<>();
    public int burnTime = 20;

    public boolean isBurning(AllomanticMetal metal)
    {
        return _burningMetals.containsKey(metal) && _burningMetals.get(metal) >= 0;
    }

    public boolean startBurning(AllomanticMetal metal)
    {
        int storedMetal = get(metal);
        int storedImpurity = getImpurity(metal);
        if (storedMetal <= 0 && storedImpurity <= 0)
            return false;
        _burningMetals.put(metal, 0);
        markDirty();
        return true;
    }

    public boolean updateBurnTimer(Entity entity, AllomanticMetal metal)
    {
        if (!isBurning(metal))
            return false;
        _burningMetals.increment(metal);
        if (_burningMetals.get(metal) == burnTime)
        {
            _burningMetals.put(metal, 0);
            if (getImpurity(metal) > 0)
            {
                removeImpurity(metal, 1);
                metal.applyImpurityEffects(entity);
            }
            else if (!remove(metal, 1) || get(metal) == 0)
                stopBurning(metal);
            return true;
        }
        else
            return false;
    }

    public void stopBurning(AllomanticMetal metal)
    {
        if (isBurning(metal))
        {
            _burningMetals.remove(metal);
            markDirty();
        }
    }

    public Set<AllomanticMetal> burningMetals()
    {
        return FluentIterable.from(_burningMetals.keySet()).filter(this::isBurning).toSet();
    }

    public TObjectIntMap<AllomanticMetal> burnTimers()
    {
        return TCollections.unmodifiableMap(_burningMetals);
    }

    @Override
    public void copy(MetalStorage from)
    {
        super.copy(from);
        if (from instanceof MetalBurner)
        {
            MetalBurner burner = (MetalBurner) from;
            _burningMetals.clear();
            _burningMetals.putAll(burner.burnTimers());
        }
    }

    protected void setBurnTimer(AllomanticMetal metal, int value)
    {
        _burningMetals.put(metal, value);
    }

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
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                burner.store(metal.get(), buffer.readInt());
            }

            int burningCount = buffer.readInt();
            for (int i = 0; i < burningCount; i++)
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                burner.setBurnTimer(metal.get(), buffer.readInt());
            }

            int impurityCount = buffer.readInt();
            for (int i = 0; i < impurityCount; i++)
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                burner.storeImpurity(metal.get(), buffer.readInt());
            }

            return burner;
        }
    }

    public static class EntityMetalBurner extends MetalBurner implements IExtendedEntityProperties
    {
        private Entity entity;

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
            root.setTag("metals", storage);

            NBTTagCompound timers = new NBTTagCompound();
            burnTimers().forEachEntry((metal, value) -> {
                timers.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("timers", timers);

            NBTTagCompound impurities = new NBTTagCompound();
            impurities().forEachEntry((metal, value) -> {
                impurities.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("impurities", impurities);

            compound.setTag(Allomancy.NBT.BURNER_ID, root);
        }

        @Override
        public void loadNBTData(NBTTagCompound compound)
        {
            NBTTagCompound root = compound.getCompoundTag(Allomancy.NBT.BURNER_ID);

            NBTTagCompound storage = root.getCompoundTag("metals");
            for (String id : storage.getKeySet())
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(id);
                if (metal.isPresent())
                    store(metal.get(), storage.getInteger(id));
            }

            NBTTagCompound timers = root.getCompoundTag("timers");
            for (String id : timers.getKeySet())
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(id);
                if (metal.isPresent())
                    setBurnTimer(metal.get(), timers.getInteger(id));
            }

            NBTTagCompound impurities = root.getCompoundTag("impurities");
            for (String id : impurities.getKeySet())
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(id);
                if (metal.isPresent())
                    storeImpurity(metal.get(), impurities.getInteger(id));
            }
        }

        @Override
        protected void markDirty()
        {
            sync();
        }

        public void sync()
        {
            if (entity != null && !entity.worldObj.isRemote)
            {
                Investiture.net().sendToAll(new EntityMetalBurnerUpdate(entity.getEntityId(), this));
            }
        }
    }
}
