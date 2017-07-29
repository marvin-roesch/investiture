package de.mineformers.investiture.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegistryCollectionEvent extends Event
{
    private final List<BlockEntry> blocks = new ArrayList<>();
    private final List<ItemEntry> items = new ArrayList<>();
    private final List<TileEntityEntry> tileEntities = new ArrayList<>();

    public void registerBlock(Supplier<Block> factory)
    {
        registerBlock(factory, ItemBlock::new);
    }

    public void registerBlock(Supplier<Block> factory, @Nullable Function<Block, Item> itemFactory)
    {
        blocks.add(new BlockEntry(factory, itemFactory));
    }

    public void registerItem(Supplier<Item> factory)
    {
        items.add(new ItemEntry(factory));
    }

    public void registerTileEntity(Class<? extends TileEntity> clazz, ResourceLocation id)
    {
        tileEntities.add(new TileEntityEntry(clazz, id));
    }

    Iterable<Entry> getEntries()
    {
        return Iterables.concat(blocks, items, tileEntities);
    }

    void clear()
    {
        blocks.clear();
        items.clear();
        tileEntities.clear();
    }

    public static class Post extends Event
    {
    }

    static abstract class Entry
    {
        abstract void registerBlock(IForgeRegistry<Block> registry);

        abstract void registerItem(IForgeRegistry<Item> registry);
    }

    static class BlockEntry extends Entry
    {
        private final Supplier<Block> factory;
        @Nullable
        private final Function<Block, Item> itemFactory;
        @Nullable
        private ResourceLocation id = null;

        BlockEntry(Supplier<Block> factory, @Nullable Function<Block, Item> itemFactory)
        {
            this.factory = factory;
            this.itemFactory = itemFactory;
        }

        @Override
        void registerBlock(IForgeRegistry<Block> registry)
        {
            Block block = factory.get();
            this.id = block.getRegistryName();
            registry.register(block);
        }

        @Override
        void registerItem(IForgeRegistry<Item> registry)
        {
            if (itemFactory == null)
                return;
            Preconditions.checkNotNull(id, "Tried to create a new item for an unregistered block.");
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            Item item = itemFactory.apply(block);
            item.setRegistryName(block.getRegistryName());
            registry.register(item);
        }
    }

    static class ItemEntry extends Entry
    {
        private final Supplier<Item> factory;

        private ItemEntry(Supplier<Item> factory)
        {
            this.factory = factory;
        }

        @Override
        void registerBlock(IForgeRegistry<Block> registry)
        {
        }

        @Override
        void registerItem(IForgeRegistry<Item> registry)
        {
            registry.register(factory.get());
        }
    }

    static class TileEntityEntry extends Entry
    {
        private final Class<? extends TileEntity> clazz;
        private final ResourceLocation id;

        TileEntityEntry(Class<? extends TileEntity> clazz, ResourceLocation id)
        {
            this.clazz = clazz;
            this.id = id;
        }

        @Override
        void registerBlock(IForgeRegistry<Block> registry)
        {
            GameRegistry.registerTileEntity(clazz, id.toString());
        }

        @Override
        void registerItem(IForgeRegistry<Item> registry)
        {
        }
    }
}
