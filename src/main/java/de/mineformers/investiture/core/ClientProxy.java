package de.mineformers.investiture.core;

import de.mineformers.investiture.client.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Handles all Investiture-level operations specific to the dedicated client.
 */
public class ClientProxy implements Proxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Textures.init();
    }

    @Override
    public EntityPlayer localPlayer()
    {
        return Minecraft.getMinecraft().thePlayer;
    }
}
