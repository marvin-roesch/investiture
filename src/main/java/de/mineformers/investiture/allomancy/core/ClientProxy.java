package de.mineformers.investiture.allomancy.core;

import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.client.gui.MetalSelectionHUD;
import de.mineformers.investiture.allomancy.client.renderer.tileentity.MetalExtractorRenderer;
import de.mineformers.investiture.allomancy.item.*;
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
                                            stack -> ingotResources.get(Allomancy.Items.allomantic_ingot.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_ingot,
                                         ingotResources.toArray(new ModelResourceLocation[ingotResources.size()]));

        // Assign models to each allomantic metal nugget
        final List<ModelResourceLocation> nuggetResources =
                FluentIterable.from(Arrays.asList(AllomanticMetalNugget.NAMES))
                        .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_nugget", "metal=" + n))
                        .toList();
        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_nugget,
                stack -> nuggetResources.get(Allomancy.Items.allomantic_nugget.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_nugget,
                nuggetResources.toArray(new ModelResourceLocation[nuggetResources.size()]));

        // Assign models to each allomantic metal bead
        final List<ModelResourceLocation> beadResources =
                FluentIterable.from(Arrays.asList(AllomanticMetalBead.NAMES))
                        .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_bead", "metal=" + n))
                        .toList();
        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_bead,
                stack -> beadResources.get(Allomancy.Items.allomantic_bead.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_bead,
                beadResources.toArray(new ModelResourceLocation[beadResources.size()]));

        // Assign models to each allomantic metal chunk
        final List<ModelResourceLocation> chunkResources =
                FluentIterable.from(Arrays.asList(AllomanticMetalChunk.NAMES))
                        .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_chunk", "metal=" + n))
                        .toList();
        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_chunk,
                stack -> chunkResources.get(Allomancy.Items.allomantic_chunk.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_chunk,
                chunkResources.toArray(new ModelResourceLocation[chunkResources.size()]));

        // Assign models to each allomantic metal dust
        final List<ModelResourceLocation> dustResources =
                FluentIterable.from(Arrays.asList(AllomanticMetalDust.NAMES))
                        .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_dust", "metal=" + n))
                        .toList();
        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_dust,
                stack -> dustResources.get(Allomancy.Items.allomantic_dust.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_dust,
                dustResources.toArray(new ModelResourceLocation[dustResources.size()]));

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
