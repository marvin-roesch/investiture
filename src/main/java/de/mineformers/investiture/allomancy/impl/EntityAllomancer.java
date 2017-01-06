package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.network.AllomancerUpdate;
import de.mineformers.investiture.serialisation.Serialisation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * ${JDOC}
 */
@ParametersAreNonnullByDefault
public class EntityAllomancer implements Allomancer, INBTSerializable<NBTTagCompound>
{
    public final Entity entity;
    private final HashMap<Class<? extends Misting>, Misting> powers = new HashMap<>();
    private final Set<Class<? extends Misting>> activePowers = new HashSet<>();

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

    @Nullable
    @Override
    public <T extends Misting> T grantPower(Class<T> type)
    {
        if (is(type))
            return as(type).get();
        else
        {
            T instance = AllomancyAPIImpl.INSTANCE.instantiate(type, this, entity);
            if (instance != null)
            {
                powers.put(type, instance);
                sync();
                AllomancyAPIImpl.INSTANCE.factories.get(type).companion.sendToAll(instance, entity);
            }
            return instance;
        }
    }

    @Override
    public boolean takePower(Class<? extends Misting> type)
    {
        if (!powers.containsKey(type))
        {
            return false;
        }
        powers.remove(type);
        activePowers.remove(type);
        sync();
        return true;
    }

    @Override
    public void activate(Class<? extends Misting> type)
    {
        if (activePowers.contains(type))
            return;
        as(type).ifPresent(m -> {
            activePowers.add(type);
            m.startBurning();
            sync();
        });
    }

    @Override
    public void deactivate(Class<? extends Misting> type)
    {
        if (!activePowers.contains(type))
            return;
        as(type).ifPresent(m -> {
            activePowers.remove(type);
            m.stopBurning();
            sync();
        });
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

    @Nonnull
    @Override
    public Collection<Class<? extends Misting>> powers()
    {
        return powers.keySet();
    }

    @Nonnull
    @Override
    public Collection<Class<? extends Misting>> activePowers()
    {
        return Collections.unmodifiableSet(activePowers);
    }

    public void setActivePowers(Set<Class<? extends Misting>> activePowers)
    {
        Set<Class<? extends Misting>> old = new HashSet<>();
        old.addAll(this.activePowers);
        this.activePowers.clear();
        this.activePowers.addAll(activePowers);
        activePowers.stream().filter(c -> !old.contains(c)).forEach(p -> as(p).ifPresent(Misting::startBurning));
        old.stream().filter(c -> !activePowers.contains(c)).forEach(p -> as(p).ifPresent(Misting::stopBurning));
    }

    public void sync()
    {
        Investiture.net().sendToTracking(entity, new AllomancerUpdate(entity.getEntityId(), activePowers));
    }

    public void sync(EntityPlayer player)
    {
        if (!player.world.isRemote)
            Investiture.net().sendTo((EntityPlayerMP) player, new AllomancerUpdate(entity.getEntityId(), activePowers));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound result = new NBTTagCompound();
        NBTTagCompound mistings = new NBTTagCompound();
        for (Map.Entry<Class<? extends Misting>, Misting> misting : powers.entrySet())
        {
            NBTTagCompound mistingData = new NBTTagCompound();
            Serialisation.INSTANCE.serialise(misting.getValue(), mistingData);
            mistingData.setString("Allomancy$MistingClass", misting.getKey().getName());
            mistings.setTag(misting.getValue().getClass().getSimpleName(), mistingData);
        }
        result.setTag("Mistings", mistings);
        NBTTagList activePowers = new NBTTagList();
        for (Class<? extends Misting> power : this.activePowers)
        {
            activePowers.appendTag(new NBTTagString(power.getName()));
        }
        result.setTag("ActivePowers", activePowers);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagCompound mistings = nbt.getCompoundTag("Mistings");
        powers.clear();
        for (String misting : mistings.getKeySet())
        {
            NBTTagCompound mistingData = mistings.getCompoundTag(misting);
            try
            {
                Class<? extends Misting> type = (Class<? extends Misting>) Class.forName(mistingData.getString("Allomancy$MistingClass"));
                Misting m = AllomancyAPIImpl.INSTANCE.instantiate(type, this, entity);
                if (m != null)
                {
                    powers.put(type, m);
                    Serialisation.INSTANCE.deserialise(mistingData, m);
                }
            }
            catch (Exception e)
            {
                Investiture.log().error("Could not load misting with class '" + mistingData.getString("Allomancy$MistingClass") + "', skipping.", e);
            }
        }
        NBTTagList activePowers = nbt.getTagList("ActivePowers", Constants.NBT.TAG_STRING);
        this.activePowers.clear();
        for (int i = 0; i < activePowers.tagCount(); i++)
        {
            try
            {
                Class<? extends Misting> type = (Class<? extends Misting>) Class.forName(activePowers.getStringTagAt(i));
                if (is(type))
                    this.activePowers.add(type);
            }
            catch (Exception e)
            {
                Investiture.log().error("Could not load misting with class '" + activePowers.getStringTagAt(i) + "', skipping.", e);
            }
        }
    }
}
