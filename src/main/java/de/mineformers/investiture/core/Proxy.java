package de.mineformers.investiture.core;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * A proxy acts as handler for side-specific operations. All actions involving classes, fields or methods marked with
 * {@link net.minecraftforge.fml.relauncher.SideOnly SideOnly} should go through a proxy.
 */
public interface Proxy
{
    /**
     * Fired during the pre-initialisation phase. Should be used for registering blocks, items etc.
     *
     * @param event the event that triggers this method
     */
    default void preInit(FMLPreInitializationEvent event) {
    }

    /**
     * Fired during the initialisation phase. Should be used for registering recipes.
     *
     * @param event the event that triggers this method
     */
    default void init(FMLInitializationEvent event) {
    }

    /**
     * Fired during the post-initialisation phase. Should be used for all kinds of interaction with other mods.
     *
     * @param event the event that triggers this method
     */
    default void postInit(FMLPostInitializationEvent event) {
    }
}
