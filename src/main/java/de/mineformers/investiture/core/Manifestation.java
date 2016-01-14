package de.mineformers.investiture.core;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * A "manifestation of Investiture" is a module for this mod.
 * A <code>Manifestation</code> acts as main entry point for a given module.
 */
public interface Manifestation
{
    /**
     * @return the module's identifier
     */
    String id();

    /**
     * Fired during the pre-initialisation phase. Should be used for registering blocks, items etc.
     *
     * @param event the event that triggers this method
     */
    void preInit(FMLPreInitializationEvent event);

    /**
     * Fired during the initialisation phase. Should be used for registering recipes.
     *
     * @param event the event that triggers this method
     */
    void init(FMLInitializationEvent event);

    /**
     * Fired during the post-initialisation phase. Should be used for all kinds of interaction with other mods.
     *
     * @param event the event that triggers this method
     */
    void postInit(FMLPostInitializationEvent event);
}
