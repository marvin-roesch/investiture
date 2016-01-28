package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
