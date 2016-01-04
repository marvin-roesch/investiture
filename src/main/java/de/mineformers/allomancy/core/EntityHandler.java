package de.mineformers.allomancy.core;

import de.mineformers.allomancy.Allomancy;
import de.mineformers.allomancy.metal.MetalBurner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * EntityHandler
 *
 * @author PaleoCrafter
 */
public class EntityHandler {
    @SubscribeEvent
    public void onConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer)
            if (MetalBurner.from(event.entity) == null)
                event.entity.registerExtendedProperties(Allomancy.NBT.BURNER_ID, new MetalBurner.EntityMetalBurner());
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        MetalBurner oldStorage = MetalBurner.from(event.original);
        MetalBurner newStorage = MetalBurner.from(event.entity);

        newStorage.copy(oldStorage);
    }

    @SubscribeEvent
    public void onLogin(PlayerLoggedInEvent event) {
        ((MetalBurner.EntityMetalBurner) event.player.getExtendedProperties(Allomancy.NBT.BURNER_ID)).sync();
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!event.entity.worldObj.isRemote && event.entityPlayer.getHeldItem() != null) {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
                MetalBurner metals = MetalBurner.from(event.entity);
                if (metals.consume(event.entityPlayer.getHeldItem()))
                    event.entityPlayer.destroyCurrentEquippedItem();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.worldObj.isRemote || event.phase == TickEvent.Phase.END)
            return;

        MetalBurner metals = MetalBurner.from(event.player);
        metals.burningMetals().forEach(metals::updateBurnTimer);
    }
}
