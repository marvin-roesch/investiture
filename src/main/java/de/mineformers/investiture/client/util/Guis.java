package de.mineformers.investiture.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

/**
 * ${JDOC}
 */
public class Guis
{
    public static int getMouseX(ScaledResolution resolution)
    {
        return Mouse.getX() * resolution.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
    }

    public static int getMouseY(ScaledResolution resolution)
    {
        return resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;
    }
}
