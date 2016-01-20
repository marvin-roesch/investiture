package de.mineformers.investiture.client.util;

import com.google.common.base.Throwables;
import de.mineformers.investiture.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities revolving around sounds.
 */
public class Sounds
{
    private static final MethodHandle SOUND_MANAGER;
    private static final MethodHandle PLAYING_SOUNDS;

    static
    {
        SOUND_MANAGER = Reflection.getterHandle(SoundHandler.class)
                                  .mcpName("sndManager")
                                  .srgName("field_147694_f")
                                  .build();
        PLAYING_SOUNDS = Reflection.getterHandle(SoundManager.class)
                                   .mcpName("playingSounds")
                                   .srgName("field_148629_h")
                                   .build();
    }

    @Nonnull
    public static SoundManager soundManager()
    {
        try
        {
            return (SoundManager) SOUND_MANAGER.bindTo(Minecraft.getMinecraft().getSoundHandler()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return null;
        }
    }

    @Nonnull
    public static Map<String, ISound> playingSounds()
    {
        try
        {
            return (Map<String, ISound>) PLAYING_SOUNDS.bindTo(soundManager()).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            return new HashMap<>();
        }
    }

    public static boolean isPlaying(ResourceLocation resource) {
        for (ISound sound : playingSounds().values())
        {
            if(sound.getSoundLocation().equals(resource))
            {
                return soundManager().isSoundPlaying(sound);
            }
        }
        return false;
    }

    public static void stop(ResourceLocation resource)
    {
        for (ISound sound : playingSounds().values())
        {
            if(sound.getSoundLocation().equals(resource))
            {
                Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
                return;
            }
        }
    }
}
