package de.mineformers.investiture.allomancy;

import com.google.common.base.Throwables;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.misting.Targeting;
import de.mineformers.investiture.allomancy.block.MetalOre;
import de.mineformers.investiture.allomancy.core.AllomancyCommand;
import de.mineformers.investiture.allomancy.crusher.CrusherRecipes;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.CoreEventHandler;
import de.mineformers.investiture.allomancy.impl.SimpleMetalStorage;
import de.mineformers.investiture.allomancy.impl.misting.temporal.AugurImpl;
import de.mineformers.investiture.allomancy.item.MetalItem;
import de.mineformers.investiture.allomancy.network.*;
import de.mineformers.investiture.allomancy.world.MetalGenerator;
import de.mineformers.investiture.core.Manifestation;
import de.mineformers.investiture.core.Proxy;
import de.mineformers.investiture.core.RegistryCollectionEvent;
import de.mineformers.investiture.network.Message;
import de.mineformers.investiture.serialisation.Serialisation;
import de.mineformers.investiture.serialisation.Translator;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.mineformers.investiture.Investiture.MOD_ID;
import static de.mineformers.investiture.allomancy.api.metal.Metals.*;
import static de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl.getAllomancer;

/**
 * The "Allomancy" module is based on the "Mistborn" series of books by Brandon Sanderson.
 * <p>
 * The focus of this module are so called allomancers who can burn metals to gain special powers.
 */
