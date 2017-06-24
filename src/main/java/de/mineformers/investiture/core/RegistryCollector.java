package de.mineformers.investiture.core;

import de.mineformers.investiture.Investiture;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Investiture.MOD_ID)
public class RegistryCollector
{
    private static final RegistryCollectionEvent EVENT = new RegistryCollectionEvent();

    @SubscribeEvent
    public static void onNewRegistry(RegistryEvent.NewRegistry event)
    {
        EVENT.clear();
        MinecraftForge.EVENT_BUS.post(EVENT);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        Iterable<RegistryCollectionEvent.Entry> entries = EVENT.getEntries();
        for (RegistryCollectionEvent.Entry entry : entries)
        {
            entry.registerBlock(event.getRegistry());
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        Iterable<RegistryCollectionEvent.Entry> entries = EVENT.getEntries();
        for (RegistryCollectionEvent.Entry entry : entries)
        {
            entry.registerItem(event.getRegistry());
        }
    }
}
