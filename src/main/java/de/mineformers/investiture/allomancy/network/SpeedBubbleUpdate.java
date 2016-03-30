package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.allomancy.impl.misting.temporal.SpeedBubble;
import de.mineformers.investiture.network.Message;
import net.minecraft.util.math.BlockPos;

/**
 * Updates a metal extractor tile entity
 */
public class SpeedBubbleUpdate extends Message
{
    public static final int ACTION_ADD = 0;
    public static final int ACTION_REMOVE = 1;

    public int action;
    public int dimension;
    public BlockPos position;
    public double radius;

    public SpeedBubbleUpdate()
    {
    }

    public SpeedBubbleUpdate(int action, SpeedBubble bubble)
    {
        this.action = action;
        this.dimension = bubble.dimension;
        this.position = bubble.position;
        this.radius = bubble.radius;
    }
}
