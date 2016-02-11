package de.mineformers.investiture.allomancy.impl.misting.mental;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.mental.EmotionManipulator;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import de.mineformers.investiture.util.Reflection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.village.Village;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.Predicate;

/**
 * ${JDOC}
 */
public abstract class AbstractEmotionManipulator extends AbstractMisting implements EmotionManipulator, ITickable
{
    private static final MethodHandle GET_VILLAGE;

    static
    {
        GET_VILLAGE = Reflection.getterHandle(EntityVillager.class)
                                .mcpName("villageObj")
                                .srgName("field_70954_d")
                                .build();
    }

    @Nonnull
    public static Village getVillage(EntityVillager villager)
    {
        try
        {
            return (Village) GET_VILLAGE.bindTo(villager).invokeExact();
        }
        catch (Throwable throwable)
        {
            Throwables.propagate(throwable);
            // Because Java is stupid
            return null;
        }
    }

    @Inject
    private Entity entity;
    private Map<EntityCreature, AIData> data = new HashMap<>();

    abstract protected AIData gather(EntityCreature entity);

    @Override
    public void update()
    {
        for (Iterator<Map.Entry<EntityCreature, AIData>> it = data.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<EntityCreature, AIData> entry = it.next();
            AIData data = entry.getValue();
            if (data.update() || entry.getKey().isDead)
            {
                data.uninstall(entry.getKey());
                it.remove();
            }
        }
    }

    @Override
    public void apply(MovingObjectPosition target)
    {
        if (!isValid(target))
            return;
        EntityCreature entity = (EntityCreature) target.entityHit;
        if (entity instanceof EntityVillager && this.entity instanceof EntityPlayer)
        {
            if (entity.worldObj.isRemote)
                return;
            getVillage((EntityVillager) entity).setReputationForPlayer(this.entity.getName(), villagerReputation());
            entity.worldObj.setEntityState(entity, (byte) (villagerReputation() > 0 ? 14 : 13));
            return;
        }
        if (data.containsKey(entity))
        {
            data.get(entity).uninstall(entity);
            data.remove(entity);
        }
        data.put(entity, gather(entity));
    }

    @Override
    public boolean isValid(MovingObjectPosition target)
    {
        return target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && target.entityHit instanceof EntityCreature;
    }

    public abstract int villagerReputation();

    @Override
    public boolean repeatEvent()
    {
        return false;
    }

    static class AIData
    {
        public int counter = 0;
        public final Collection<EntityAITaskEntry> prevTasks;
        public final Collection<EntityAITaskEntry> prevTargetTasks;
        public final Collection<EntityAIBase> tasks;
        public final Collection<EntityAIBase> targetTasks;

        public static AIData install(EntityCreature entity,
                                     Map<EntityAIBase, Integer> tasks, Map<EntityAIBase, Integer> targetTasks,
                                     Predicate<EntityAIBase> removedTasks, Predicate<EntityAIBase> removedTargetTasks)
        {
            ImmutableList.Builder<EntityAITaskEntry> prevTasksBuilder = ImmutableList.builder();
            entity.tasks.taskEntries.stream().filter(e -> removedTasks.test(e.action)).forEach(prevTasksBuilder::add);
            ImmutableList.Builder<EntityAITaskEntry> prevTargetTasksBuilder = ImmutableList.builder();
            entity.targetTasks.taskEntries.stream().filter(e -> removedTargetTasks.test(e.action)).forEach(prevTargetTasksBuilder::add);

            List<EntityAITaskEntry> prevTasks = prevTasksBuilder.build();
            prevTasks.stream().map(e -> e.action).forEach(entity.tasks::removeTask);
            List<EntityAITaskEntry> prevTargetTasks = prevTasksBuilder.build();
            prevTargetTasks.stream().map(e -> e.action).forEach(entity.targetTasks::removeTask);

            for (Map.Entry<EntityAIBase, Integer> entry : tasks.entrySet())
            {
                entity.tasks.addTask(entry.getValue(), entry.getKey());
            }
            for (Map.Entry<EntityAIBase, Integer> entry : targetTasks.entrySet())
            {
                entity.targetTasks.addTask(entry.getValue(), entry.getKey());
            }

            entity.setRevengeTarget(null);
            entity.setAttackTarget(null);

            return new AIData(prevTasks, prevTargetTasks, tasks.keySet(), targetTasks.keySet());
        }

        private AIData(Collection<EntityAITaskEntry> prevTasks, Collection<EntityAITaskEntry> prevTargetTasks,
                       Collection<EntityAIBase> tasks, Collection<EntityAIBase> targetTasks)
        {
            this.prevTasks = prevTasks;
            this.prevTargetTasks = prevTargetTasks;
            this.tasks = tasks;
            this.targetTasks = targetTasks;
        }

        public boolean update()
        {
            counter++;
            return counter > 200;
        }

        public void uninstall(EntityCreature entity)
        {
            tasks.forEach(entity.tasks::removeTask);
            targetTasks.forEach(entity.targetTasks::removeTask);
            for (EntityAITaskEntry task : prevTasks)
            {
                entity.tasks.addTask(task.priority, task.action);
            }
            for (EntityAITaskEntry task : prevTargetTasks)
            {
                entity.targetTasks.addTask(task.priority, task.action);
            }
        }
    }
}
