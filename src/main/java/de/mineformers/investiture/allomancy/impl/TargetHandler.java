package de.mineformers.investiture.allomancy.impl;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Targeting;
import de.mineformers.investiture.allomancy.api.power.Effect;
import de.mineformers.investiture.allomancy.network.TargetEffect;
import de.mineformers.investiture.util.RayTracing;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ${JDOC}
 */
public class TargetHandler
{
    private RayTraceResult leftTarget;
    private RayTraceResult rightTarget;

    @SubscribeEvent
    public void onMouseClick(MouseEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (!Minecraft.getMinecraft().inGameHasFocus || player == null)
            return;
        if (player.getHeldItem(EnumHand.MAIN_HAND) == null && event.isButtonstate() && (event.getButton() == 0 || event.getButton() == 1))
        {
            updateTargets(event.getButton() == 0, player);
            if (leftTarget != null || rightTarget != null)
            {
                player.swingArm(EnumHand.MAIN_HAND);
                event.setCanceled(true);
            }
        }
    }

    private void updateTargets(boolean leftClick, EntityPlayer player)
    {

        AllomancyAPIImpl.INSTANCE.toAllomancer(player).ifPresent(a -> {

            List<Targeting> mistings = a.activePowers()
                                        .stream()
                                        .map(m -> a.as(m).get())
                                        .filter(m -> m instanceof Targeting)
                                        .map(m -> (Targeting) m)
                                        .collect(Collectors.toList());
            RayTraceResult blockHit = RayTracing.rayTraceBlocks(Minecraft.getMinecraft().thePlayer, 20,
                                                                s -> mistings.stream().anyMatch(m -> m.isValid(s.getPos())), false, false, false);
            RayTraceResult entityHit = RayTracing.rayTraceEntities(Minecraft.getMinecraft().thePlayer, 20,
                                                                   e -> mistings.stream().anyMatch(m -> m.isValid(e)));
            if (blockHit != null || entityHit != null)
            {
                RayTraceResult hit = blockHit;
                if (hit == null)
                    hit = entityHit;
                else if (entityHit != null)
                {
                    double blockDistance = blockHit.hitVec.distanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector());
                    double entityDistance = entityHit.hitVec.distanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector());
                    hit = blockDistance < entityDistance ? blockHit : entityHit;
                }
                final RayTraceResult finalHit = hit;
                Stream<Targeting> targeters = mistings.stream().filter(m -> m.isValid(finalHit));
                if (leftClick)
                {
                    leftTarget = targeters.filter(m -> m.effect() == Effect.PUSH)
                                          .map(t -> {
                                              apply(a, t, finalHit);
                                              return null;
                                          })
                                          .count() > 0 ? finalHit
                                                       : null;
                }
                else
                {
                    rightTarget = targeters.filter(m -> m.effect() == Effect.PULL)
                                           .map(t -> {
                                               apply(a, t, finalHit);
                                               return null;
                                           })
                                           .count() > 0 ? finalHit
                                                        : null;
                }
            }
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (event.phase != TickEvent.Phase.START || player == null || !Minecraft.getMinecraft().inGameHasFocus)
            return;
        AllomancyAPIImpl.INSTANCE.toAllomancer(player).ifPresent(a -> {
            List<Targeting> mistings = a.activePowers()
                                        .stream()
                                        .map(m -> a.as(m).get())
                                        .filter(m -> m instanceof Targeting)
                                        .map(m -> (Targeting) m)
                                        .collect(Collectors.toList());
            if (leftTarget == null && Mouse.isButtonDown(0))
            {
                if ((leftTarget.entityHit != null && leftTarget.entityHit.isDead) ||
                    mistings.stream().noneMatch(m -> m.isValid(leftTarget)))
                    updateTargets(true, player);
            }
            if (rightTarget == null && Mouse.isButtonDown(1))
            {
                if ((rightTarget.entityHit != null && rightTarget.entityHit.isDead) ||
                    mistings.stream().noneMatch(m -> m.isValid(rightTarget)))
                    updateTargets(true, player);
            }
            if (leftTarget != null && Mouse.isButtonDown(0) &&
                player.getHeldItem(EnumHand.MAIN_HAND) == null && player.getPositionVector().squareDistanceTo(leftTarget.hitVec) <= 400)
            {
                mistings.stream()
                        .filter(m -> m.isValid(leftTarget) && m.repeatEvent() && m.effect() == Effect.PUSH)
                        .forEach(t -> apply(a, t, leftTarget));
            }
            else
            {
                leftTarget = null;
            }

            if (rightTarget != null && Mouse.isButtonDown(1) &&
                player.getHeldItem(EnumHand.MAIN_HAND) == null && player.getPositionVector().squareDistanceTo(rightTarget.hitVec) <= 400)
            {
                mistings.stream()
                        .filter(m -> m.isValid(rightTarget) && m.repeatEvent() && m.effect() == Effect.PULL)
                        .forEach(t -> apply(a, t, rightTarget));
            }
            else
            {
                rightTarget = null;
            }
        });
    }

    private void apply(Allomancer allomancer, Targeting misting, RayTraceResult target)
    {
        Investiture.net().sendToServer(new TargetEffect(Minecraft.getMinecraft().thePlayer.getEntityId(),
                                                        allomancer.powers()
                                                                  .stream()
                                                                  .filter(t -> t.isAssignableFrom(misting.getClass()))
                                                                  .findFirst().get().getName(),
                                                        target));
        misting.apply(target);
    }
}