public final class Allomancy implements Manifestation
{
    public static final String DOMAIN = "allomancy";
    @SidedProxy(modId = MOD_ID,
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
        proxy.serverStart(event);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        CoreEventHandler.init();
        Metals.init();
        CrusherRecipes.register(IRON);
        CrusherRecipes.register(GOLD);
        CrusherRecipes.register(COPPER);
        CrusherRecipes.register(ZINC, ZINC, CADMIUM, 0.5f);
        CrusherRecipes.register(TIN);
        CrusherRecipes.register(ALUMINIUM);
        CrusherRecipes.register(CHROMIUM);
        CrusherRecipes.register(SILVER);
        CrusherRecipes.register(BISMUTH);
        CrusherRecipes.register(LEAD);
        AllomancyAPIImpl.INSTANCE.init();

        MinecraftForge.EVENT_BUS.register(new AugurImpl.EventHandler());
        CommonNetworking.init();

        proxy.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        GameRegistry.registerWorldGenerator(new MetalGenerator(), 0);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

    /**
     * Container class for all blocks in the Allomancy module.
     */
    @ObjectHolder(MOD_ID)
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class Blocks
    {
        public static final MetalOre ALLOMANTIC_ORE = null;

        @SubscribeEvent
        public static void collect(RegistryCollectionEvent event)
        {
            event.registerBlock(MetalOre::new, MetalOre.ItemRepresentation::new);
        }

        @SubscribeEvent
        public static void onRegistrationFinished(RegistryCollectionEvent.Post event)
        {
            // Add ores to the ore dictionary
            for (int i = 0; i < MetalOre.NAMES.length; i++)
            {
                OreDictionary
                    .registerOre(String.format("ore%s", StringUtils.capitalize(MetalOre.NAMES[i])), new ItemStack(ALLOMANTIC_ORE, 1, i));
            }
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event)
        {
            proxy.registerBlockResources(Allomancy.DOMAIN, Allomancy.Blocks.ALLOMANTIC_ORE);
        }
    }

    /**
     * Container class for all items in the Allomancy module.
     */
    @ObjectHolder(MOD_ID)
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class Items
    {
        public final static MetalItem INGOT = null;
        public final static MetalItem NUGGET = null;
        public final static MetalItem BEAD = null;
        public final static MetalItem CHUNK = null;
        public final static MetalItem DUST = null;

        /**
         * Adds all items to the game's registry.
         */
        @SubscribeEvent
        public static void collect(RegistryCollectionEvent event)
        {
            event.registerItem(() -> new MetalItem("chunk", "chunk", MetalItem.Type.CHUNK, new Metal[]{
                COPPER, TIN, ZINC, IRON, LEAD, ALUMINIUM, CHROMIUM, GOLD, CADMIUM, SILVER, BISMUTH, NICKEL
            }));
            event.registerItem(() -> new MetalItem("dust", "dust", MetalItem.Type.DUST, new Metal[]{
                BRONZE, BRASS, COPPER, ZINC, TIN, PEWTER, STEEL, IRON, LEAD, NICKEL, SILVER, BISMUTH, GOLD, DURALUMIN,
                NICROSIL, ALUMINIUM, CHROMIUM, CADMIUM, ELECTRUM, BENDALLOY
            }));
            event.registerItem(() -> new MetalItem("bead", "bead", MetalItem.Type.BEAD, new Metal[]{
                BRONZE, BRASS, COPPER, ZINC, TIN, PEWTER, STEEL, IRON, LEAD, NICKEL, SILVER, BISMUTH, GOLD, DURALUMIN,
                NICROSIL, ALUMINIUM, CHROMIUM, CADMIUM, ELECTRUM, BENDALLOY
            }));
            event.registerItem(() -> new MetalItem("nugget", "nugget", MetalItem.Type.NUGGET, new Metal[]{
                BRONZE, BRASS, COPPER, ZINC, TIN, PEWTER, STEEL, LEAD, NICKEL, SILVER, BISMUTH, DURALUMIN, NICROSIL,
                ALUMINIUM, CHROMIUM, CADMIUM, ELECTRUM, BENDALLOY
            }));
            event.registerItem(() -> new MetalItem("ingot", "ingot", MetalItem.Type.INGOT, new Metal[]{
                BRONZE, BRASS, COPPER, ZINC, TIN, PEWTER, STEEL, LEAD, NICKEL, SILVER, BISMUTH, DURALUMIN, NICROSIL,
                ALUMINIUM, CHROMIUM, CADMIUM, ELECTRUM, BENDALLOY
            }));
        }

        @SubscribeEvent
        public static void onRegistrationFinished(RegistryCollectionEvent.Post event)
        {
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
            Serialisation.INSTANCE.registerTranslator(MetalStack.class, new Translator<MetalStack, NBTTagCompound>()
            {
                @Override
                public void serialiseImpl(MetalStack value, ByteBuf buffer)
                {
                    ByteBufUtils.writeUTF8String(buffer, value.getMetal().id());
                    buffer.writeFloat(value.getQuantity());
                    buffer.writeFloat(value.getPurity());
                }

                @Override
                public MetalStack deserialiseImpl(ByteBuf buffer)
                {
                    String id = ByteBufUtils.readUTF8String(buffer);
                    float quantity = buffer.readFloat();
                    float purity = buffer.readFloat();
                    return new MetalStack(Metals.get(id), quantity, purity);
                }

                @Override
                public NBTTagCompound serialiseImpl(MetalStack value)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("Metal", value.getMetal().id());
                    tag.setFloat("Quantity", value.getQuantity());
                    tag.setFloat("Purity", value.getPurity());
                    return tag;
                }

                @Override
                public MetalStack deserialiseImpl(NBTTagCompound tag)
                {
                    return new MetalStack(tag);
                }
            });
            Serialisation.INSTANCE.registerTranslator(SimpleMetalStorage.class, new Translator<SimpleMetalStorage, NBTTagCompound>()
            {
                @Override
                public void serialiseImpl(SimpleMetalStorage value, ByteBuf buffer)
                {
                    Set<Metal> metals = value.getStoredMetals();
                    buffer.writeInt(metals.size());
                    for (Metal metal : metals)
                    {
                        ByteBufUtils.writeUTF8String(buffer, metal.id());
                        List<? extends MetalStack> stacks = value.getStored(metal);
                        buffer.writeInt(stacks.size());
                        for (MetalStack stack : stacks)
                        {
                            Serialisation.INSTANCE.writeToBuffer(stack, buffer);
                        }
                    }
                }

                @Override
                public SimpleMetalStorage deserialiseImpl(ByteBuf buffer)
                {
                    SimpleMetalStorage storage = new SimpleMetalStorage();
                    int metalCount = buffer.readInt();
                    for (int i = 0; i < metalCount; i++)
                    {
                        String id = ByteBufUtils.readUTF8String(buffer);
                        Metal metal = Metals.get(id);
                        int stackCount = buffer.readInt();
                        for (int j = 0; j < stackCount; j++)
                        {
                            storage.storage.put(metal, Serialisation.INSTANCE.readFromBuffer(MetalStack.class, buffer));
                        }
                    }
                    return storage;
                }

                @Override
                public NBTTagCompound serialiseImpl(SimpleMetalStorage value)
                {
                    return value.serializeNBT();
                }

                @Override
                public SimpleMetalStorage deserialiseImpl(NBTTagCompound tag)
                {
                    SimpleMetalStorage storage = new SimpleMetalStorage();
                    storage.deserializeNBT(tag);
                    return storage;
                }
            });

            Investiture.net().registerMessage(ToggleBurningMetal.class);
            Investiture.net().registerMessage(MetalExtractorUpdate.class);
            Investiture.net().registerMessage(AllomancerUpdate.class);
            Investiture.net().registerMessage(AllomancerStorageUpdate.class);
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
