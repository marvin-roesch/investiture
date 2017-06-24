package de.mineformers.investiture.block;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.tileentity.Crusher;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nullable;

public class CrusherBlock extends Block
{
    public CrusherBlock()
    {
        super(Material.IRON);
        setRegistryName(Investiture.MOD_ID, "crusher");
        setUnlocalizedName(Investiture.MOD_ID + ".crusher");
        setHardness(5);
        setResistance(10);
        setSoundType(SoundType.METAL);
        setCreativeTab(Investiture.CREATIVE_TAB);
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param)
    {
        if (!world.isRemote)
        {
            return id == 0;
        }
        if (id == 0)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof Crusher)
            {
                ((Crusher) te).triggerAnimation();
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new Crusher();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this,
                                      new IProperty[]{Properties.StaticProperty},
                                      new IUnlistedProperty[]{Properties.AnimationProperty});
    }
}
