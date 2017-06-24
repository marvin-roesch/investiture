package de.mineformers.investiture.core;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;

import javax.annotation.Nullable;
import java.text.NumberFormat;

public interface ModProxy extends Proxy
{
    @Nullable
    default EntityPlayer localPlayer()
    {
        return null;
    }

    default void animateFOV(float desiredFOV, int animationDuration)
    {
    }

    default void setFOV(EntityPlayer player, float value)
    {
    }

    default float getFOV(EntityPlayer player)
    {
        return 0;
    }

    default NumberFormat getPercentageFormat()
    {
        return NumberFormat.getNumberInstance();
    }

    @Nullable
    default IAnimationStateMachine loadASM(ResourceLocation location, ImmutableMap<String, ITimeValue> parameters)
    {
        return null;
    }
}
