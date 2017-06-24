package de.mineformers.investiture.core;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.client.renderer.SelectionRenderer;
import de.mineformers.investiture.client.renderer.tileentity.CrusherRenderer;
import de.mineformers.investiture.client.util.Textures;
import de.mineformers.investiture.tileentity.Crusher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.Stack;

/**
 * Handles all Investiture-level operations specific to the dedicated client.
 */
public class ClientProxy implements ModProxy
{
    private EventHandler eventHandler;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        B3DLoader.INSTANCE.addDomain(Investiture.MOD_ID);
        Textures.init();
        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(new SelectionRenderer());
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
            return Minecraft.getMinecraft().gameSettings.fovSetting;
        }
        return 0;
    }

    @Override
    public NumberFormat getPercentageFormat()
    {
        NumberFormat percentageFormat = NumberFormat.getPercentInstance(MinecraftForgeClient.getLocale());
        percentageFormat.setMaximumFractionDigits(2);
        return percentageFormat;
    }

    @Nullable
    @Override
    public IAnimationStateMachine loadASM(ResourceLocation location, ImmutableMap<String, ITimeValue> parameters)
    {
        return ModelLoaderRegistry.loadASM(location, parameters);
    }

    private static class EventHandler
    {
        Stack<Pair<Float, Integer>> schedule = new Stack<>();
        private float prevTarget = 0;
        private float target = 0;
        private int targetDuration = 0;
        private int fovTimer = 0;
        private boolean animating;

        @SubscribeEvent
        public void onFOV(EntityViewRenderEvent.FOVModifier event)
        {
            if (targetDuration > 0)
            {
                float delta = (prevTarget +
                    (target - prevTarget) * ((float) Math.min(targetDuration, fovTimer + event.getRenderPartialTicks()) / targetDuration));
                event.setFOV(event.getFOV() + delta);
            }
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event)
        {
            if (event.phase != TickEvent.Phase.START)
                return;
            float gameFOV = Minecraft.getMinecraft().gameSettings.fovSetting;
            if (fovTimer >= targetDuration)
            {
                fovTimer = targetDuration;
                animating = false;
                if (target == 0f)
                {
                    fovTimer = 0;
                    targetDuration = 0;
                }
            }
            if (animating)
            {
                fovTimer = Math.max(0, Math.min(fovTimer + 1, targetDuration));
            }
            if (!schedule.isEmpty())
            {
                Pair<Float, Integer> target = schedule.pop();
                this.prevTarget = this.target;
                this.target = target.getLeft() - gameFOV;
                if (animating)
                {
                    targetDuration = Math.max(0, target.getRight() - fovTimer);
                }
                else
                {
                    animating = true;
                    targetDuration = target.getRight();
                    fovTimer = 0;
                }
            }
        }
    }
}
