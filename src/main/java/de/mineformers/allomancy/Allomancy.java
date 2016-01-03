package de.mineformers.allomancy;

import de.mineformers.allomancy.block.AllomanticMetalOre;
import de.mineformers.allomancy.core.Proxy;
import de.mineformers.allomancy.item.AllomanticMetalIngot;
import de.mineformers.allomancy.metal.AllomanticMetals;
import de.mineformers.allomancy.network.FunctionalNetwork;
import de.mineformers.allomancy.world.MetalGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Allomancy
 *
 * @author PaleoCrafter
 */
@Mod(modid = Allomancy.MOD_ID, name = "Allomancy", version = Allomancy.MOD_VERSION)
public class Allomancy {
    public static final String MOD_ID = "allomancy";
    public static final String MOD_VERSION = "0.0.1";
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(CreativeTabs.getNextID(), MOD_ID) {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return Items.allomantic_ingot;
        }

        @Override
        public int getIconItemDamage() {
            return 8;
        }
    };

    @Mod.Instance(MOD_ID)
    public static Allomancy instance;

    @SidedProxy(modId = MOD_ID, clientSide = "de.mineformers.allomancy.core.ClientProxy",
            serverSide = "de.mineformers.allomancy.core.ServerSide")
    public static Proxy proxy;

    public static FunctionalNetwork net() {
        return instance.network;
    }

    private FunctionalNetwork network;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        network = FunctionalNetwork.create(MOD_ID);
        Blocks.register();
        Items.register();
        GameRegistry.registerWorldGenerator(new MetalGenerator(), 0);
        AllomanticMetals.init();
        proxy.preInit(event);

        CommonNetworking.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    public static class Blocks {
        public static AllomanticMetalOre allomantic_ore;

        public static void register() {
            GameRegistry.registerBlock(allomantic_ore = new AllomanticMetalOre(),
                    AllomanticMetalOre.ItemRepresentation.class);
        }
    }

    public static class Items {
        public static AllomanticMetalIngot allomantic_ingot;

        public static void register() {
            GameRegistry.registerItem(allomantic_ingot = new AllomanticMetalIngot());
        }
    }

    public static class NBT {
        public static final String STORAGE_ID = "allomancy_metal_storage";
        public static final String BURNER_ID = "allomancy_metal_burner";
    }

    public static class CommonNetworking {
        public static void init() {

        }
    }
}
