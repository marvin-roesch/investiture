package de.mineformers.investiture.block.properties;

import net.minecraft.block.properties.PropertyHelper;

import java.util.Arrays;
import java.util.Collection;

/**
 * Provides a IBlockState compatible property holding strings.
 */
public class PropertyString extends PropertyHelper<String>
{
    private Collection<String> allowedValues;

    /**
     * Creates a new PropertyString.
     *
     * @param name          the name of the property
     * @param allowedValues the values this property is allowed to take
     */
    public PropertyString(String name, String... allowedValues)
    {
        super(name, String.class);
        this.allowedValues = Arrays.asList(allowedValues);
    }

    @Override
    public String getName(String value)
    {
        return value;
    }

    @Override
    public Collection<String> getAllowedValues()
    {
        return allowedValues;
    }
}
