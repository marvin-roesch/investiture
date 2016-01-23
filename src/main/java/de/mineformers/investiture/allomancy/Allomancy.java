package de.mineformers.investiture.allomancy;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractorController;
import de.mineformers.investiture.allomancy.block.MetalOre;
import de.mineformers.investiture.allomancy.core.EntityHandler;
import de.mineformers.investiture.allomancy.extractor.ExtractorRecipes;
import de.mineformers.investiture.allomancy.item.MetalItem;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalBurner;
import de.mineformers.investiture.allomancy.api.metal.MetalStorage;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.allomancy.network.ToggleBurningMetal;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorOutput;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorSlave;
import de.mineformers.investiture.allomancy.world.MetalGenerator;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.Proxy;
import de.mineformers.investiture.network.Message;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

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

    /**
     * @param path the path of the resource
     * @return a resource location pointing at the given path in allomancy's resource domain
     */
    public static ResourceLocation resource(String path)
    {
        return new ResourceLocation(DOMAIN, path);
    }

    @Override
    public String id()
    {
        return DOMAIN;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Blocks.register();
        Items.register();
        ExtractorRecipes.register(Metals.IRON);
        ExtractorRecipes.register(Metals.GOLD);
        ExtractorRecipes.register(Metals.COPPER);
        ExtractorRecipes.register(Metals.ZINC, Metals.ZINC, Metals.CADMIUM, 0.5f);
        ExtractorRecipes.register(Metals.TIN);
        ExtractorRecipes.register(Metals.ALUMINIUM);
        ExtractorRecipes.register(Metals.CHROMIUM);
        ExtractorRecipes.register(Metals.SILVER);
        ExtractorRecipes.register(Metals.BISMUTH);
        ExtractorRecipes.register(Metals.LEAD);
        GameRegistry.registerWorldGenerator(new MetalGenerator(), 0);
        Metals.init();

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
        public static MetalOre allomantic_ore;
        public static MetalExtractor metal_extractor;
        public static MetalExtractorController metal_extractor_controller;

        /**
         * Adds all blocks to the game's registry.
         */
        public static void register()
        {
            GameRegistry.registerBlock(allomantic_ore = new MetalOre(), MetalOre.ItemRepresentation.class);
            GameRegistry.registerBlock(metal_extractor = new MetalExtractor(), MetalExtractor.ItemRepresentation.class);
            GameRegistry.registerBlock(metal_extractor_controller = new MetalExtractorController());
            GameRegistry.registerTileEntity(TileMetalExtractorMaster.class, "allomancy:metal_extractor_master");
            GameRegistry.registerTileEntity(TileMetalExtractorSlave.class, "allomancy:metal_extractor_slave");
            GameRegistry.registerTileEntity(TileMetalExtractorOutput.class, "allomancy:metal_extractor_output");

            // Add ores to the ore dictionary
            for (int i = 0; i < MetalOre.NAMES.length; i++)
            {
                OreDictionary.registerOre(String.format("ore%s", StringUtils.capitalize(MetalOre.NAMES[i])), new ItemStack(allomantic_ore, 1, i));
            }
        }
    }

    /**
     * Container class for all items in the Allomancy module.
     */
    public static class Items
    {
        public static MetalItem allomantic_ingot;
        public static MetalItem allomantic_nugget;
        public static MetalItem allomantic_bead;
        public static MetalItem allomantic_chunk;
        public static MetalItem allomantic_dust;

        /**
         * Adds all items to the game's registry.
         */
        public static void register()
        {
            GameRegistry.registerItem(allomantic_ingot = new MetalItem("allomantic_metal_ingot", "ingot", MetalItem.Type.INGOT, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "lead", "nickel", "silver", "bismuth", "duralumin", "nicrosil",
                "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.registerItem(allomantic_nugget = new MetalItem("allomantic_metal_nugget", "nugget", MetalItem.Type.NUGGET, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "duralumin", "nicrosil",
                "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.registerItem(allomantic_bead = new MetalItem("allomantic_metal_bead", "bead", MetalItem.Type.BEAD, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "gold", "duralumin",
                "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.registerItem(allomantic_chunk = new MetalItem("allomantic_metal_chunk", "chunk", MetalItem.Type.CHUNK, new String[]{
                "copper", "tin", "zinc", "iron", "lead", "aluminium", "chromium", "gold", "cadmium", "silver", "bismuth", "nickel"
            }));
            GameRegistry.registerItem(allomantic_dust = new MetalItem("allomantic_metal_dust", "dust", MetalItem.Type.DUST, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "gold", "duralumin",
                "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));

            // Add items to the ore dictionary
            allomantic_bead.registerOreDict();
            allomantic_chunk.registerOreDict();
            allomantic_dust.registerOreDict();
            allomantic_ingot.registerOreDict();
            allomantic_nugget.registerOreDict();
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
         * @see de.mineformers.investiture.allomancy.api.metal.MetalStorage.EntityMetalStorage
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
                    Optional<Metal> optional = Metals.get(msg.metal);

                    // Safety measures, in case the client sends bad data
                    if (optional.isPresent() && burner != null)
                    {
                        Metal metal = optional.get();
                        if (burner.isBurning(metal))
                        {
                            burner.stopBurning(metal);
                        }
                        else
                        {
                            burner.startBurning(metal);
                        }
                    }
                });
                return null;
            });
        }
    }
}
