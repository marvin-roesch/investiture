package de.mineformers.allomancy.core;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Proxy
 *
 * @author PaleoCrafter
 */
public interface Proxy {
    default void preInit(FMLPreInitializationEvent event) {
    }

    default void init(FMLInitializationEvent event) {
    }

    default void postInit(FMLPostInitializationEvent event) {
    }
}
