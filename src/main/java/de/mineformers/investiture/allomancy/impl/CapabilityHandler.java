package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackMappingProvider;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
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
        CapabilityManager.INSTANCE.register(MetalStack.class, new Capability.IStorage<MetalStack>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<MetalStack> capability, MetalStack instance, EnumFacing side)
            {
                if (instance instanceof INBTSerializable)
                {
                    return ((INBTSerializable) instance).serializeNBT();
                }
                return null;
            }

            @Override
            public void readNBT(Capability<MetalStack> capability, MetalStack instance, EnumFacing side, NBTBase nbt)
            {
                if (instance instanceof INBTSerializable)
                {
                    ((INBTSerializable<NBTBase>) instance).deserializeNBT(nbt);
                }
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
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.getEntity())
                                 .ifPresent(a ->
                                            {
                                                if (a instanceof EntityAllomancer)
                                                {
                                                    for (Class<? extends Misting> type : a.powers())
                                                    {
                                                        a.as(type).ifPresent(
                                                            m -> AllomancyAPIImpl.INSTANCE.factories
                                                                .get(type).companion
                                                                .sendToAll(m, ((EntityAllomancer) a).entity));
                                                    }
                                                    a.activePowers()
                                                     .forEach(p -> a.as(p).ifPresent(Misting::startBurning));
                                                    ((EntityAllomancer) a).sync();
                                                }
                                            });
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.getTarget())
                                 .ifPresent(a ->
                                            {
                                                if (a instanceof EntityAllomancer)
                                                {
                                                    for (Class<? extends Misting> type : a.powers())
                                                    {
                                                        a.as(type).ifPresent(
                                                            m -> AllomancyAPIImpl.INSTANCE.factories
                                                                .get(type).companion
                                                                .sendTo(event.getEntityPlayer(), m,
                                                                        ((EntityAllomancer) a).entity));
                                                    }
                                                    ((EntityAllomancer) a).sync(event.getEntityPlayer());
                                                }
                                            });
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event)
    {
        NBTTagCompound data = ((EntityAllomancer) event.getOriginal().getCapability(Capabilities.ALLOMANCER, null)).serializeNBT();
        ((EntityAllomancer) event.getEntity().getCapability(Capabilities.ALLOMANCER, null)).deserializeNBT(data);
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
            event.addCapability(Allomancy.resource("entity_allomancer"), new PlayerCapabilityProvider(event.getObject()));
        }
    }

    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent.Item event)
    {
        AllomancyAPIImpl.INSTANCE.getMapping(event.getItemStack())
                                 .ifPresent(m -> event.addCapability(Allomancy.resource("metal_mapping"),
                                                                     new MetalStackMappingProvider(event.getItemStack(), m)));
    }

    private static class PlayerCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
    {
        private Entity entity;
        private EntityAllomancer allomancer;

        private PlayerCapabilityProvider(Entity entity)
        {
            this.entity = entity;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
        {
            return capability == Capabilities.ALLOMANCER;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
        {
            if (capability == Capabilities.ALLOMANCER)
            {
                if (allomancer == null)
                    allomancer = new EntityAllomancer(entity);
                return Capabilities.ALLOMANCER.cast(allomancer);
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
                allomancer = new EntityAllomancer(entity);
            allomancer.deserializeNBT(nbt);
        }
    }
}
