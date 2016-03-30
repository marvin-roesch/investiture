package de.mineformers.investiture.allomancy.core;

import com.google.common.base.Throwables;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.block.MetalExtractorController;
import de.mineformers.investiture.allomancy.client.gui.MetalSelectionHUD;
import de.mineformers.investiture.allomancy.client.renderer.misting.SpeedBubbleRenderer;
import de.mineformers.investiture.allomancy.client.renderer.tileentity.MetalExtractorRenderer;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.EntityAllomancer;
import de.mineformers.investiture.allomancy.impl.TargetHandler;
import de.mineformers.investiture.allomancy.impl.misting.physical.AbstractMetalManipulator;
import de.mineformers.investiture.allomancy.impl.misting.physical.TineyeImpl;
import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubble;
import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubbles;
import de.mineformers.investiture.allomancy.item.MetalItem;
import de.mineformers.investiture.allomancy.network.AllomancerUpdate;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.allomancy.network.MistingUpdate;
import de.mineformers.investiture.allomancy.network.SpeedBubbleUpdate;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.client.renderer.block.ModuleStateMap;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all Allomancy-level operations specific to the dedicated client.
 */
public class ClientProxy extends de.mineformers.investiture.core.ClientProxy
{
    @Override
    @SuppressWarnings("unchecked")
    public void preInit(FMLPreInitializationEvent event)
    {
        OBJLoader.INSTANCE.addDomain(Allomancy.DOMAIN);

        registerMetalResources(Allomancy.Items.allomantic_ingot);
        registerMetalResources(Allomancy.Items.allomantic_chunk);
        registerMetalResources(Allomancy.Items.allomantic_nugget);
        registerMetalResources(Allomancy.Items.allomantic_bead);
        registerMetalResources(Allomancy.Items.allomantic_dust);

        registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.allomantic_ore);
        registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.metal_extractor);
        registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.metal_extractor_controller,
                               ModuleStateMap.builder().ignore(MetalExtractorController.MASTER));

        // Register key bindings
        KeyBindings.init();
        MinecraftForge.EVENT_BUS.register(new MetalSelectionHUD());

        MinecraftForge.EVENT_BUS.register(new AbstractMetalManipulator.EventHandler());
        MinecraftForge.EVENT_BUS.register(new TineyeImpl.EventHandler());
        MinecraftForge.EVENT_BUS.register(new TargetHandler());

//        // Handle changes in a storage of allomantic metals
//        Investiture.net().addHandler(EntityMetalStorageUpdate.class, Side.CLIENT, (msg, ctx) -> {
//            ctx.schedule(() -> {
//                if (ctx.player() != null)
//                {
//                    Entity entity = ctx.player().worldObj.getEntityByID(msg.entity);
//                    MetalStorage.from(entity).copy(msg.storage);
//                }
//            });
//            return null;
//        });
//
//        // Handle changes in a burner of allomantic metals
//        Investiture.net().addHandler(EntityMetalBurnerUpdate.class, Side.CLIENT, (msg, ctx) -> {
//            ctx.schedule(() -> {
//                if (ctx.player() != null)
//                {
//                    Entity entity = ctx.player().worldObj.getEntityByID(msg.entity);
//                    MetalBurner.from(entity).copy(msg.burner);
//                }
//            });
//            return null;
//        });

        Investiture.net().addHandler(MetalExtractorUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                if (ctx.player().worldObj.getTileEntity(msg.pos) instanceof TileMetalExtractorMaster)
                    ((TileMetalExtractorMaster) ctx.player().worldObj.getTileEntity(msg.pos)).processUpdate(msg);
            });
            return null;
        });

        Investiture.net().addHandler(AllomancerUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                Entity entity = ctx.player().worldObj.getEntityByID(msg.entityId);
                AllomancyAPIImpl.INSTANCE.toAllomancer(entity).ifPresent(a -> {
                    if (a instanceof EntityAllomancer)
                        ((EntityAllomancer) a).setActivePowers(msg.activePowers);
                });
            });
            return null;
        });

        Investiture.net().addHandler(MistingUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                Entity entity = ctx.player().worldObj.getEntityByID(msg.entityId);
                try
                {
                    AllomancyAPIImpl.INSTANCE.read(entity, (Class<? extends Misting>) Class.forName(msg.type), msg.data);
                }
                catch (ClassNotFoundException e)
                {
                    Throwables.propagate(e);
                }
            });
            return null;
        });

        Investiture.net().addHandler(SpeedBubbleUpdate.class, Side.CLIENT, (msg, ctx) -> {
            ctx.schedule(() -> {
                if (ctx.player().dimension != msg.dimension)
                    return;
                SpeedBubble bubble = new SpeedBubble(msg.dimension, msg.position, msg.radius);
                if (msg.action == SpeedBubbleUpdate.ACTION_ADD)
                    SpeedBubbles.from(ctx.player().worldObj).add(bubble);
                else if (msg.action == SpeedBubbleUpdate.ACTION_REMOVE)
                    SpeedBubbles.from(ctx.player().worldObj).remove(bubble);
            });
            return null;
        });
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new SpeedBubbleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMetalExtractorMaster.class, new MetalExtractorRenderer());
    }

    private void registerMetalResources(MetalItem item)
    {
        final List<ModelResourceLocation> resources =
            Arrays.stream(item.getNames())
                  .map(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_" + item.getItemType(), "metal=" + n))
                  .collect(Collectors.toList());
        ModelLoader.setCustomMeshDefinition(item, stack -> resources.get(item.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(item, resources.toArray(new ResourceLocation[resources.size()]));
    }
}
