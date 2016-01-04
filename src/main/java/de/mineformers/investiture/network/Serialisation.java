package de.mineformers.investiture.network;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Internal handler for all networking serialisation needs.
 * <p>
 * WARNING: This class is an implementation detail and not supposed to be called by anything directly but {@link Message}, so binary incompatible
 * API changes can occur at random.
 */
class Serialisation
{
    static final Serialisation INSTANCE = new Serialisation();
    private Map<Class<?>, Message.Translator<?>> translators = new HashMap<>();
    private Map<String, Field[]> fields = new WeakHashMap<>();
    private Table<String, String, Message.Translator<?>> fieldTranslators = HashBasedTable.create();

    /**
     * Constructor that registers default serialisation.
     * <p>
     * It's private because there should only ever be one {@link Serialisation#INSTANCE instance} of this class.
     */
    private Serialisation()
    {
        // String translator
        addTranslator(String.class, new Message.Translator<String>()
        {
            @Override
            public void serialiseImpl(String value, ByteBuf buffer)
            {
                ByteBufUtils.writeUTF8String(buffer, value);
            }

            @Override
            public String deserialiseImpl(ByteBuf buffer)
            {
                return ByteBufUtils.readUTF8String(buffer);
            }
        });

        // int translator
        addTranslator(Integer.TYPE, new Message.Translator<Integer>()
        {
            @Override
            public void serialiseImpl(Integer value, ByteBuf buffer)
            {
                buffer.writeInt(value);
            }

            @Override
            public Integer deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readInt();
            }
        });

