package de.mineformers.investiture.util;

/**
 * Provides utilities for dealing with Minecraft attributes.
 */
public class Attributes
{
    /**
     * Adds an absolute number to an attribute's value.
     */
    public static final int OP_ADD = 0;
    /**
     * Adds a percentage of the current value to an attribute's value.
     */
    public static final int OP_ADD_PERCENTAGE = 1;
    /**
     * Multiplies the attribute's value with a percentage.
     * Note that the percentage will be coerced into being larger than 100%.
     */
    public static final int OP_PERCENTAGE = 2;
}
