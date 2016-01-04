package de.mineformers.investiture.allomancy;

import com.google.common.base.Optional;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.block.AllomanticMetalOre;
import de.mineformers.investiture.allomancy.core.EntityHandler;
import de.mineformers.investiture.allomancy.item.AllomanticMetalIngot;
import de.mineformers.investiture.allomancy.metal.AllomanticMetal;
import de.mineformers.investiture.allomancy.metal.AllomanticMetals;
import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.allomancy.network.ToggleBurningMetal;
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
 * Allomancy
 *
 * @author PaleoCrafter
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

    public static class Blocks
    {
        public static AllomanticMetalOre allomantic_ore;

        public static void register()
        {
            GameRegistry.registerBlock(allomantic_ore = new AllomanticMetalOre(), AllomanticMetalOre.ItemRepresentation.class);
        }
    }

    public static class Items
    {
        public static AllomanticMetalIngot allomantic_ingot;

        public static void register()
        {
            GameRegistry.registerItem(allomantic_ingot = new AllomanticMetalIngot());
        }
    }

    public static class NBT
    {
        public static final String STORAGE_ID = "allomancy_metal_storage";
        public static final String BURNER_ID = "allomancy_metal_burner";
    }

    public static class CommonNetworking
    {
        public static void init()
        {
            Message.registerTranslator(MetalStorage.class, new MetalStorage.Translator());
            Message.registerTranslator(MetalBurner.class, new MetalBurner.Translator());

            Investiture.net().registerMessage(EntityMetalStorageUpdate.class);
            Investiture.net().registerMessage(EntityMetalBurnerUpdate.class);
            Investiture.net().registerMessage(ToggleBurningMetal.class);

            Investiture.net().addHandler(ToggleBurningMetal.class, Side.SERVER, (msg, ctx) -> {
                ctx.schedule(() -> {
                    MetalBurner burner = MetalBurner.from(ctx.player());
                    Optional<AllomanticMetal> optional = AllomanticMetals.get(msg.metal);
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
