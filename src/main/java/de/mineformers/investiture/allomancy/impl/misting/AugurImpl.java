package de.mineformers.investiture.allomancy.impl.misting;

import de.mineformers.investiture.allomancy.api.misting.Augur;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.serialisation.Serialise;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

/**
 * ${JDOC}
 */
public class AugurImpl extends AbstractMisting implements Augur
{
    @Serialise
    private Vec3 position;

    @Override
    public Optional<Vec3> lastDeathPosition()
    {
        return Optional.ofNullable(position);
    }

    public static class EventHandler
    {
        @SubscribeEvent
        public void onDeath(LivingDeathEvent event)
        {
            if(event.entity.worldObj.isRemote)
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(event.entity).flatMap(a -> a.as(Augur.class)).ifPresent(a -> {
                if(a instanceof AugurImpl)
                    ((AugurImpl) a).position = event.entity.getPositionVector();
            });
        }
    }
}
