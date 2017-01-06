package de.mineformers.investiture.core;

import de.mineformers.investiture.client.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Stack;

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
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void animateFOV(float desiredFOV, int animationDuration)
    {
        eventHandler.schedule.push(Pair.of(desiredFOV, animationDuration));
    }

    @Override
    public void setFOV(EntityPlayer player, float value)
    {
        if (Minecraft.getMinecraft().player == player)
        {
            Minecraft.getMinecraft().gameSettings.fovSetting = value;
        }
    }

    @Override
    public float getFOV(EntityPlayer player)
    {
        if (Minecraft.getMinecraft().player == player)
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
        Stack<Pair<Float, Integer>> schedule = new Stack<>();
        private float targetFOV = -1;
        private int targetDuration = 0;
        private float initialFOV = -1;
        private float prevFOV = -1;
        private float fov = -1;
        private int fovTimer = 0;
        private boolean animating;

        @SubscribeEvent
        public void onRenderLast(RenderWorldLastEvent event)
        {
            if (animating && fov != -1 && prevFOV != -1)
                Minecraft.getMinecraft().gameSettings.fovSetting = prevFOV + (fov - prevFOV) * event.getPartialTicks();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event)
        {
            if (event.phase != TickEvent.Phase.START)
                return;
            if (animating && fovTimer >= targetDuration)
            {
                fovTimer = 0;
                prevFOV = fov = -1;
                animating = false;
                targetDuration = 0;
                Minecraft.getMinecraft().gameSettings.fovSetting = targetFOV;
            }
            if (!schedule.isEmpty())
            {
                Pair<Float, Integer> target = schedule.pop();
                if (!animating)
                {
                    System.out.println("DONE: " + initialFOV);
                    initialFOV = Minecraft.getMinecraft().gameSettings.fovSetting;
                    animating = true;
                    targetDuration = target.getRight();
                }
                else
                {
                    System.out.println(initialFOV);
                    targetDuration = target.getRight() - fovTimer;
                }
                targetFOV = target.getLeft();
                schedule.clear();
            }
            if (animating)
            {
                fovTimer = Math.max(0, Math.min(fovTimer + 1, targetDuration));
                prevFOV = fov;
                fov = initialFOV + (targetFOV - initialFOV) * ((float) fovTimer / targetDuration);
            }
        }
    }
}
