package de.mineformers.investiture.allomancy.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.network.EntityAllomancerUpdate;
import de.mineformers.investiture.serialisation.Serialisation;
import de.mineformers.investiture.serialisation.SerialisationCompanion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ${JDOC}
 */
public class EntityAllomancerCompanion implements SerialisationCompanion
{
    private final Class<?> type;
    private final String baseType;
    private final Table<Misting, String, Optional<?>> oldValues = HashBasedTable.create();

    public EntityAllomancerCompanion(Class<?> type, Class<? extends Misting> baseType)
    {
        this.type = type;
        this.baseType = baseType.getName();
    }

    @Override
    public void write(Misting instance, Entity entity)
    {
        if (entity.worldObj.isRemote)
            return;
        Set<Serialisation.FieldData> fields = Serialisation.INSTANCE.getNetFields(type, true)
                                                                    .stream()
                                                                    .filter(f -> {
                                                                        Object value = f.get(instance);
                                                                        Optional<?> oldValue =
                                                                            oldValues.contains(instance, f.name) ? oldValues.get(instance, f.name)
                                                                                                                 : null;
                                                                        return oldValue == null || !Objects.equals(value, oldValue.orElse(null));
                                                                    })
                                                                    .collect(Collectors.toSet());
        if (fields.isEmpty())
            return;
        ByteBuf buffer = Unpooled.buffer();
        Serialisation.INSTANCE.serialiseFieldsFrom(instance, fields, buffer);
        Investiture.net()
                   .sendToWatching(entity.worldObj, entity.getPosition(), new EntityAllomancerUpdate(entity.getEntityId(), baseType, buffer.array()));
        for (Serialisation.FieldData field : fields)
            oldValues.put(instance, field.name, Optional.ofNullable(field.get(instance)));
    }

    @Override
    public void read(Misting instance, byte[] data)
    {
        Serialisation.INSTANCE.deserialiseFieldsTo(Unpooled.copiedBuffer(data), instance);
    }
}
