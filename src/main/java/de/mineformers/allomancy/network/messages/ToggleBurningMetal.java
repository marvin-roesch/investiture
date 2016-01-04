package de.mineformers.allomancy.network.messages;

import de.mineformers.allomancy.network.Message;

/**
 * ToggleBurningMetal
 *
 * @author PaleoCrafter
 */
public class ToggleBurningMetal extends Message
{
    public String metal;

    public ToggleBurningMetal()
    {
    }

    public ToggleBurningMetal(String metal)
    {
        this.metal = metal;
    }
}
