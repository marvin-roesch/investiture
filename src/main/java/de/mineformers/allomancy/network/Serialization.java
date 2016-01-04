package de.mineformers.allomancy.network;

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
 * Serialization
 *
 * @author PaleoCrafter
 */
class Serialization {
    static final Serialization INSTANCE = new Serialization();
    private Map<Class<?>, Message.Translator<?>> translators = new HashMap<>();
    private Map<String, Field[]> fields = new WeakHashMap<>();
    private Table<String, String, Message.Translator<?>> fieldTranslators = HashBasedTable.create();

    private Serialization() {
        addTranslator(String.class, new Message.Translator<String>() {
            @Override
            public void serialiseImpl(String value, ByteBuf buffer) {
                ByteBufUtils.writeUTF8String(buffer, value);
            }

            @Override
            public String deserialiseImpl(ByteBuf buffer) {
                return ByteBufUtils.readUTF8String(buffer);
            }
        });

        addTranslator(Integer.TYPE, new Message.Translator<Integer>() {
            @Override
            public void serialiseImpl(Integer value, ByteBuf buffer) {
                buffer.writeInt(value);
            }

            @Override
            public Integer deserialiseImpl(ByteBuf buffer) {
                return buffer.readInt();
            }
        });

        addTranslator(Byte.TYPE, new Message.Translator<Byte>() {
            @Override
            public void serialiseImpl(Byte value, ByteBuf buffer) {
                buffer.writeByte(value);
            }

            @Override
            public Byte deserialiseImpl(ByteBuf buffer) {
                return buffer.readByte();
            }
        });

        addTranslator(Short.TYPE, new Message.Translator<Short>() {
            @Override
            public void serialiseImpl(
                    Short value, ByteBuf buffer) {
                buffer.writeShort(value);
            }

            @Override
            public Short deserialiseImpl(ByteBuf buffer) {
                return buffer.readShort();
            }
        });

        addTranslator(Long.TYPE, new Message.Translator<Long>() {
            @Override
            public void serialiseImpl(Long value, ByteBuf buffer) {
                buffer.writeLong(value);
            }

            @Override
            public Long deserialiseImpl(ByteBuf buffer) {
                return buffer.readLong();
            }
        });

        addTranslator(Character.TYPE, new Message.Translator<Character>() {
            @Override
            public void serialiseImpl(Character value, ByteBuf buffer) {
                buffer.writeChar(value);
            }

            @Override
            public Character deserialiseImpl(ByteBuf buffer) {
                return buffer.readChar();
            }
        });

        addTranslator(Boolean.TYPE, new Message.Translator<Boolean>() {
            @Override
            public void serialiseImpl(Boolean value, ByteBuf buffer) {
                buffer.writeBoolean(value);
            }

            @Override
            public Boolean deserialiseImpl(ByteBuf buffer) {
                return buffer.readBoolean();
            }
        });

        addTranslator(Float.TYPE, new Message.Translator<Float>() {
            @Override
            public void serialiseImpl(Float value, ByteBuf buffer) {
                buffer.writeFloat(value);
            }

            @Override
            public Float deserialiseImpl(ByteBuf buffer) {
                return buffer.readFloat();
            }
        });

        addTranslator(Double.TYPE, new Message.Translator<Double>() {
            @Override
            public void serialiseImpl(Double value, ByteBuf buffer) {
                buffer.writeDouble(value);
            }

            @Override
            public Double deserialiseImpl(ByteBuf buffer) {
                return buffer.readDouble();
            }
        });

        addTranslator(ItemStack.class, new Message.Translator<ItemStack>() {
            @Override
            public void serialiseImpl(ItemStack value, ByteBuf buffer) {
                ByteBufUtils.writeItemStack(buffer, value);
            }

            @Override
            public ItemStack deserialiseImpl(ByteBuf buffer) {
                return ByteBufUtils.readItemStack(buffer);
            }
        });

        addTranslator(NBTTagCompound.class, new Message.Translator<NBTTagCompound>() {
            @Override
            public void serialiseImpl(NBTTagCompound value, ByteBuf buffer) {
                ByteBufUtils.writeTag(buffer, value);
            }

            @Override
            public NBTTagCompound deserialiseImpl(ByteBuf buffer) {
                return ByteBufUtils.readTag(buffer);
            }
        });

        addTranslator(EnumFacing.class, new Message.Translator<EnumFacing>() {
            @Override
            public void serialiseImpl(EnumFacing value, ByteBuf buffer) {
                buffer.writeInt(value.ordinal());
            }

            @Override
            public EnumFacing deserialiseImpl(ByteBuf buffer) {
                return EnumFacing.values()[buffer.readInt()];
            }
        });
    }

    void addTranslator(Class<?> type, Message.Translator<?> translator) {
        translators.put(type, translator);
    }

    private Message.Translator<?> findTranslator(Class<?> type) {
        if (translators.containsKey(type))
            return translators.get(type);
        Optional<Message.Translator<?>> fit = FluentIterable.from(translators.entrySet())
                .firstMatch(e -> e.getKey().isAssignableFrom(type))
                .transform(Map.Entry::getValue);
        if (fit.isPresent())
            return fit.get();
        else
            throw new RuntimeException("There is no translator for type " + type + " consider writing one.");
    }

    <T extends Message> void registerMessage(Class<T> clazz) {
        Field[] fs = clazz.getDeclaredFields();
        Arrays.sort(fs, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        fields.put(clazz.getName(), fs);
        for (Field f : fs) {
            f.setAccessible(true);
            fieldTranslators.put(clazz.getName(), f.getName(), findTranslator(f.getType()));
        }
    }

    void serializeFrom(Message message, ByteBuf buffer) {
        String className = message.getClass().getName();
        Field[] fields = this.fields.get(className);
        for (Field f : fields) {
            Message.Translator<?> translator = fieldTranslators.get(className, f.getName());
            f.setAccessible(true);
            try {
                translator.serialise(f.get(message), buffer);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    void deserializeTo(ByteBuf buffer, Message message) {
        String className = message.getClass().getName();
        Field[] fields = this.fields.get(className);
        for (Field f : fields) {
            Message.Translator<?> translator = fieldTranslators.get(className, f.getName());
            f.setAccessible(true);
            try {
                f.set(message, translator.deserialise(buffer));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
