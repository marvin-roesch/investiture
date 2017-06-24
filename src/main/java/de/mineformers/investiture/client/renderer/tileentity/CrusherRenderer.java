package de.mineformers.investiture.client.renderer.tileentity;

import de.mineformers.investiture.tileentity.Crusher;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.common.animation.Event;

public class CrusherRenderer extends AnimationTESR<Crusher>
{
    @Override
    public void handleEvents(Crusher te, float time, Iterable<Event> pastEvents)
    {
        for (Event event : pastEvents)
        {
            if (event.event().equals("hit"))
            {
                te.getWorld().playSound(te.getPos().getX(), te.getPos().getY() - 1, te.getPos().getZ(),
                                        SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 2f, 0.75f, false);
            }
        }
    }
}
