package de.mineformers.investiture.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles all Investiture-level operations specific to the dedicated client.
 */
public class ClientProxy implements Proxy
{
    @Override
    public EntityPlayer localPlayer()
    {
        return Minecraft.getMinecraft().thePlayer;
    }
}
