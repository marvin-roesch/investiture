package de.mineformers.investiture.client.util;

/**
 * ${JDOC}
 */
public class Colour
{
    private final float r, g, b, a;

    public static Colour fromHex(int hex, float alpha)
    {
        float r = ((hex >> 16) & 0xFF) / 255f;
        float g = ((hex >> 8) & 0xFF) / 255f;
        float b = ((hex >> 0) & 0xFF) / 255f;
        return new Colour(r, g, b, alpha);
    }

    public Colour(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public float r()
    {
        return r;
    }

    public float g()
    {
        return g;
    }

    public float b()
    {
        return b;
    }

    public float a()
    {
        return a;
    }
}
