package de.mineformers.investiture.allomancy;

import com.google.common.base.Optional;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.block.AllomanticMetalOre;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.core.EntityHandler;
import de.mineformers.investiture.allomancy.item.AllomanticMetalIngot;
import de.mineformers.investiture.allomancy.metal.AllomanticMetal;
import de.mineformers.investiture.allomancy.metal.AllomanticMetals;
import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.allomancy.network.ToggleBurningMetal;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorSlave;
import de.mineformers.investiture.allomancy.world.MetalGenerator;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.Proxy;
import de.mineformers.investiture.network.Message;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The "Allomancy" module is based on the "Mistborn" series of books by Brandon Sanderson.
 * <p>
 * The focus of this module are so called allomancers who can burn metals to gain special powers.
 */
public final class Allomancy implements Manifestation
{
    public static final String DOMAIN = "allomancy";
    @SidedProxy(modId = Investiture.MOD_ID,
        clientSide = "de.mineformers.investiture.allomancy.core.ClientProxy",
        serverSide = "de.mineformers.investiture.allomancy.core.ServerSide")
    public static Proxy proxy;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Blocks.register();
        Items.register();
        GameRegistry.registerWorldGenerator(new MetalGenerator(), 0);
        AllomanticMetals.init();

        MinecraftForge.EVENT_BUS.register(new EntityHandler());
        CommonNetworking.init();

        proxy.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {

        proxy.postInit(event);
    }

    /**
     * Container class for all blocks in the Allomancy module.
     */
    public static class Blocks
    {
        public static AllomanticMetalOre allomantic_ore;
        public static MetalExtractor metal_extractor;

        /**
         * Adds all blocks to the game's registry.
         */
        public static void register()
        {
            GameRegistry.registerBlock(allomantic_ore = new AllomanticMetalOre(), AllomanticMetalOre.ItemRepresentation.class);
            GameRegistry.registerBlock(metal_extractor = new MetalExtractor(), MetalExtractor.ItemRepresentation.class);
            GameRegistry.registerTileEntity(TileMetalExtractorMaster.class, "allomancy:metal_extractor_master");
            GameRegistry.registerTileEntity(TileMetalExtractorSlave.class, "allomancy:metal_extractor_slave");
        }
    }

    /**
     * Container class for all items in the Allomancy module.
     */
    public static class Items
    {
        public static AllomanticMetalIngot allomantic_ingot;

        /**
         * Adds all items to the game's registry.
         */
        public static void register()
        {
            GameRegistry.registerItem(allomantic_ingot = new AllomanticMetalIngot());
        }
    }

    /**
     * Container class for all NBT related constants in the Allomancy module.
     */
    public static class NBT
    {
        /**
         * The ID of both the {@link net.minecraftforge.common.IExtendedEntityProperties IEEP} used for storage of a {@link MetalStorage} in an
         * entity as well as the corresponding NBT compound tag.
         *
         * @see de.mineformers.investiture.allomancy.metal.MetalStorage.EntityMetalStorage
         */
        public static final String STORAGE_ID = "allomancy_metal_storage";

        /**
         * The ID of both the {@link net.minecraftforge.common.IExtendedEntityProperties IEEP} used for storage of a {@link MetalBurner} in an
         * entity as well as the corresponding NBT compound tag.
         */
        public static final String BURNER_ID = "allomancy_metal_burner";
    }

    /**
     * Container class for all networking related objects in the Allomancy module.
     */
    public static class CommonNetworking
    {
        /**
         * Initialise the Allomancy network sub-module and register
         * {@link de.mineformers.investiture.network.Message.Translator Translators}, {@link Message Messages}
         * or {@link de.mineformers.investiture.network.Message.Handler handlers}.
         */
        public static void init()
        {
            Message.registerTranslator(MetalStorage.class, new MetalStorage.Translator());
            Message.registerTranslator(MetalBurner.class, new MetalBurner.Translator());

            Investiture.net().registerMessage(EntityMetalStorageUpdate.class);
            Investiture.net().registerMessage(EntityMetalBurnerUpdate.class);
            Investiture.net().registerMessage(ToggleBurningMetal.class);
            Investiture.net().registerMessage(MetalExtractorUpdate.class);

            // Add handler for toggling the burning of a metal
            Investiture.net().addHandler(ToggleBurningMetal.class, Side.SERVER, (msg, ctx) -> {
                ctx.schedule(() -> {
                    MetalBurner burner = MetalBurner.from(ctx.player());
                    Optional<AllomanticMetal> optional = AllomanticMetals.get(msg.metal);

                    // Safety measures, in case the client sends bad data
                    if (optional.isPresent() && burner != null)
                    {
                        AllomanticMetal metal = optional.get();
                        if (burner.isBurning(metal))
                            burner.stopBurning(metal);
                        else
                            burner.startBurning(metal);
                    }
                });
                return null;
            });
        }
    }
}
