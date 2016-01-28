package de.mineformers.investiture.allomancy.impl;

import com.google.common.base.Throwables;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.AllomancyAPI;
import de.mineformers.investiture.allomancy.api.MistingFactory;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.misting.Smoker;
import de.mineformers.investiture.serialisation.Serialisation;
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
        factories.put(type, new MistingData(factory));
    }

    @SuppressWarnings("unchecked")
    public <T extends Misting> T instantiate(Class<T> type, Allomancer allomancer, Entity entity)
    {
        MistingData data = factories.get(type);
        if (data == null)
            return null;
        T result = (T) data.factory.create();
        data.injectedFields.forEach(f -> {
            try
            {
                if (Allomancer.class.isAssignableFrom(f.getType()))
                    f.set(result, allomancer);
                else
                    f.set(result, entity);
            }
            catch (IllegalAccessException e)
            {
                Throwables.propagate(e);
            }
        });
        return result;
    }

    private static class MistingData
    {
        public final MistingFactory<?> factory;
        public final Collection<Field> injectedFields;

        public MistingData(MistingFactory<?> factory)
        {
            this.factory = factory;
            Class<?> clazz = factory.referenceClass();
            injectedFields = StreamSupport
                .stream(ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE).spliterator(), false)
                .map(Class::getDeclaredFields)
                .flatMap(Arrays::stream)
                .filter(f -> f.getAnnotation(Inject.class) != null &&
                    (Allomancer.class.isAssignableFrom(f.getType()) || Entity.class.isAssignableFrom(f.getType())))
                .collect(Collectors.toList());
            injectedFields.forEach(f -> f.setAccessible(true));
            Serialisation.INSTANCE.registerClass(clazz, true);
        }
    }
}
