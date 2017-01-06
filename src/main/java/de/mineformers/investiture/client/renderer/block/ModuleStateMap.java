package de.mineformers.investiture.client.renderer.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A state mapper that is specific to a given Investiture module.
 * This is a required "hack" to give modules their own resource domain without being a mod on their own.
 */
public class ModuleStateMap extends StateMapperBase
{
    private final String domain;
    private final IProperty name;
    private final String suffix;
    private final List<IProperty<?>> ignored;

    private ModuleStateMap(@Nullable String domain, @Nullable IProperty<?> name, @Nullable String suffix, List<IProperty<?>> ignored)
    {
        this.domain = domain;
        this.name = name;
        this.suffix = suffix;
        this.ignored = ignored;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public ModelResourceLocation getModelResourceLocation(IBlockState state)
    {
        Map<IProperty<?>, Comparable<?>> map = Maps.newLinkedHashMap(state.getProperties());
        String name;
        String domain;
        if (this.domain == null)
            domain = Block.REGISTRY.getNameForObject(state.getBlock()).getResourceDomain();
        else
            domain = this.domain;

        if (this.name == null)
        {
            name = String.format("%s:%s", domain, Block.REGISTRY.getNameForObject(state.getBlock()).getResourcePath());
        }
        else
        {
            name = String.format("%s:%s", domain, this.name.getName(map.remove(this.name)));
        }

        if (this.suffix != null)
        {
            name = name + this.suffix;
        }

        this.ignored.forEach(map::remove);

        return new ModelResourceLocation(name, this.getPropertyString(map));
    }

    @SideOnly(Side.CLIENT)
    public static class Builder
    {
        private String domain;
        private IProperty<?> name;
        private String suffix;
        private final List<IProperty<?>> ignored = Lists.<IProperty<?>>newArrayList();

        public Builder withDomain(String domain)
        {
            this.domain = domain;
            return this;
        }

        public Builder withName(IProperty<?> builderPropertyIn)
        {
            this.name = builderPropertyIn;
            return this;
        }

        public Builder withSuffix(String builderSuffixIn)
        {
            this.suffix = builderSuffixIn;
            return this;
        }

        public Builder ignore(IProperty<?>... properties)
        {
            Collections.addAll(this.ignored, properties);
            return this;
        }

        public ModuleStateMap build()
        {
            return new ModuleStateMap(this.domain, this.name, this.suffix, this.ignored);
        }
    }
}
