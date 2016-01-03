package de.mineformers.allomancy.block.properties;

import net.minecraft.block.properties.PropertyHelper;

import java.util.Arrays;
import java.util.Collection;

/**
 * PropertyString
 *
 * @author PaleoCrafter
 */
public class PropertyString extends PropertyHelper<String> {
    private Collection<String> allowedValues;

    public PropertyString(String name, String... allowedValues) {
        super(name, String.class);
        this.allowedValues = Arrays.asList(allowedValues);
    }

    @Override
    public String getName(String value) {
        return value;
    }

    @Override
    public Collection<String> getAllowedValues() {
        return allowedValues;
    }
}
