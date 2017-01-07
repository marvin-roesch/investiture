package de.mineformers.investiture.allomancy;

import com.google.common.base.Throwables;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.misting.Targeting;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractorController;
import de.mineformers.investiture.allomancy.block.MetalOre;
import de.mineformers.investiture.allomancy.core.AllomancyCommand;
import de.mineformers.investiture.allomancy.extractor.ExtractorRecipes;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.CapabilityHandler;
import de.mineformers.investiture.allomancy.impl.misting.temporal.AugurImpl;
import de.mineformers.investiture.allomancy.item.MetalItem;
import de.mineformers.investiture.allomancy.network.*;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorOutput;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorSlave;
import de.mineformers.investiture.allomancy.world.MetalGenerator;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.Proxy;
import de.mineformers.investiture.network.Message;
import de.mineformers.investiture.serialisation.Translator;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

import static de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl.getAllomancer;

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
        serverSide = "de.mineformers.investiture.allomancy.core.ServerProxy")
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
    public void serverStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new AllomancyCommand());
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        CapabilityHandler.init();
        Metals.init();
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
        AllomancyAPIImpl.INSTANCE.init();

        MinecraftForge.EVENT_BUS.register(new AugurImpl.EventHandler());
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
        public static MetalOre ORE;
        public static MetalExtractor METAL_EXTRACTOR;
        public static MetalExtractorController METAL_EXTRACTOR_CONTROLLER;

        /**
         * Adds all blocks to the game's registry.
         */
        public static void register()
        {
            register(ORE = new MetalOre(), MetalOre.ItemRepresentation::new);
            register(METAL_EXTRACTOR = new MetalExtractor(), MetalExtractor.ItemRepresentation::new);
            register(METAL_EXTRACTOR_CONTROLLER = new MetalExtractorController());
            GameRegistry.registerTileEntity(TileMetalExtractorMaster.class, "allomancy:metal_extractor_master");
            GameRegistry.registerTileEntity(TileMetalExtractorSlave.class, "allomancy:metal_extractor_slave");
            GameRegistry.registerTileEntity(TileMetalExtractorOutput.class, "allomancy:metal_extractor_output");

            // Add ores to the ore dictionary
            for (int i = 0; i < MetalOre.NAMES.length; i++)
            {
                OreDictionary.registerOre(String.format("ore%s", StringUtils.capitalize(MetalOre.NAMES[i])), new ItemStack(ORE, 1, i));
            }
        }

        private static void register(Block block)
        {
            register(block, ItemBlock::new);
        }

        private static void register(Block block, @Nullable Function<Block, Item> itemFactory)
        {
            GameRegistry.register(block);
            if (itemFactory == null)
                return;
            Item item = itemFactory.apply(block);
            if (item != null)
            {
                item.setRegistryName(block.getRegistryName());
                GameRegistry.register(item);
            }
        }
    }

    /**
     * Container class for all items in the Allomancy module.
     */
    public static class Items
    {
        public static MetalItem INGOT;
        public static MetalItem NUGGET;
        public static MetalItem BEAD;
        public static MetalItem CHUNK;
        public static MetalItem DUST;

        /**
         * Adds all items to the game's registry.
         */
        public static void register()
        {
            GameRegistry.register(CHUNK = new MetalItem("chunk", "chunk", MetalItem.Type.CHUNK, new String[]{
                "copper", "tin", "zinc", "iron", "lead", "aluminium", "chromium", "gold", "cadmium", "silver", "bismuth", "nickel"
            }));
            GameRegistry.register(DUST = new MetalItem("dust", "dust", MetalItem.Type.DUST, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "gold", "duralumin",
                "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.register(BEAD = new MetalItem("bead", "bead", MetalItem.Type.BEAD, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "gold", "duralumin",
                "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.register(NUGGET = new MetalItem("nugget", "nugget", MetalItem.Type.NUGGET, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "duralumin", "nicrosil",
                "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));
            GameRegistry.register(INGOT = new MetalItem("ingot", "ingot", MetalItem.Type.INGOT, new String[]{
                "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "lead", "nickel", "silver", "bismuth", "duralumin", "nicrosil",
                "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
            }));

            // Add items to the ore dictionary
            CHUNK.registerOreDict();
            BEAD.registerOreDict();
            DUST.registerOreDict();
            INGOT.registerOreDict();
            NUGGET.registerOreDict();
        }
    }

    /**
     * Container class for all networking related objects in the Allomancy module.
     */
    public static class CommonNetworking
    {
        /**
         * Initialise the Allomancy network sub-module and register
         * {@link Translator Translators}, {@link Message Messages}
         * or {@link de.mineformers.investiture.network.Message.Handler handlers}.
         */
        @SuppressWarnings("unchecked")
        public static void init()
        {
            Investiture.net().registerMessage(ToggleBurningMetal.class);
            Investiture.net().registerMessage(MetalExtractorUpdate.class);
            Investiture.net().registerMessage(AllomancerUpdate.class);
            Investiture.net().registerMessage(MistingUpdate.class);

            Investiture.net().registerMessage(TargetEffect.class);
            Investiture.net().registerMessage(SpeedBubbleUpdate.class);

            // Add handler for toggling the burning of a metal
            Investiture.net().addHandler(ToggleBurningMetal.class, Side.SERVER, (msg, ctx) ->
            {
                ctx.schedule(() ->
                                 getAllomancer(ctx.player())
                                     .ifPresent(a ->
                                                {
                                                    Metal metal = Metals.get(msg.metal);

                                                    if (a.activePowers().contains(metal.mistingType()))
                                                    {
                                                        a.deactivate(metal.mistingType());
                                                    }
                                                    else
                                                    {
                                                        a.activate(metal.mistingType());
                                                    }
                                                }));
                return null;
            });

            Investiture.net().addHandler(TargetEffect.class, Side.SERVER, (msg, ctx) ->
            {
                ctx.schedule(() ->
                             {
                                 Entity entity = ctx.player().world.getEntityByID(msg.entityId);
                                 if (entity != null)
                                 {
                                     getAllomancer(entity)
                                         .flatMap(a ->
                                                  {
                                                      try
                                                      {
                                                          return a.as((Class<? extends Misting>) Class.forName(msg.type));
                                                      }
                                                      catch (ClassNotFoundException e)
                                                      {
                                                          Throwables.propagate(e);
                                                      }
                                                      return Optional.empty();
                                                  })
                                         .filter(m -> m instanceof Targeting && ((Targeting) m).isValid(msg.target))
                                         .ifPresent(t -> ((Targeting) t).apply(msg.target));
                                 }
                             });
                return null;
            });
        }
    }
}
