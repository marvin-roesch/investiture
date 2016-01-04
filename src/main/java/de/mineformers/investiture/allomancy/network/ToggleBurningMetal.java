package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;

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
