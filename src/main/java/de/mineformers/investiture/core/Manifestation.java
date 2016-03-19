package de.mineformers.investiture.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.mineformers.investiture.Investiture;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
     * Fired during the server start phase. Should be used for registering commands.
     *
     * @param event the event that triggers this method
     */
    void serverStart(FMLServerStartingEvent event);

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

    default Config loadConfig(FMLPreInitializationEvent event)
    {
        File file = event.getModConfigurationDirectory().toPath().resolve(id() + ".conf").toFile();
        Config cfg = ConfigFactory.parseFile(file).resolve()
                                  .withFallback(ConfigFactory.load(id()));
        if (!file.exists())
            try
            {
                Files.copy(getClass().getResourceAsStream("/" + id() + ".conf"), file.toPath());
            }
            catch (IOException e)
            {
                Investiture.log().error("Could not write config for module '" + id() + "' to '" + file.getAbsolutePath() + "'!", e);
            }
        return cfg;
    }
}
