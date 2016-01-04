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
 * MetalStorage
 *
 * @author PaleoCrafter
 */
public class MetalStorage
{
    public static MetalStorage from(Entity entity)
    {
        return (MetalStorage) entity.getExtendedProperties(Allomancy.NBT.STORAGE_ID);
    }

    public static final int MAX_STORAGE = 100;
    private final TObjectIntMap<AllomanticMetal> _consumedMetals = new TObjectIntHashMap<>();
    private final TObjectIntMap<AllomanticMetal> _impurities = new TObjectIntHashMap<>();

    public int get(AllomanticMetal metal)
    {
        if (!_consumedMetals.containsKey(metal))
            return 0;
        return _consumedMetals.get(metal);
    }

    private int store(TObjectIntMap<AllomanticMetal> map, AllomanticMetal metal, int amount)
    {
        int storedMetal = get(metal);
        int storedImpurity = getImpurity(metal);
        if (storedMetal >= MAX_STORAGE || storedImpurity >= MAX_STORAGE || amount <= 0)
            return 0;
        int storedAmount = Math.min(amount, MAX_STORAGE - storedMetal - storedImpurity);
        map.adjustOrPutValue(metal, storedAmount, storedAmount);
        markDirty();
        return storedAmount;
    }

    public int store(AllomanticMetal metal, int amount)
    {
        return store(_consumedMetals, metal, amount);
    }

    public boolean remove(AllomanticMetal metal, int amount)
    {
        int storage = get(metal);
        if (storage < amount)
            return false;
        _consumedMetals.adjustValue(metal, -amount);
        markDirty();
        return true;
    }

    public TObjectIntMap<AllomanticMetal> consumedMetals()
    {
        return TCollections.unmodifiableMap(_consumedMetals);
    }

    public int getImpurity(AllomanticMetal metal)
    {
        if (!_impurities.containsKey(metal))
            return 0;
        return _impurities.get(metal);
    }

    public int storeImpurity(AllomanticMetal metal, int amount)
    {
        return store(_impurities, metal, amount);
    }

    public boolean removeImpurity(AllomanticMetal metal, int amount)
    {
        int storage = getImpurity(metal);
        if (storage < amount)
            return false;
        _impurities.adjustValue(metal, -amount);
        markDirty();
        return true;
    }

    public TObjectIntMap<AllomanticMetal> impurities()
    {
        return TCollections.unmodifiableMap(_impurities);
    }

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

    public void copy(MetalStorage from)
    {
        _consumedMetals.clear();
        _consumedMetals.putAll(from.consumedMetals());
        _impurities.clear();
        _impurities.putAll(from.impurities());
    }

    protected void markDirty()
    {
    }

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

    public static class EntityMetalStorage extends MetalStorage implements IExtendedEntityProperties
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

        public void sync()
        {
            if (entity != null && !entity.worldObj.isRemote)
            {
                Investiture.net().sendToAll(new EntityMetalStorageUpdate(entity.getEntityId(), this));
            }
        }
    }
}
