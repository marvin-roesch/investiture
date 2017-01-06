package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;

/**
 * ${JDOC}
 */
public class CapabilityHandler
{
    @CapabilityInject(Allomancer.class)
    public static Capability<Allomancer> ALLOMANCER_CAPABILITY;

    public static void init()
    {
        CapabilityManager.INSTANCE.register(Allomancer.class, new Capability.IStorage<Allomancer>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<Allomancer> capability, Allomancer instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<Allomancer> capability, Allomancer instance, EnumFacing side, NBTBase nbt)
            {
            }
        }, () -> null);
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }

    private CapabilityHandler()
    {
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event)
    {
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.getEntity()).ifPresent(a -> {
            if (a instanceof EntityAllomancer)
            {
                for (Class<? extends Misting> type : a.powers())
                {
                    a.as(type).ifPresent(
                        m -> AllomancyAPIImpl.INSTANCE.factories.get(type).companion.sendToAll(m, ((EntityAllomancer) a).entity));
                }
                a.activePowers().forEach(p -> a.as(p).ifPresent(Misting::startBurning));
                ((EntityAllomancer) a).sync();
            }
        });
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.getTarget()).ifPresent(a -> {
            if (a instanceof EntityAllomancer)
            {
                for (Class<? extends Misting> type : a.powers())
                {
                    a.as(type).ifPresent(
                        m -> AllomancyAPIImpl.INSTANCE.factories.get(type).companion
                            .sendTo(event.getEntityPlayer(), m, ((EntityAllomancer) a).entity));
                }
                ((EntityAllomancer) a).sync(event.getEntityPlayer());
            }
        });
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event)
    {
        NBTTagCompound data = ((EntityAllomancer) event.getOriginal().getCapability(ALLOMANCER_CAPABILITY, null)).serializeNBT();
        ((EntityAllomancer) event.getEntity().getCapability(ALLOMANCER_CAPABILITY, null)).deserializeNBT(data);
    }

    /**
     * Updates every player's burning metals.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
            return;
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.player).ifPresent(a -> AllomancyAPIImpl.INSTANCE.update(a, event.player));
    }

    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPlayer)
        {
            class PlayerCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
            {
                private EntityAllomancer allomancer;

                @Override
                public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
                {
                    return capability == ALLOMANCER_CAPABILITY;
                }

                @SuppressWarnings("unchecked")
                @Override
                public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
                {
                    if (capability == ALLOMANCER_CAPABILITY)
                    {
                        if (allomancer == null)
                            allomancer = new EntityAllomancer(event.getObject());
                        return (T) allomancer;
                    }
                    return null;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    return allomancer != null ? allomancer.serializeNBT() : new NBTTagCompound();
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    if (allomancer == null)
                        allomancer = new EntityAllomancer(event.getObject());
                    allomancer.deserializeNBT(nbt);
                }
            }
            event.addCapability(Allomancy.resource("entity_allomancer"), new PlayerCapabilityProvider());
        }
    }
}
