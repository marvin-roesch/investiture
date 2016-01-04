package de.mineformers.investiture.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * A state mapper that is specific to a given Investiture module.
 * This is a required "hack" to give modules their own resource domain without being a mod on their own.
 */
public class ModuleStateMapper extends StateMapperBase
{
    private final String domain;

    public ModuleStateMapper(String domain)
    {
        this.domain = domain;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state)
    {
        return new ModelResourceLocation(domain + ":" + Block.blockRegistry.getNameForObject(state.getBlock()).getResourcePath(),
                                         this.getPropertyString(state.getProperties()));
    }
}
