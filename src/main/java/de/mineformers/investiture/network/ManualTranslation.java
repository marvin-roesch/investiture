package de.mineformers.investiture.network;

import io.netty.buffer.ByteBuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for any field which will not be automatically translated in a message.
 * Subtypes of {@link Message} who utilise this should override
 * {@link Message#fromBytes(ByteBuf)} and {@link Message#toBytes(ByteBuf)} to perform manual translation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManualTranslation
{
}
