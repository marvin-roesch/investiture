package de.mineformers.investiture.client.util;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility for working with Minecraft's {{@link net.minecraft.client.renderer.texture.TextureAtlasSprite TextureAtlasSprite}}.
 * Allows registering new textures with ease.
 */
public class Textures
{
    public enum TextureType
    {
        BLOCK("blocks"),
        ITEM("items"),
        GUI("gui"),
        MISC("misc");

        public final String name;

        TextureType(String name)
        {
            this.name = name;
        }
    }

    private static Stitcher stitcher;
    private static final Set<ResourceLocation> sprites = new HashSet<>();

    public static void init()
    {
        if (stitcher != null)
            return;
        stitcher = new Stitcher();
        MinecraftForge.EVENT_BUS.register(stitcher);
    }

    public static void stitch(String domain, TextureType type, String path)
    {
        stitch(new ResourceLocation(domain, type.name + "/" + path));
    }

    public static void stitch(ResourceLocation texture)
    {
        sprites.add(texture);
    }

    private static class Stitcher
    {
        @SubscribeEvent
        public void onStitch(TextureStitchEvent.Pre event)
        {
            sprites.forEach(event.getMap()::registerSprite);
        }
    }
}
