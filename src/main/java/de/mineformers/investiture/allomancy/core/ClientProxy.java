package de.mineformers.investiture.allomancy.core;

import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.client.gui.MetalSelectionHUD;
import de.mineformers.investiture.allomancy.client.renderer.tileentity.MetalExtractorRenderer;
import de.mineformers.investiture.allomancy.item.AllomanticMetalIngot;
import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.client.renderer.block.ModuleStateMap;
import de.mineformers.investiture.core.Proxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handles all Allomancy-level operations specific to the dedicated client.
 */
public class ClientProxy implements Proxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        OBJLoader.instance.addDomain(Allomancy.DOMAIN);
        MinecraftForge.EVENT_BUS.register(new MetalSelectionHUD());

        // Assign models to each allomantic metal ingot
        final List<ModelResourceLocation> ingotResources =
            FluentIterable.from(Arrays.asList(AllomanticMetalIngot.NAMES))
                          .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_ingot", "metal=" + n))
                          .toList();
        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_ingot,
                                            stack -> ingotResources.get(AllomanticMetalIngot.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_ingot,
                                         ingotResources.toArray(new ModelResourceLocation[ingotResources.size()]));

        registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.allomantic_ore);
        registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.metal_extractor, ModuleStateMap.builder().ignore(MetalExtractor.MASTER));

        // Register key bindings
        ClientRegistry.registerKeyBinding(KeyBindings.SHOW_DIAL);

        // Handle changes in a storage of allomantic metals
        Investiture.net().addHandler(EntityMetalStorageUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                if (ctx.player() != null)
                {
                    Entity entity = ctx.player().worldObj.getEntityByID(msg.entity);
                    MetalStorage.from(entity).copy(msg.storage);
                }
            });
            return null;
        });

        // Handle changes in a burner of allomantic metals
        Investiture.net().addHandler(EntityMetalBurnerUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                if (ctx.player() != null)
                {
                    Entity entity = ctx.player().worldObj.getEntityByID(msg.entity);
                    MetalBurner.from(entity).copy(msg.burner);
                }
            });
            return null;
        });

        Investiture.net().addHandler(MetalExtractorUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                if (ctx.player().worldObj.getTileEntity(msg.pos) instanceof TileMetalExtractorMaster)
                    ((TileMetalExtractorMaster) ctx.player().worldObj.getTileEntity(msg.pos)).processUpdate(msg);
            });
            return null;
        });
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMetalExtractorMaster.class, new MetalExtractorRenderer());
    }
}
