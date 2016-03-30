package de.mineformers.investiture.core;

import de.mineformers.investiture.client.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Handles all Investiture-level operations specific to the dedicated client.
 */
public class ClientProxy implements Proxy
{
    private static EventHandler eventHandler;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Textures.init();
        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @Override
    public EntityPlayer localPlayer()
    {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void animateFOV(float desiredFOV, int animationDuration)
    {
        eventHandler.animating = true;
        eventHandler.desiredFOV = desiredFOV;
        eventHandler.animationDuration = animationDuration - eventHandler.fovTimer;
        eventHandler.fovTimer = 0;
        eventHandler.initialFOV = eventHandler.prevFOV = eventHandler.fov = Minecraft.getMinecraft().gameSettings.fovSetting;
    }

    @Override
    public void setFOV(EntityPlayer player, float value)
    {
        if (Minecraft.getMinecraft().thePlayer == player)
        {
            Minecraft.getMinecraft().gameSettings.fovSetting = value;
        }
    }

    @Override
    public float getFOV(EntityPlayer player)
    {
        if (Minecraft.getMinecraft().thePlayer == player)
        {
            if (eventHandler.animating)
                return eventHandler.initialFOV;
            else
                return Minecraft.getMinecraft().gameSettings.fovSetting;
        }
        return 0;
    }

    private static class EventHandler
    {
        float desiredFOV = -1;
        int animationDuration = 0;
        private float initialFOV = -1;
        private float prevFOV = -1;
        private float fov = -1;
        private int fovTimer = 0;
        private boolean animating;

        @SubscribeEvent
        public void onRenderLast(RenderWorldLastEvent event)
        {
            if (fov != -1 && prevFOV != -1)
                Minecraft.getMinecraft().gameSettings.fovSetting = prevFOV + (fov - prevFOV) * event.getPartialTicks();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event)
        {
            if (event.phase != TickEvent.Phase.START || !animating)
                return;
            fovTimer = Math.max(0, Math.min(fovTimer + 1, animationDuration));
            prevFOV = fov;
            fov = initialFOV + (desiredFOV - initialFOV) * ((float) fovTimer / animationDuration);
            if (fovTimer >= animationDuration)
            {
                fovTimer = 0;
                prevFOV = fov = -1;
                animating = false;
                animationDuration = 0;
                Minecraft.getMinecraft().gameSettings.fovSetting = desiredFOV;
            }
        }
    }
}
