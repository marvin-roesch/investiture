package de.mineformers.investiture.allomancy.api.metal.stack;

public interface PurifiableMetalStackProvider extends MetalStackProvider
{
    float lowerPurityBound();

    float upperPurityBound();

    default float middlePurityBound()
    {
        return (lowerPurityBound() + upperPurityBound()) / 2;
    }

    void setPurity(float purity);
}
