package de.mineformers.investiture.allomancy.metal;

import com.google.common.base.Optional;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.network.Message;
import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Provides storage capabilities for both pure and impure metals.
 */
public class MetalStorage
{
    /**
     * Gets the metal storage associated with a given entity, should it be using the facilities provided by this class.
     *
     * @param entity the entity
     * @return the entity's metal storage if it has one, <code>null</code> otherwise
     */
    public static MetalStorage from(Entity entity)
    {
        return (MetalStorage) entity.getExtendedProperties(Allomancy.NBT.STORAGE_ID);
    }

    /**
     * Sensible default for metal storage
     */
    public static final int MAX_STORAGE = 100;
    private final TObjectIntMap<AllomanticMetal> consumedMetals = new TObjectIntHashMap<>();
    private final TObjectIntMap<AllomanticMetal> impurities = new TObjectIntHashMap<>();

    /**
     * @param metal the metal to check
     * @return the amount stored of a given metal in its pure form
     */
    public int get(AllomanticMetal metal)
    {
        if (!consumedMetals.containsKey(metal))
            return 0;
        return consumedMetals.get(metal);
    }

    /**
     * Internal method to store metals.
     *
     * @param map    the type of the metal, either pure or impure
     * @param metal  the metal
     * @param amount the amount to store
     * @return the amount of the metal that was actually stored
     */
    private int store(TObjectIntMap<AllomanticMetal> map, AllomanticMetal metal, int amount)
    {
        int storedMetal = get(metal);
        int storedImpurity = getImpurity(metal);
        // We're already full, can't store any more metal
        if (storedMetal >= MAX_STORAGE || storedImpurity >= MAX_STORAGE || amount <= 0)
            return 0;
        int storedAmount = Math.min(amount, MAX_STORAGE - storedMetal - storedImpurity);
        map.adjustOrPutValue(metal, storedAmount, storedAmount);
        markDirty();
        return storedAmount;
    }

    /**
     * Stores a specified amount of a metal in its pure form.
     *
     * @param metal  the metal
     * @param amount the amount to store
     * @return the amount of the metal that was actually stored
     */
    public int store(AllomanticMetal metal, int amount)
    {
        return store(consumedMetals, metal, amount);
    }

    /**
     * Removes a specified amount of metal in its pure form from the storage.
     *
     * @param metal  the metal
     * @param amount the amount to remove
     * @return true if the energy was successfully removed, false if there wasn't enough energy left
     */
    public boolean remove(AllomanticMetal metal, int amount)
    {
        int storage = get(metal);
        if (storage < amount)
            return false;
        consumedMetals.adjustValue(metal, -amount);
        markDirty();
        return true;
    }

    /**
     * @return an unmodifiable view of all pure consumed metals
     */
    public TObjectIntMap<AllomanticMetal> consumedMetals()
    {
        return TCollections.unmodifiableMap(consumedMetals);
    }

    /**
     * @param metal the metal to check
     * @return the amount of impure metal stored
     */
    public int getImpurity(AllomanticMetal metal)
    {
        if (!impurities.containsKey(metal))
            return 0;
        return impurities.get(metal);
    }

    /**
     * Stores a specified amount of a metal in its impure form.
     *
     * @param metal  the metal
     * @param amount the amount to store
     * @return the amount of the metal that was actually stored
     */
    public int storeImpurity(AllomanticMetal metal, int amount)
    {
        return store(impurities, metal, amount);
    }

    /**
     * Removes a specified amount of metal in its impure form from the storage.
     *
     * @param metal  the metal
     * @param amount the amount to remove
     * @return true if the energy was successfully removed, false if there wasn't enough energy left
     */
    public boolean removeImpurity(AllomanticMetal metal, int amount)
    {
        int storage = getImpurity(metal);
        if (storage < amount)
            return false;
        impurities.adjustValue(metal, -amount);
        markDirty();
        return true;
    }

    /**
     * @return an unmodifiable view of all stored impurie metals
     */
    public TObjectIntMap<AllomanticMetal> impurities()
    {
        return TCollections.unmodifiableMap(impurities);
    }

    /**
     * Try to consume a given stack of metal in some form.
     *
     * @param stack the stack to consume
     * @return the amount of metal consumed or -1 if the stack did not contain any allomantic metal
     */
    public int consume(ItemStack stack)
    {
        for (AllomanticMetal metal : AllomanticMetals.metals())
        {
            int value = metal.getValue(stack);
            if (value > 0)
                return metal.canBurn(stack) ? store(metal, value) : storeImpurity(metal, value);
        }
        return -1;
    }

    /**
     * Copies the contents of a given storage to this one.
     *
     * @param from the storage to copy the contents from
     */
    public void copy(MetalStorage from)
    {
        consumedMetals.clear();
        consumedMetals.putAll(from.consumedMetals());
        impurities.clear();
        impurities.putAll(from.impurities());
    }

    /**
     * Allows subclasses to do something when the content of this storage changes, e.g. send the data to clients.
     */
    protected void markDirty()
    {
    }

    /**
     * Translates a metal storage to and from a byte buffer
     */
    public static class Translator implements Message.Translator<MetalStorage>
    {
        @Override
        public void serialiseImpl(MetalStorage value, ByteBuf buffer)
        {
            buffer.writeInt(value.consumedMetals().size());
            value.consumedMetals().forEachEntry((metal, amount) -> {
                ByteBufUtils.writeUTF8String(buffer, metal.id());
                buffer.writeInt(amount);
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
        public MetalStorage deserialiseImpl(ByteBuf buffer)
        {
            MetalStorage storage = new MetalStorage();

            int consumedCount = buffer.readInt();
            for (int i = 0; i < consumedCount; i++)
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                storage.store(metal.get(), buffer.readInt());
            }

            int impurityCount = buffer.readInt();
            for (int i = 0; i < impurityCount; i++)
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                storage.storeImpurity(metal.get(), buffer.readInt());
            }

            return storage;
        }
    }

    /**
     * Provides a non-invasive way of attaching a metal storage to an entity.
     */
    public static class EntityMetalStorage extends MetalStorage implements IExtendedEntityProperties
    {
        private Entity entity;

        /**
         * @return the entity this storage is associated with
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
            root.setTag("metals", storage);

            NBTTagCompound impurities = new NBTTagCompound();
            impurities().forEachEntry((metal, value) -> {
                impurities.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("impurities", impurities);

            compound.setTag(Allomancy.NBT.STORAGE_ID, root);
        }

        @Override
        public void loadNBTData(NBTTagCompound compound)
        {
            NBTTagCompound root = compound.getCompoundTag(Allomancy.NBT.STORAGE_ID);

            NBTTagCompound storage = root.getCompoundTag("metals");
            for (String id : storage.getKeySet())
            {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(id);
                if (metal.isPresent())
                    store(metal.get(), storage.getInteger(id));
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

        /**
         * Synchronizes this storage's data with all clients.
         */
        public void sync()
        {
            if (entity != null && !entity.worldObj.isRemote)
            {
                Investiture.net().sendToAll(new EntityMetalStorageUpdate(entity.getEntityId(), this));
            }
        }
    }
}
