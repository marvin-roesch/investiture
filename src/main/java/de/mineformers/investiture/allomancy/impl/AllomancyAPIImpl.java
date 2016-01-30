package de.mineformers.investiture.allomancy.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.AllomancyAPI;
import de.mineformers.investiture.allomancy.api.MistingFactory;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.misting.Smoker;
import de.mineformers.investiture.serialisation.Serialisation;
import de.mineformers.investiture.serialisation.SerialisationCompanion;
import de.mineformers.investiture.serialisation.Serialise;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
@ParametersAreNonnullByDefault
public class AllomancyAPIImpl implements AllomancyAPI
{
    public static final AllomancyAPIImpl INSTANCE = new AllomancyAPIImpl();
    private static final List<Class<?>> INJECTABLE_TYPES = ImmutableList.of(Allomancer.class, Entity.class);

    private Map<Class<? extends Misting>, MistingData> factories = new HashMap<>();

    private AllomancyAPIImpl()
    {
    }

    public void init()
    {
        registerMisting(Smoker.class, () -> new Smoker()
        {
            @Inject
            Entity entity;
            @Serialise
            public String category = "";

            @Override
            public void setCategory(String test)
            {
                this.category = test;
            }

            @Override
            public String category()
            {
                return category;
            }
        });
    }

    @Nonnull
    @Override
    public Optional<Allomancer> toAllomancer(Entity entity)
    {
        return Optional.ofNullable(entity.getCapability(CapabilityHandler.ALLOMANCER_CAPABILITY, null));
    }

    @Override
    public <T extends Misting> void registerMisting(Class<T> type, MistingFactory<? extends T> factory)
    {
        factories.put(type, new MistingData(type, factory));
    }

    @SuppressWarnings("unchecked")
    public <T extends Misting> T instantiate(Class<T> type, Allomancer allomancer, Entity entity)
    {
        MistingData data = factories.get(type);
        if (data == null)
            return null;
        T result = (T) data.factory.create();
        data.inject(result, ImmutableMap.of(Allomancer.class, allomancer,
                                            Entity.class, entity));
        return result;
    }

    public void update(Allomancer allomancer, Entity entity)
    {
        for (Class<? extends Misting> type : allomancer.powers())
        {
            allomancer.as(type).ifPresent(m -> factories.get(type).companion.write(m, entity));
        }
    }

    public void read(Entity entity, Class<? extends Misting> type, byte[] data)
    {
        toAllomancer(entity).map(a -> a.grantPower(type)).ifPresent(m ->  factories.get(type).companion.read(m, data));
    }

    private static class MistingData
    {
        public final Class<? extends Misting> type;
        public final MistingFactory<?> factory;
        public final Collection<Field> injectedFields;
        public final SerialisationCompanion companion;

        public MistingData(Class<? extends Misting> baseType, MistingFactory<?> factory)
        {
            this.factory = factory;
            type = factory.referenceClass();
            injectedFields = StreamSupport
                .stream(ClassUtils.hierarchy(type, ClassUtils.Interfaces.INCLUDE).spliterator(), false)
                .map(Class::getDeclaredFields)
                .flatMap(Arrays::stream)
                .filter(f -> f.getAnnotation(Inject.class) != null &&
                    INJECTABLE_TYPES.stream().anyMatch(t -> t.isAssignableFrom(f.getType())))
                .collect(Collectors.toList());
            injectedFields.forEach(f -> f.setAccessible(true));
            this.companion = new EntityAllomancerCompanion(type, baseType);
            Serialisation.INSTANCE.registerClass(type, true);
        }

        public void inject(Misting instance, Map<Class<?>, Object> values)
        {
            for (Field f : injectedFields)
            {
                values.keySet()
                      .stream()
                      .filter(e -> e.isAssignableFrom(f.getType()))
                      .findFirst()
                      .ifPresent(t -> {
                          try
                          {
                              f.set(instance, values.get(t));
                          }
                          catch (IllegalAccessException e)
                          {
                              Throwables.propagate(e);
                          }
                      });
            }
        }
    }
}
