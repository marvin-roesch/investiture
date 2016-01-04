package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;

/**
 * Starts or stops burning a metal for the sending player.
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
