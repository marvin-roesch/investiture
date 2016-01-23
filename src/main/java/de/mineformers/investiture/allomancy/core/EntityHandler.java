package de.mineformers.investiture.allomancy.core;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.metal.MetalBurner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Handles basic operations on entities related to Allomancy.
 */
public class EntityHandler
{
    /**
     * Registers required {@link net.minecraftforge.common.IExtendedEntityProperties IEEPs} to various entities.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onConstructing(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayer)
            if (MetalBurner.from(event.entity) == null)
                event.entity.registerExtendedProperties(Allomancy.NBT.BURNER_ID, new MetalBurner.EntityMetalBurner());
    }

    /**
     * Copies {@link net.minecraftforge.common.IExtendedEntityProperties IEEP} data between two player instances.
     * This happens on death or on teleport.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event)
    {
        MetalBurner oldStorage = MetalBurner.from(event.original);
        MetalBurner newStorage = MetalBurner.from(event.entity);

        newStorage.copy(oldStorage);
    }

    /**
     * Updates clients on the status of the player logging in.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onLogin(PlayerLoggedInEvent event)
    {
        ((MetalBurner.EntityMetalBurner) event.player.getExtendedProperties(Allomancy.NBT.BURNER_ID)).sync();
    }

    /**
     * Handles players swallowing metals in various forms.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event)
    {
        // Only do things on the server
        if (!event.entity.worldObj.isRemote && event.entityPlayer.getHeldItem() != null)
        {
            // Right click = swallow metal
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
            {
                MetalBurner metals = MetalBurner.from(event.entity);
                int consumed = metals.consume(event.entityPlayer.getHeldItem());
                if (consumed >= 0)
                    event.entityPlayer.getHeldItem().stackSize -= consumed;
            }
        }
    }

    /**
     * Updates every player's burning metals.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        // Only do things on the server
        if (event.player.worldObj.isRemote || event.phase == TickEvent.Phase.END)
            return;

        MetalBurner metals = MetalBurner.from(event.player);
        metals.burningMetals().forEach(m -> metals.updateBurnTimer(event.player, m));
    }
}