        // byte translator
        addTranslator(Byte.TYPE, new Message.Translator<Byte>()
        {
            @Override
            public void serialiseImpl(Byte value, ByteBuf buffer)
            {
                buffer.writeByte(value);
            }

            @Override
            public Byte deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readByte();
            }
        });

        // short translator
        addTranslator(Short.TYPE, new Message.Translator<Short>()
        {
            @Override
            public void serialiseImpl(
                Short value, ByteBuf buffer)
            {
                buffer.writeShort(value);
            }

            @Override
            public Short deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readShort();
            }
        });

        // long translator
        addTranslator(Long.TYPE, new Message.Translator<Long>()
        {
            @Override
            public void serialiseImpl(Long value, ByteBuf buffer)
            {
                buffer.writeLong(value);
            }

            @Override
            public Long deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readLong();
            }
        });

        // char translator
        addTranslator(Character.TYPE, new Message.Translator<Character>()
        {
            @Override
            public void serialiseImpl(Character value, ByteBuf buffer)
            {
                buffer.writeChar(value);
            }

            @Override
            public Character deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readChar();
            }
        });

        // boolean translator
        addTranslator(Boolean.TYPE, new Message.Translator<Boolean>()
        {
            @Override
            public void serialiseImpl(Boolean value, ByteBuf buffer)
            {
                buffer.writeBoolean(value);
            }

            @Override
            public Boolean deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readBoolean();
            }
        });

        // float translator
        addTranslator(Float.TYPE, new Message.Translator<Float>()
        {
            @Override
            public void serialiseImpl(Float value, ByteBuf buffer)
            {
                buffer.writeFloat(value);
            }

            @Override
            public Float deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readFloat();
            }
        });

        // double translator
        addTranslator(Double.TYPE, new Message.Translator<Double>()
        {
            @Override
            public void serialiseImpl(Double value, ByteBuf buffer)
            {
                buffer.writeDouble(value);
            }

            @Override
            public Double deserialiseImpl(ByteBuf buffer)
            {
                return buffer.readDouble();
            }
        });

        // ItemStack translator
        addTranslator(ItemStack.class, new Message.Translator<ItemStack>()
        {
            @Override
            public void serialiseImpl(ItemStack value, ByteBuf buffer)
            {
                ByteBufUtils.writeItemStack(buffer, value);
            }

            @Override
            public ItemStack deserialiseImpl(ByteBuf buffer)
            {
                return ByteBufUtils.readItemStack(buffer);
            }
        });

        // NBT compound translator
        addTranslator(NBTTagCompound.class, new Message.Translator<NBTTagCompound>()
        {
            @Override
            public void serialiseImpl(NBTTagCompound value, ByteBuf buffer)
            {
                ByteBufUtils.writeTag(buffer, value);
            }

            @Override
            public NBTTagCompound deserialiseImpl(ByteBuf buffer)
            {
                return ByteBufUtils.readTag(buffer);
            }
        });

        // Direction translator
        addTranslator(EnumFacing.class, new Message.Translator<EnumFacing>()
        {
            @Override
            public void serialiseImpl(EnumFacing value, ByteBuf buffer)
            {
                buffer.writeInt(value.ordinal());
            }

            @Override
            public EnumFacing deserialiseImpl(ByteBuf buffer)
            {
                return EnumFacing.values()[buffer.readInt()];
            }
        });
    }

    /**
     * Internal method for adding a translator to the framework.
     *
     * @param type       the type the translator supports
     * @param translator the translator
     */
    void addTranslator(Class<?> type, Message.Translator<?> translator)
    {
        translators.put(type, translator);
    }

    /**
     * Finds a translator for a given type. If there is no direct translation available, the first translator that can handle a super class of the
     * type will be used.
     *
     * @param type the type to find a translator for
     * @return a translator for the given type, either one that directly supports the type or one for a super type
     */
    private Message.Translator<?> findTranslator(Class<?> type)
    {
        // Direct translation available, short circuit
        if (translators.containsKey(type))
            return translators.get(type);

        // No direct translation available, we have to find one that fits the type nonetheless
        Optional<Message.Translator<?>> fit = FluentIterable.from(translators.entrySet())
                                                            .firstMatch(e -> e.getKey().isAssignableFrom(type))
                                                            .transform(Map.Entry::getValue);
        if (fit.isPresent())
            return fit.get();
        else
            // There doesn't seem to be a translator for this type, we can't handle this particular situation gracefully
            throw new RuntimeException("There is no translator for type " + type.getName() + ", consider writing one.");
    }

    /**
     * Registers a message to the serialisation framework. Allows faster serialisation due to caching of the results of intensive reflective
     * operations.
     *
     * @param type the class representing the type of the message
     * @param <T>  the type of the message
     */
    <T extends Message> void registerMessage(Class<T> type)
    {
        Field[] fs = type.getDeclaredFields();
        // Sort fields by name to prevent disparities between client and server
        Arrays.sort(fs, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        // Cache fields for the given type, looking them up reflectively is costly
        fields.put(type.getName(), fs);
        for (Field f : fs)
        {
            // Cache the translator for each field, prevents disparities between different points in time
            f.setAccessible(true);
            fieldTranslators.put(type.getName(), f.getName(), findTranslator(f.getType()));
        }
    }

    /**
     * Serialises each field of a message to a byte buffer, utilising translators that fit each field's type best.
     *
     * @param message the message to serialise
     * @param buffer the buffer to serialise the message into
     */
    void serialiseFrom(Message message, ByteBuf buffer)
    {
        String className = message.getClass().getName();
        Field[] fields = this.fields.get(className);
        for (Field f : fields)
        {
            Message.Translator<?> translator = fieldTranslators.get(className, f.getName());
            // Fields might be private
            f.setAccessible(true);
            try
            {
                translator.serialise(f.get(message), buffer);
            }
            catch (IllegalAccessException e)
            {
                // Should never happen
                e.printStackTrace();
            }
        }
    }

    /**
     * Deserialises the contents of a byte buffer into a message, writing each field utilising translators.
     *
     * @param buffer the buffer to deserialise from
     * @param message the message to deserialise into
     */
    void deserialiseTo(ByteBuf buffer, Message message)
    {
        String className = message.getClass().getName();
        Field[] fields = this.fields.get(className);
        for (Field f : fields)
        {
            Message.Translator<?> translator = fieldTranslators.get(className, f.getName());
            // Fields might be private
            f.setAccessible(true);
            try
            {
                f.set(message, translator.deserialise(buffer));
            }
            catch (IllegalAccessException e)
            {
                // Should never happen
                e.printStackTrace();
            }
        }
    }
}
