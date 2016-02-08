package de.mineformers.investiture;

import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.Proxy;
import de.mineformers.investiture.network.FunctionalNetwork;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Main entry point for the Investiture mod
 */
@Mod(modid = Investiture.MOD_ID, name = "Investiture", version = Investiture.MOD_VERSION)
public final class Investiture
{
    public static final String MOD_ID = "investiture";
    public static final String MOD_VERSION = "0.0.1";
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(CreativeTabs.getNextID(), MOD_ID)
    {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem()
        {
            return Allomancy.Items.allomantic_ingot;
        }

        @Override
        public int getIconItemDamage()
        {
            return 8;
        }
    };
    @Mod.Instance(MOD_ID)
    public static Investiture instance;
    @SidedProxy(modId = MOD_ID, clientSide = "de.mineformers.investiture.core.ClientProxy",
        serverSide = "de.mineformers.investiture.core.ServerProxy")
    public static Proxy proxy;

    /**
     * @return the network used by Investiture
     */
    public static FunctionalNetwork net()
    {
        return instance.network;
    }

    /**
     * @return Investiture's logger
     */
    public static Logger log()
    {
        return instance.log;
    }

    private FunctionalNetwork network;
    private Logger log;
    private static final List<Manifestation> modules = ImmutableList.of(new Allomancy());

    /**
     * Fired during the pre-initialisation phase. Should be used for registering blocks, items etc.
     *
     * @param event the event that triggers this method
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        network = FunctionalNetwork.create(MOD_ID);
        log = LogManager.getLogger(MOD_ID);
        // Delegate event to modules
        modules.forEach(m -> {
            log().info("Running pre-initialisation for module '" + m.id() + "'");
            m.preInit(event);
        });
        proxy.preInit(event);
    }

    /**
     * Fired during the initialisation phase. Should be used for registering recipes.
     *
     * @param event the event that triggers this method
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Delegate event to modules
        modules.forEach(m -> {
            log().info("Running initialisation for module '" + m.id() + "'");
            m.init(event);
        });
        proxy.init(event);
    }

    /**
     * Fired during the post-initialisation phase. Should be used for all kinds of interaction with other mods.
     *
     * @param event the event that triggers this method
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Delegate event to modules
        modules.forEach(m -> {
            log().info("Running post-initialisation for module '" + m.id() + "'");
            m.postInit(event);
        });
        proxy.postInit(event);
    }
}
