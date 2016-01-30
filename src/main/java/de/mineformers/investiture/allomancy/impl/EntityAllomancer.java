package de.mineformers.investiture.allomancy.impl;

import com.google.common.base.Throwables;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.serialisation.Serialisation;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ${JDOC}
 */
@ParametersAreNonnullByDefault
public class EntityAllomancer implements Allomancer, INBTSerializable<NBTTagCompound>
{
    private final Entity entity;
    private final HashMap<Class<? extends Misting>, Misting> powers = new HashMap<>();

    public EntityAllomancer(Entity entity)
    {
        this.entity = entity;
    }

    @Nonnull
    @Override
    public <T extends Misting> Optional<T> as(Class<T> type)
    {
        return powers().stream().filter(type::isAssignableFrom).map(powers::get).map(type::cast).findFirst();
    }

    @Override
    public boolean takePower(Class<? extends Misting> type)
    {
        if (!powers.containsKey(type))
        {
            return false;
        }
        powers.remove(type);
        return true;
    }

    @Override
    public void increaseStrength()
    {

    }

    @Override
    public void decreaseStrength()
    {

    }

    @Override
    public void increaseStrength(Class<? extends Misting> type)
    {

    }

    @Override
    public void decreaseStrength(Class<? extends Misting> type)
    {

    }

    @Override
    public <T extends Misting> T grantPower(Class<T> type)
    {
        if (is(type))
            return as(type).get();
        else
        {
            T instance = AllomancyAPIImpl.INSTANCE.instantiate(type, this, entity);
            if (instance != null)
                powers.put(type, instance);
            return instance;
        }
    }

    @Nonnull
    @Override
    public Collection<Class<? extends Misting>> powers()
    {
        return powers.keySet();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound result = new NBTTagCompound();
        for (Map.Entry<Class<? extends Misting>, Misting> misting : powers.entrySet())
        {
            NBTTagCompound mistingData = new NBTTagCompound();
            Serialisation.INSTANCE.serialise(misting.getValue(), mistingData);
            mistingData.setString("Allomancy$MistingClass", misting.getKey().getName());
            result.setTag(misting.getValue().getClass().getSimpleName(), mistingData);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        for(String misting : nbt.getKeySet())
        {
            NBTTagCompound mistingData = nbt.getCompoundTag(misting);
            try
            {
                Class<? extends Misting> clazz = (Class<? extends Misting>) Class.forName(mistingData.getString("Allomancy$MistingClass"));
                Misting m = grantPower(clazz);
                Serialisation.INSTANCE.deserialise(mistingData, m);
            }
            catch (Exception e)
            {
                Throwables.propagate(e);
            }
        }
    }
}
