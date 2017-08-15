package de.mineformers.investiture.allomancy.core;

import com.google.common.base.Throwables;
import com.google.common.collect.ListMultimap;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackProvider;
import de.mineformers.investiture.allomancy.api.metal.stack.SingleMetalStackProvider;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.client.gui.MetalSelectionHUD;
import de.mineformers.investiture.allomancy.client.renderer.misting.SpeedBubbleRenderer;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.EntityAllomancer;
import de.mineformers.investiture.allomancy.impl.SimpleMetalStorage;
import de.mineformers.investiture.allomancy.impl.TargetHandler;
import de.mineformers.investiture.allomancy.impl.misting.physical.AbstractMetalManipulator;
import de.mineformers.investiture.allomancy.impl.misting.physical.TineyeImpl;
import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubble;
import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubbles;
import de.mineformers.investiture.allomancy.item.MetalItem;
import de.mineformers.investiture.allomancy.network.AllomancerStorageUpdate;
import de.mineformers.investiture.allomancy.network.AllomancerUpdate;
import de.mineformers.investiture.allomancy.network.MistingUpdate;
import de.mineformers.investiture.allomancy.network.SpeedBubbleUpdate;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.core.ManifestationProxy;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all Allomancy-level operations specific to the dedicated client.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy implements ManifestationProxy
{
    @Override
    @SuppressWarnings("unchecked")
    public void preInit(FMLPreInitializationEvent event)
    {
        OBJLoader.INSTANCE.addDomain(Allomancy.DOMAIN);

        MinecraftForge.EVENT_BUS.register(new EventHandler());

        // Register key bindings
        KeyBindings.init();
        MinecraftForge.EVENT_BUS.register(new MetalSelectionHUD());

        MinecraftForge.EVENT_BUS.register(new AbstractMetalManipulator.EventHandler());
        MinecraftForge.EVENT_BUS.register(new TineyeImpl.EventHandler());
        MinecraftForge.EVENT_BUS.register(new TargetHandler());

        Investiture.net().addHandler(AllomancerUpdate.class, Side.CLIENT, (msg, ctx) ->
        {
            ctx.schedule(() ->
                         {
                             Entity entity = ctx.player().world.getEntityByID(msg.entityId);
                             AllomancyAPIImpl.INSTANCE.toAllomancer(entity)
                                                      .ifPresent(a ->
                                                                 {
                                                                     if (a instanceof EntityAllomancer)
                                                                     {
                                                                         ((EntityAllomancer) a).setStorage(msg.metalStorage);
                                                                         msg.metalStorage.allomancer = (EntityAllomancer) a;
                                                                         ((EntityAllomancer) a).setActivePowers(msg.activePowers);
                                                                     }
                                                                 });
                         });
            return null;
        });

        Investiture.net().addHandler(MistingUpdate.class, Side.CLIENT, (msg, ctx) ->
        {
            ctx.schedule(() ->
                         {
                             Entity entity = ctx.player().world.getEntityByID(msg.entityId);
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

        Investiture.net().addHandler(AllomancerStorageUpdate.class, Side.CLIENT, (msg, ctx) ->
        {
            ctx.schedule(() ->
                         {
                             Entity entity = ctx.player().world.getEntityByID(msg.entityId);
                             AllomancyAPIImpl.INSTANCE.toAllomancer(entity)
                                                      .ifPresent(a ->
                                                                 {
                                                                     if (a instanceof EntityAllomancer)
                                                                     {
                                                                         ListMultimap<Metal, MetalStack> storage =
                                                                             ((SimpleMetalStorage) a.storage()).storage;
                                                                         switch (msg.action)
                                                                         {
                                                                             case AllomancerStorageUpdate.ACTION_APPEND:
                                                                                 storage.put(msg.stack.getMetal(), msg.stack);
                                                                                 break;
                                                                             case AllomancerStorageUpdate.ACTION_UPDATE_LAST:
                                                                                 List<MetalStack> existing = storage.get(msg.stack.getMetal());
                                                                                 if (!existing.isEmpty())
                                                                                 {
                                                                                     existing.set(existing.size() - 1, msg.stack);
                                                                                 }
                                                                                 break;
                                                                             case AllomancerStorageUpdate.ACTION_REMOVE_LAST:
                                                                                 List<MetalStack> removable = storage.get(msg.stack.getMetal());
                                                                                 if (!removable.isEmpty())
                                                                                 {
                                                                                     removable.remove(removable.size() - 1);
                                                                                 }
                                                                                 break;
                                                                         }
                                                                     }
                                                                 });
                         });
            return null;
        });

        Investiture.net().addHandler(SpeedBubbleUpdate.class, Side.CLIENT, (msg, ctx) ->
        {
            ctx.schedule(() ->
                         {
                             if (ctx.player().dimension != msg.dimension)
                                 return;
                             if (msg.action == SpeedBubbleUpdate.ACTION_ADD)
                                 SpeedBubbles.from(ctx.player().world).add(msg.owner, msg.dimension, msg.position, msg.radius);
                             else if (msg.action == SpeedBubbleUpdate.ACTION_REMOVE)
                                 SpeedBubbles.from(ctx.player().world).remove(msg.owner);
                         });
            return null;
        });
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new SpeedBubbleRenderer());
    }

    private static class EventHandler
    {
        @SubscribeEvent
        public void onTooltip(ItemTooltipEvent event)
        {
            ItemStack stack = event.getItemStack();
            if (!stack.hasCapability(Capabilities.METAL_STACK_PROVIDER, null))
                return;
            MetalStackProvider provider = stack.getCapability(Capabilities.METAL_STACK_PROVIDER, null);
            if (provider instanceof SingleMetalStackProvider)
            {
                // Put purity in the tooltip
                MetalStack metal = provider.get().get(0);
                event.getToolTip().add(I18n.format("allomancy.message.purity", Investiture.proxy.getPercentageFormat().format(metal.getPurity())));
            }
        }

        @SubscribeEvent
        public void registerModels(ModelRegistryEvent event)
        {
            registerMetalResources(Allomancy.Items.INGOT);
            registerMetalResources(Allomancy.Items.CHUNK);
            registerMetalResources(Allomancy.Items.NUGGET);
            registerMetalResources(Allomancy.Items.BEAD);
            registerMetalResources(Allomancy.Items.DUST);
        }

        private static void registerMetalResources(MetalItem item)
        {
            final List<ModelResourceLocation> resources =
                Arrays.stream(item.getMetals())
                      .map(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_" + item.getItemType(), "metal=" + n))
                      .collect(Collectors.toList());
            ModelLoader.setCustomMeshDefinition(item, stack -> resources.get(item.clampDamage(stack.getItemDamage())));
            ModelBakery.registerItemVariants(item, resources.toArray(new ResourceLocation[resources.size()]));
        }
    }
}
