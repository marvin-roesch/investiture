package de.mineformers.investiture.util;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility functions for reflection, including method handles.
 */
public class Reflection
{
    public static FieldHandleBuilder getterHandle(Class<?> clazz)
    {
        return new FieldHandleBuilder(clazz, false);
    }

    public static FieldHandleBuilder setterHandle(Class<?> clazz)
    {
        return new FieldHandleBuilder(clazz, true);
    }

    public static MethodHandleBuilder<?> methodHandle(Class<?> clazz)
    {
        return new MethodHandleBuilder<>(clazz);
    }

    public static class FieldHandleBuilder
    {
        private final Class<?> clazz;
        private String srgName;
        private String mcpName;
        private final boolean setter;

        FieldHandleBuilder(Class<?> clazz, boolean setter)
        {
            this.clazz = clazz;
            this.setter = setter;
        }

        public FieldHandleBuilder srgName(String srgName)
        {
            this.srgName = srgName;
            return this;
        }

        public FieldHandleBuilder mcpName(String mcpName)
        {
            this.mcpName = mcpName;
            return this;
        }

        public MethodHandle build()
        {
            try
            {
                Field field = ReflectionHelper.findField(clazz, srgName, mcpName);
                field.setAccessible(true);
                if (setter)
                    return MethodHandles.lookup().unreflectSetter(field);
                else
                    return MethodHandles.lookup().unreflectGetter(field);
            }
            catch (ReflectiveOperationException e)
            {
                Throwables.propagate(e);
                return null;
            }
        }
    }

    public static class MethodHandleBuilder<T>
    {
        private final Class<T> clazz;
        private String srgName;
        private String mcpName;
        private final List<Class<?>> types = new ArrayList<>();

        MethodHandleBuilder(Class<T> clazz)
        {
            this.clazz = clazz;
        }

        public MethodHandleBuilder srgName(String srgName)
        {
            this.srgName = srgName;
            return this;
        }

        public MethodHandleBuilder mcpName(String mcpName)
        {
            this.mcpName = mcpName;
            return this;
        }

        public <C> MethodHandleBuilder type(Class<C> type)
        {
            types.add(type);
            return this;
        }

        public MethodHandle build()
        {
            try
            {
                Method method = ReflectionHelper.findMethod(clazz, null, new String[]{srgName, mcpName}, types.toArray(new Class<?>[types.size()]));
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            }
            catch (ReflectiveOperationException e)
            {
                Throwables.propagate(e);
                return null;
            }
        }
    }
}
