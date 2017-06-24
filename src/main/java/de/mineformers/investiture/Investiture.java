package de.mineformers.investiture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.block.Conveyor;
import de.mineformers.investiture.block.CrusherBlock;
import de.mineformers.investiture.block.MachinePart;
import de.mineformers.investiture.client.renderer.tileentity.CrusherRenderer;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.ModProxy;
import de.mineformers.investiture.core.RegistryCollectionEvent;
import de.mineformers.investiture.network.FunctionalNetwork;
import de.mineformers.investiture.tileentity.ConveyorInterface;
import de.mineformers.investiture.tileentity.Crusher;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Main entry point for the Investiture mod
 */
@Mod(modid = Investiture.MOD_ID, name = "Investiture", version = Investiture.MOD_VERSION)
public final class Investiture
{
    public static final String MOD_ID = "investiture";
    public static final String MOD_VERSION = "@VERSION@";
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(CreativeTabs.getNextID(), MOD_ID)
    {
        @SideOnly(Side.CLIENT)
        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items)
        {
            super.displayAllRelevantItems(items);
            Ordering<ItemStack> order =
                Ordering.<ItemStack>from((o1, o2) ->
                                         {
                                             if (o1.getItem() instanceof ItemBlock && !(o2.getItem() instanceof ItemBlock))
                                             {
                                                 return -1;
                                             }
                                             else if (!(o1.getItem() instanceof ItemBlock) && o2
                                                 .getItem() instanceof ItemBlock)
                                             {
                                                 return 1;
                                             }
                                             else
                                             {
                                                 return 0;
                                             }
                                         }).compound((o1, o2) -> o1.getItem().getRegistryName().toString()
                                                                   .compareToIgnoreCase(o2.getItem().getRegistryName().toString()));
            items.sort(order);
        }

        @Override
        @Nonnull
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Allomancy.Items.INGOT, 1, 8);
        }
    };
    @Mod.Instance(MOD_ID)
    public static Investiture instance;
    @SidedProxy(modId = MOD_ID,
        clientSide = "de.mineformers.investiture.core.ClientProxy",
        serverSide = "de.mineformers.investiture.core.ServerProxy")
    public static ModProxy proxy;

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
     * Fired during the server startup phase. Should be used for registering server side commands.
     *
     * @param event the event that triggers this method
     */
    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event)
    {
        // Delegate event to modules
        modules.forEach(m ->
                        {
                            log().info("Running server start for module '" + m.id() + "'");
                            m.serverStart(event);
                        });
    }

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
        modules.forEach(m ->
                        {
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
        MinecraftForge.EVENT_BUS.post(new RegistryCollectionEvent.Post());
        // Delegate event to modules
        modules.forEach(m ->
                        {
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
        modules.forEach(m ->
                        {
                            log().info("Running post-initialisation for module '" + m.id() + "'");
                            m.postInit(event);
                        });
        proxy.postInit(event);
    }

    /**
     * Container class for all blocks in the base mod.
     */
    @ObjectHolder(MOD_ID)
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class Blocks
    {
        private static final Block DUMMY = new Block(Material.AIR);
        public static final Block CONVEYOR = DUMMY;
        public static final Block MACHINE_PART = DUMMY;
        public static final Block CRUSHER = DUMMY;

        /**
         * Adds all blocks to the game's registry.
         */
        @SubscribeEvent
        public static void collect(RegistryCollectionEvent event)
        {
            event.registerBlock(Conveyor::new, Conveyor.ItemRepresentation::new);
            event.registerBlock(MachinePart::new, MachinePart.ItemRepresentation::new);
            event.registerBlock(CrusherBlock::new);

            event.registerTileEntity(ConveyorInterface.class, new ResourceLocation(MOD_ID, "conveyor_interface"));
            event.registerTileEntity(Crusher.class, new ResourceLocation(MOD_ID, "crusher"));
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event)
        {
            ClientRegistry.bindTileEntitySpecialRenderer(Crusher.class, new CrusherRenderer());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Investiture.Blocks.CONVEYOR), 0,
                                                       new ModelResourceLocation(Investiture.MOD_ID + ":conveyor", "inventory_normal"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Investiture.Blocks.CONVEYOR), 1,
                                                       new ModelResourceLocation(Investiture.MOD_ID + ":conveyor", "inventory_interface"));
            proxy.registerBlockResources(Investiture.MOD_ID, Investiture.Blocks.MACHINE_PART);
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Investiture.Blocks.CRUSHER), 0,
                                                       new ModelResourceLocation(Investiture.MOD_ID + ":crusher", "inventory"));
        }
    }
}
