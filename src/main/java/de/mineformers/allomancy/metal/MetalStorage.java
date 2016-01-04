package de.mineformers.allomancy.metal;

import com.google.common.base.Optional;
import de.mineformers.allomancy.Allomancy;
import de.mineformers.allomancy.network.Message;
import de.mineformers.allomancy.network.messages.EntityMetalStorageUpdate;
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
public class MetalStorage {
    public static MetalStorage from(Entity entity) {
        return (MetalStorage) entity.getExtendedProperties(Allomancy.NBT.STORAGE_ID);
    }

    private final TObjectIntMap<AllomanticMetal> _consumedMetals = new TObjectIntHashMap<>();
    private int _impurities;

    public int get(AllomanticMetal metal) {
        if (_consumedMetals.containsKey(metal))
            return _consumedMetals.get(metal);
        return 0;
    }

    public void store(AllomanticMetal metal, int amount) {
        _consumedMetals.adjustOrPutValue(metal, amount, amount);
        markDirty();
    }

    public boolean remove(AllomanticMetal metal, int amount) {
        int storage = get(metal);
        if (storage < amount)
            return false;
        _consumedMetals.adjustValue(metal, -amount);
        markDirty();
        return true;
    }

    public TObjectIntMap<AllomanticMetal> consumedMetals() {
        return TCollections.unmodifiableMap(_consumedMetals);
    }

    public void addImpurity(int amount) {
        _impurities += amount;
        markDirty();
    }

    public void removeImpurity(int amount) {
        int oldImpurities = _impurities;
        _impurities = Math.max(_impurities - amount, 0);
        if (_impurities != oldImpurities)
            markDirty();
    }

    public int impurities() {
        return _impurities;
    }

    public boolean consume(ItemStack stack) {
        for (AllomanticMetal metal : AllomanticMetals.metals()) {
            int value = metal.getValue(stack);
            if (value > 0) {
                if (metal.canBurn(stack))
                    store(metal, value);
                else
                    addImpurity(1);
                return true;
            }
        }
        return false;
    }

    public void copy(MetalStorage from) {
        _consumedMetals.clear();
        _consumedMetals.putAll(from.consumedMetals());
        _impurities = from.impurities();
    }

    protected void setImpurity(int value) {
        _impurities = value;
    }

    protected void markDirty() {
    }

    public static class Translator implements Message.Translator<MetalStorage> {
        @Override
        public void serialiseImpl(MetalStorage value, ByteBuf buffer) {
            buffer.writeInt(value.consumedMetals().size());
            value.consumedMetals().forEachEntry((metal, amount) -> {
                ByteBufUtils.writeUTF8String(buffer, metal.id());
                buffer.writeInt(amount);
                return true;
            });

            buffer.writeInt(value.impurities());
        }

        @Override
        public MetalStorage deserialiseImpl(ByteBuf buffer) {
            MetalStorage storage = new MetalStorage();
            for (int i = 0; i < buffer.readInt(); i++) {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(ByteBufUtils.readUTF8String(buffer));
                if (metal.isPresent())
                    storage.store(metal.get(), buffer.readInt());
            }

            storage.addImpurity(buffer.readInt());
            return storage;
        }
    }

    public static class EntityMetalStorage extends MetalStorage implements IExtendedEntityProperties {
        private Entity entity;

        @Override
        public void init(Entity entity, World world) {
            this.entity = entity;
        }

        @Override
        public void saveNBTData(NBTTagCompound compound) {
            NBTTagCompound root = new NBTTagCompound();
            NBTTagCompound storage = new NBTTagCompound();

            consumedMetals().forEachEntry((metal, value) -> {
                storage.setInteger(metal.id(), value);
                return true;
            });
            root.setTag("metals", storage);

            root.setInteger("impurities", impurities());
            compound.setTag(Allomancy.NBT.STORAGE_ID, root);
        }

        @Override
        public void loadNBTData(NBTTagCompound compound) {
            NBTTagCompound root = compound.getCompoundTag(Allomancy.NBT.STORAGE_ID);

            NBTTagCompound storage = root.getCompoundTag("metals");
            for (String id : storage.getKeySet()) {
                Optional<AllomanticMetal> metal = AllomanticMetals.get(id);
                if (metal.isPresent())
                    store(metal.get(), storage.getInteger(id));
            }

            setImpurity(root.getInteger("impurities"));
        }

        @Override
        protected void markDirty() {
            sync();
        }

        public void sync() {
            if (entity != null && !entity.worldObj.isRemote) {
                Allomancy.net().sendToAll(new EntityMetalStorageUpdate(entity.getEntityId(), this));
            }
        }
    }
}
