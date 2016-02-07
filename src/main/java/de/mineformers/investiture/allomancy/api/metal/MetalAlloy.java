package de.mineformers.investiture.allomancy.api.metal;

import de.mineformers.investiture.allomancy.api.misting.Misting;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface MetalAlloy extends Metal
{

    Map<Metal, Float> getComponents();

    abstract class AbstractAlloy extends AbstractMetal implements MetalAlloy
    {
        private final Map<Metal, Float> components = new HashMap<>();

        public AbstractAlloy(@Nonnull String id, Class<? extends Misting> mistingType, Object... components)
        {
            super(id, mistingType);

            if (components.length % 2 != 0)
            {
                throw new RuntimeException("Invalid alloy components provided");
            }

            float totalPer = 0;

            for (int i = 0; i < (components.length / 2); i++)
            {
                Object metalObj = components[i * 2];
                Object perObj = components[i * 2 + 1];

                if (!(metalObj instanceof Metal) || !(perObj instanceof Float))
                {
                    throw new RuntimeException("Invalid alloy components provided");
                }

                Metal metal = (Metal) metalObj;
                Float per = (Float) perObj;

                this.components.put(metal, per);
                totalPer += per;
            }

            if (totalPer != 1f)
            {
                throw new RuntimeException("Alloy percentages do not add up to 100. Do you not understand what percentages are?");
            }
        }

        public Map<Metal, Float> getComponents()
        {
            return Collections.unmodifiableMap(this.components);
        }
    }
}
