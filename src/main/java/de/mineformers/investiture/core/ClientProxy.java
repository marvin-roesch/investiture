package de.mineformers.investiture.core;

import de.mineformers.investiture.client.util.Textures;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Handles all Investiture-level operations specific to the dedicated client.
 */
public class ClientProxy implements Proxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Textures.init();
    }
}
