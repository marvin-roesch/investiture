package de.mineformers.investiture.allomancy.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.Capabilities;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.MetalStorage;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackProvider;
import de.mineformers.investiture.allomancy.network.AllomancerStorageUpdate;
import de.mineformers.investiture.allomancy.network.AllomancerUpdate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;

public class SimpleMetalStorage implements MetalStorage, INBTSerializable<NBTTagCompound>
{
    private static final float CAPACITY = 1000;
    public final ListMultimap<Metal, MetalStack> storage = ArrayListMultimap.create();
    public EntityAllomancer allomancer;

    public SimpleMetalStorage()
    {
    }

    public SimpleMetalStorage(EntityAllomancer allomancer)
    {
        this.allomancer = allomancer;
    }

    @Override
    public List<MetalStack> consume(List<MetalStack> stacks, boolean simulate)
    {
        return stacks;
    }

    @Override
    public List<MetalStack> get()
    {
        return Lists.newArrayList(storage.values());
    }

    @Override
    public ItemStack consume(ItemStack stack, int amount, boolean simulate)
    {
        if (stack.isEmpty() || !stack.hasCapability(Capabilities.METAL_STACK_PROVIDER, null))
            return stack;
        ItemStack consumableStack = stack.copy();
        consumableStack.setCount(Math.min(amount, stack.getCount()));
        MetalStackProvider consumableMetalProvider = consumableStack.getCapability(Capabilities.METAL_STACK_PROVIDER, null);
        MetalStackProvider metalProvider = stack.getCapability(Capabilities.METAL_STACK_PROVIDER, null);
        if (consumableMetalProvider == null || metalProvider == null)
            return stack;
        List<MetalStack> metals = consumableMetalProvider.get();
        List<MetalStack> consumed = new ArrayList<>();
        for (MetalStack metal : metals)
        {
            float consumedMetal = consume(metal, metal.getQuantity(), true);
            consumed.add(metal.copy(consumedMetal, metal.getPurity()));
        }
        List<MetalStack> fullyConsumed = metalProvider.consume(consumed, simulate);
        for (MetalStack metal : fullyConsumed)
        {
            consume(metal, metal.getQuantity(), simulate);
        }
        return stack;
    }

    @Override
    public float consume(MetalStack stack, float amount, boolean simulate)
    {
        List<MetalStack> current = storage.get(stack.getMetal());
        float stored = current.stream().reduce(0f, (acc, s) -> acc + s.getQuantity(), (a, b) -> a + b);
        float acceptable = Math.min(amount, CAPACITY - stored);
        Optional<MetalStack> existing = current.isEmpty() ? Optional.empty()
                                                          : Optional.of(current.get(current.size() - 1))
                                                                    .filter(s -> s.getPurity() == stack.getPurity());
        if (!simulate && acceptable > 0)
        {
            if (existing.isPresent())
            {
                MetalStack s = existing.get();
                s.setQuantity(s.getQuantity() + acceptable);
                Investiture.net().sendToTracking(allomancer.entity,
                                                 new AllomancerStorageUpdate(allomancer.entity.getEntityId(),
                                                                             AllomancerStorageUpdate.ACTION_UPDATE_LAST, s.copy()));
            }
            else
            {
                MetalStack s = new MetalStack(stack.getMetal(), acceptable, stack.getPurity());
                storage.put(stack.getMetal(), s);
                Investiture.net().sendToTracking(allomancer.entity,
                                                 new AllomancerStorageUpdate(allomancer.entity.getEntityId(),
                                                                             AllomancerStorageUpdate.ACTION_APPEND, s.copy()));
            }
        }
        return acceptable;
    }

    @Override
    public Set<MetalStack> burn(Metal metal, float amount, boolean simulate)
    {
        List<MetalStack> current = storage.get(metal);
        Set<MetalStack> result = new HashSet<>();
        Iterator<MetalStack> iterator = current.iterator();
        while (iterator.hasNext())
        {
            MetalStack stack = iterator.next();
            if (stack.getQuantity() <= amount)
            {
                result.add(new MetalStack(stack.getMetal(), stack.getQuantity(), stack.getPurity()));
                amount -= stack.getQuantity();
                if (!simulate)
                {
                    iterator.remove();
                    Investiture.net().sendToTracking(allomancer.entity,
                                                     new AllomancerStorageUpdate(allomancer.entity.getEntityId(),
                                                                                 AllomancerStorageUpdate.ACTION_REMOVE_LAST, stack.copy()));
                }
            }
            else
            {
                float remaining = stack.getQuantity() - amount;
                result.add(new MetalStack(stack.getMetal(), amount, stack.getPurity()));
                if (!simulate)
                {
                    stack.setQuantity(remaining);
                    Investiture.net().sendToTracking(allomancer.entity,
                                                     new AllomancerStorageUpdate(allomancer.entity.getEntityId(),
                                                                                 AllomancerStorageUpdate.ACTION_UPDATE_LAST, stack.copy()));
                }
                amount = 0;
            }
            if (amount <= 0)
            {
                break;
            }
        }
        return result;
    }

    @Override
    public List<MetalStack> getStored(Metal metal)
    {
        return storage.get(metal);
    }

    @Nonnull
    @Override
    public Set<Metal> getStoredMetals()
    {
        return storage.keySet();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<Metal, Collection<MetalStack>> stacks : storage.asMap().entrySet())
        {
            NBTTagList list = new NBTTagList();
            for (MetalStack stack : stacks.getValue())
            {
                list.appendTag(stack.serializeNBT());
            }
            tag.setTag(stacks.getKey().id(), list);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        storage.clear();
        for (String id : nbt.getKeySet())
        {
            Metal metal = Metals.get(id);
            List<MetalStack> stacks = new ArrayList<>();
            NBTTagList nbtList = nbt.getTagList(id, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < nbtList.tagCount(); i++)
            {
                stacks.add(new MetalStack(nbtList.getCompoundTagAt(i)));
            }
            storage.putAll(metal, stacks);
        }
    }
}
