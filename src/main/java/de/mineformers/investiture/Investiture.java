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

import java.util.List;

/**
 * Investiture
 *
 * @author PaleoCrafter
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
        serverSide = "de.mineformers.investiture.core.ServerSide")
    public static Proxy proxy;

    public static FunctionalNetwork net()
    {
        return instance.network;
    }

    private FunctionalNetwork network;
    private static final List<Manifestation> modules = ImmutableList.of(new Allomancy());

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        network = FunctionalNetwork.create(MOD_ID);
        modules.forEach(m -> m.preInit(event));
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        modules.forEach(m -> m.init(event));
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        modules.forEach(m -> m.postInit(event));
        proxy.postInit(event);
    }
}
