package de.mineformers.investiture.allomancy.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.network.MistingUpdate;
import de.mineformers.investiture.serialisation.Serialisation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ${JDOC}
 */
public class AllomancerCompanion
{
    private final Class<?> type;
    private final String baseType;
    private final Table<Misting, String, Optional<?>> oldValues = HashBasedTable.create();

    public AllomancerCompanion(Class<?> type, Class<? extends Misting> baseType)
    {
        this.type = type;
        this.baseType = baseType.getName();
    }

    public MistingUpdate allFields(Misting instance, Entity entity)
    {
        Set<Serialisation.FieldData> fields = Serialisation.INSTANCE.getNetFields(type, true);
        ByteBuf buffer = Unpooled.buffer();
        Serialisation.INSTANCE.serialiseFieldsFrom(instance, fields, buffer);
        return new MistingUpdate(entity.getEntityId(), baseType, buffer.array());
    }

    public void sendToAll(Misting instance, Entity entity)
    {
        Investiture.net().sendToTracking(entity, allFields(instance, entity));
    }

    public void sendTo(EntityPlayer target, Misting instance, Entity entity)
    {
        if (!target.world.isRemote)
            Investiture.net().sendTo((EntityPlayerMP) target, allFields(instance, entity));
    }

    public void write(Misting instance, Entity entity)
    {
        if (entity.world.isRemote)
            return;
        Set<Serialisation.FieldData> fields =
            Serialisation.INSTANCE.getNetFields(type, true)
                                  .stream()
                                  .filter(f ->
                                          {
                                              Object value = f.get(instance);
                                              Optional<?> oldValue = oldValues.contains(instance, f.name) ? oldValues.get(instance, f.name)
                                                                                                          : null;
                                              return oldValue == null || !AllomancyAPIImpl.INSTANCE.equals(value, oldValue.orElse(null));
                                          })
                                  .collect(Collectors.toSet());
        if (fields.isEmpty())
            return;
        ByteBuf buffer = Unpooled.buffer();
        Serialisation.INSTANCE.serialiseFieldsFrom(instance, fields, buffer);
        Investiture.net().sendToTracking(entity, new MistingUpdate(entity.getEntityId(), baseType, buffer.array()));
        for (Serialisation.FieldData field : fields)
            oldValues.put(instance, field.name, Optional.ofNullable(field.get(instance)));
    }

    public void read(Misting instance, byte[] data)
    {
        Serialisation.INSTANCE.deserialiseFieldsTo(Unpooled.copiedBuffer(data), instance);
    }
}
