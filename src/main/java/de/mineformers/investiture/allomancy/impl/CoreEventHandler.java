package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackMappingProvider;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackProvider;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;

/**
 * ${JDOC}
 */
public class CoreEventHandler
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
        CapabilityManager.INSTANCE.register(MetalStackProvider.class, new Capability.IStorage<MetalStackProvider>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<MetalStackProvider> capability, MetalStackProvider instance, EnumFacing side)
            {
                if (instance instanceof INBTSerializable)
                {
                    return ((INBTSerializable) instance).serializeNBT();
                }
                return null;
            }

            @Override
            public void readNBT(Capability<MetalStackProvider> capability, MetalStackProvider instance, EnumFacing side, NBTBase nbt)
            {
                if (instance instanceof INBTSerializable)
                {
                    ((INBTSerializable<NBTBase>) instance).deserializeNBT(nbt);
                }
            }
        }, () -> null);
        MinecraftForge.EVENT_BUS.register(new CoreEventHandler());
    }

    private CoreEventHandler()
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
    public void onItemUse(PlayerInteractEvent.RightClickItem event)
    {
        ItemStack stack = event.getItemStack();
        EntityPlayer player = event.getEntityPlayer();
        if (!event.getWorld().isRemote)
            AllomancyAPIImpl.INSTANCE.toAllomancer(player).ifPresent(a ->
                                                                     {
                                                                         int oldSize = stack.getCount();
                                                                         ItemStack changed = a.storage()
                                                                                              .consume(stack,
                                                                                                       player.isSneaking() ? stack.getMaxStackSize()
                                                                                                                           : 1,
                                                                                                       false);
                                                                         if (changed.getCount() != oldSize)
                                                                         {
                                                                             event.setCanceled(true);
                                                                             event.setCancellationResult(EnumActionResult.SUCCESS);
                                                                         }
                                                                     });
    }

    @SubscribeEvent
    public void onAttachEntity(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPlayer)
        {
            event.addCapability(Allomancy.resource("entity_allomancer"), new PlayerCapabilityProvider(event.getObject()));
        }
    }

    @SubscribeEvent
    public void onAttachItemStack(AttachCapabilitiesEvent<ItemStack> event)
    {
        AllomancyAPIImpl.INSTANCE.getMapping(event.getObject())
                                 .ifPresent(m -> event.addCapability(Allomancy.resource("metal_mapping"),
                                                                     new MetalStackMappingProvider(event.getObject(), m)));
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
