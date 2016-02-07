package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.entity).ifPresent(a -> {
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
        AllomancyAPIImpl.INSTANCE.toAllomancer(event.target).ifPresent(a -> {
            if (a instanceof EntityAllomancer)
            {
                for (Class<? extends Misting> type : a.powers())
                {
                    a.as(type).ifPresent(
                        m -> AllomancyAPIImpl.INSTANCE.factories.get(type).companion.sendTo(event.entityPlayer, m, ((EntityAllomancer) a).entity));
                }
                ((EntityAllomancer) a).sync(event.entityPlayer);
            }
        });
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event)
    {
        if(!(event.entity instanceof EntityIronGolem))
            return;
        if(!event.entity.worldObj.isRemote)
        {
            if(event.source instanceof EntityDamageSource)
            {
                Entity killer = event.source.getEntity();
                if(killer instanceof EntityPlayer)
                {
                    AllomancyAPIImpl.INSTANCE.toAllomancer(killer).ifPresent(a -> {
                        Metals.BASE_METALS.forEach(m -> a.grantPower(m.mistingType()));
                    });
                    ((EntityPlayer) killer).addChatComponentMessage(new ChatComponentText("You have become a Mistborn!"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event)
    {
        NBTTagCompound data = ((EntityAllomancer) event.original.getCapability(ALLOMANCER_CAPABILITY, null)).serializeNBT();
        ((EntityAllomancer) event.entityPlayer.getCapability(ALLOMANCER_CAPABILITY, null)).deserializeNBT(data);
    }

    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            class PlayerCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
            {
                private EntityAllomancer allomancer;

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return capability == ALLOMANCER_CAPABILITY;
                }

                @SuppressWarnings("unchecked")
                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (capability == ALLOMANCER_CAPABILITY)
                    {
                        if (allomancer == null)
                            allomancer = new EntityAllomancer(event.getEntity());
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
                        allomancer = new EntityAllomancer(event.getEntity());
                    allomancer.deserializeNBT(nbt);
                }
            }
            event.addCapability(Allomancy.resource("entity_allomancer"), new PlayerCapabilityProvider());
        }
    }
}
