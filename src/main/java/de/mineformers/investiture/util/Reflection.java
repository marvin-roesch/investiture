package de.mineformers.investiture.util;

import com.google.common.base.Throwables;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities for reflection, including method handles.
 */
public class Reflection
{
    private static final MethodHandle MODIFIERS_SETTER;

    static
    {
        // It's a hack, but it's required for setting some final fields.
        MODIFIERS_SETTER = setterHandle(Field.class).mcpName("modifiers").build();
    }

    /**
     * Creates a builder for getting a field reflectively.
     *
     * @param clazz the class the field belongs to
     * @return a builder for the field's getter
     */
    public static FieldHandleBuilder getterHandle(Class<?> clazz)
    {
        return new FieldHandleBuilder(clazz, false);
    }

    /**
     * Creates a builder for setting a field reflectively.
     *
     * @param clazz the class the field belongs to
     * @return a builder for the field's setter
     */
    public static FieldHandleBuilder setterHandle(Class<?> clazz)
    {
        return new FieldHandleBuilder(clazz, true);
    }

    /**
     * Creates a builder for a reflective method handle.
     *
     * @param clazz the class the method belongs to
     * @return a builder for the method
     */
    public static MethodHandleBuilder<?> methodHandle(Class<?> clazz)
    {
        return new MethodHandleBuilder<>(clazz);
    }

    /**
     * Sets a final (private) field to a given value.
     * Only do this when performance is not very important (i.e. during startup)!
     *
     * @param clazz    the class the field belongs to
     * @param instance the instance the field should be set in, may be null for static fields
     * @param mcpName  the MCP (human-readable) name of the field
     * @param srgName  the SRG (obfuscated) name of the field
     * @param value    the value the field should be set to
     */
    public static void setFinalField(Class<?> clazz, Object instance, String mcpName, String srgName, Object value)
    {
        try
        {
            Field field = ReflectionHelper.findField(clazz, srgName, mcpName);
            field.setAccessible(true);
            MODIFIERS_SETTER.bindTo(field).invokeExact(field.getModifiers() & ~Modifier.FINAL);
            field.set(instance, value);
        }
        catch (Throwable t)
        {
            Throwables.propagate(t);
        }
    }

    /**
     * Builds a method handle pointing to a (private) field's 'getter' or 'setter'.
     * Provides a fluent interface and support for obfuscation to ease the creation of the method handle.
     */
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

        /**
         * Set the field's SRG (obfuscated) name.
         *
         * @param srgName the field's SRG name
         * @return this builder
         */
        public FieldHandleBuilder srgName(String srgName)
        {
            this.srgName = srgName;
            return this;
        }

        /**
         * Set the field's MCP (human-readable) name.
         *
         * @param mcpName the field's MCP name
         * @return this builder
         */
        public FieldHandleBuilder mcpName(String mcpName)
        {
            this.mcpName = mcpName;
            return this;
        }

        /**
         * Finalises the method handle created by this builder.
         *
         * @return the finished method handle
         */
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

    /**
     * Builds a method handle pointing to a (private) method.
     * Provides a fluent interface and support for obfuscation to ease the creation of the method handle.
     */
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

        /**
         * Set the method's SRG (obfuscated) name.
         *
         * @param srgName the method's SRG name
         * @return this builder
         */
        public MethodHandleBuilder<T> srgName(String srgName)
        {
            this.srgName = srgName;
            return this;
        }

        /**
         * Set the method's MCP (human-readable) name.
         *
         * @param mcpName the method's MCP name
         * @return this builder
         */
        public MethodHandleBuilder<T> mcpName(String mcpName)
        {
            this.mcpName = mcpName;
            return this;
        }

        /**
         * Add the type of one of the method's parameters to the method handle's specification.
         * Note that the order this method is called in for multiple parameters is important, it must match the order the parameters are defined in
         * for the method!
         *
         * @param type the type of the parameter
         * @return this builder
         */
        public <C> MethodHandleBuilder parameterType(Class<C> type)
        {
            types.add(type);
            return this;
        }

        /**
         * Finalises the method handle created by this builder.
         *
         * @return the finished method handle
         */
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
