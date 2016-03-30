package de.mineformers.investiture.serialisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this annotation on a field in a type supported by the serialisation framework and
 * it will be automatically synchronised with clients and stored to NBT.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialise
{
    /**
     * @return true if the annotated field should be saved to NBT, false otherwise
     */
    boolean nbt() default true;

    /**
     * @return true if the annotated field should be synchronised over the network, false otherwise
     */
    boolean net() default true;
}
