package de.mineformers.investiture.serialisation;

import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;

/**
 * ${JDOC}
 */
public interface SerialisationCompanion
{
    void write(Misting instance, Entity entity);

    void read(Misting instance, byte[] data);
}
