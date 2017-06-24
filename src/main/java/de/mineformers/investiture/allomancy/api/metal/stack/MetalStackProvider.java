package de.mineformers.investiture.allomancy.api.metal.stack;

import de.mineformers.investiture.allomancy.api.metal.Metal;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public interface MetalStackProvider extends Supplier<List<MetalStack>>
{
    List<MetalStack> consume(List<MetalStack> stacks, boolean simulate);

    @Nonnull
    List<MetalStack> getStored(Metal metal);

    @Nonnull
    Set<Metal> getStoredMetals();
}
