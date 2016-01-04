package de.mineformers.investiture.allomancy.core;

import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.AllomanticMetalOre;
import de.mineformers.investiture.allomancy.client.gui.MetalHUD;
import de.mineformers.investiture.allomancy.item.AllomanticMetalIngot;
import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.allomancy.network.EntityMetalBurnerUpdate;
import de.mineformers.investiture.allomancy.network.EntityMetalStorageUpdate;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.client.renderer.block.ModuleStateMapper;
import de.mineformers.investiture.core.Proxy;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.List;

/**
 * ClientProxy
 *
 * @author PaleoCrafter
 */
public class ClientProxy implements Proxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new MetalHUD());

        final List<ModelResourceLocation> ingotResources =
            FluentIterable.from(Arrays.asList(AllomanticMetalIngot.NAMES))
                          .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_ingot", "metal=" + n))
                          .toList();

        ModelLoader.setCustomMeshDefinition(Allomancy.Items.allomantic_ingot,
                                            stack -> ingotResources.get(AllomanticMetalIngot.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Allomancy.Items.allomantic_ingot,
                                         ingotResources.toArray(new ModelResourceLocation[ingotResources.size()]));

        final List<ModelResourceLocation> oreResources =
            FluentIterable.from(Arrays.asList(AllomanticMetalOre.NAMES))
                          .transform(n -> new ModelResourceLocation(Allomancy.DOMAIN + ":allomantic_metal_ore", "metal=" + n))
                          .toList();

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(Allomancy.Blocks.allomantic_ore),
                                            stack -> oreResources.get(AllomanticMetalOre.clampDamage(stack.getItemDamage())));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(Allomancy.Blocks.allomantic_ore),
                                         oreResources.toArray(new ModelResourceLocation[oreResources.size()]));
        ModelLoader.setCustomStateMapper(Allomancy.Blocks.allomantic_ore, new ModuleStateMapper(Allomancy.DOMAIN));

        ClientRegistry.registerKeyBinding(KeyBindings.SHOW_DIAL);

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
    }
}