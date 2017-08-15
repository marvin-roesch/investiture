package de.mineformers.investiture.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Container class for all key bindings used in any of the Investiture modules
 */
public class KeyBindings
{
    /**
     * Key binding for the metal selection menu
     */
    public static final KeyBinding SHOW_DIAL = new KeyBinding("key.showDial", KeyConflictContext.IN_GAME, Keyboard.KEY_Z, "key.categories.allomancy");
    /**
     * Key binding for switching to temporal setter mode.
     */
    public static final KeyBinding SET_TEMPORAL = new KeyBinding("key.setTemporal", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_LSHIFT, "key.categories.allomancy");

    public static void init()
    {
        ClientRegistry.registerKeyBinding(SHOW_DIAL);
        ClientRegistry.registerKeyBinding(SET_TEMPORAL);
    }
}
