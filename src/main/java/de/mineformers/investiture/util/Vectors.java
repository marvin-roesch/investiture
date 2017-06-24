package de.mineformers.investiture.util;

import com.google.common.collect.Range;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector4d;

public class Vectors
{
    public static Vec3d getMin(AxisAlignedBB box)
    {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }

    public static Vec3d getMax(AxisAlignedBB box)
    {
        return new Vec3d(box.maxX, box.maxY, box.maxZ);
    }

    public static AxisAlignedBB rotateBlockBoundsX(AxisAlignedBB box, Rotation rotation)
    {
        return rotateBlockBounds(box, new Vec3d(1, 0, 0), rotation);
    }

    public static AxisAlignedBB rotateBlockBoundsY(AxisAlignedBB box, Rotation rotation)
    {
        return rotateBlockBounds(box, new Vec3d(0, 1, 0), rotation);
    }

    public static AxisAlignedBB rotateBlockBoundsZ(AxisAlignedBB box, Rotation rotation)
    {
        return rotateBlockBounds(box, new Vec3d(0, 0, 1), rotation);
    }

    public static AxisAlignedBB rotateBlockBounds(AxisAlignedBB box, Vec3d axis, Rotation rotation)
    {
        return rotate(box.offset(-.5, -.5, -.5), axis, rotation).offset(.5, .5, .5);
    }

    public static AxisAlignedBB rotateX(AxisAlignedBB box, Rotation rotation)
    {
        return rotate(box, new Vec3d(1, 0, 0), rotation);
    }

    public static AxisAlignedBB rotateY(AxisAlignedBB box, Rotation rotation)
    {
        return rotate(box, new Vec3d(0, 1, 0), rotation);
    }

    public static AxisAlignedBB rotateZ(AxisAlignedBB box, Rotation rotation)
    {
        return rotate(box, new Vec3d(0, 0, 1), rotation);
    }

    public static AxisAlignedBB rotate(AxisAlignedBB box, Vec3d axis, Rotation rotation)
    {
        double angle = toAngle(rotation);
        return new AxisAlignedBB(rotate(getMin(box), axis, angle),
                                 rotate(getMax(box), axis, angle));
    }

    public static boolean contains(AxisAlignedBB box, Vec3d point)
    {
        return Range.closed(box.minX, box.maxX).contains(point.x) &&
            Range.closed(box.minY, box.maxY).contains(point.y) &&
            Range.closed(box.minZ, box.maxZ).contains(point.z);
    }

    public static Vec3d rotateX(Vec3d vec, Rotation rotation)
    {
        return rotate(vec, new Vec3d(1, 0, 0), rotation);
    }

    public static Vec3d rotateY(Vec3d vec, Rotation rotation)
    {
        return rotate(vec, new Vec3d(0, 1, 0), rotation);
    }

    public static Vec3d rotateZ(Vec3d vec, Rotation rotation)
    {
        return rotate(vec, new Vec3d(0, 0, 1), rotation);
    }

    public static Vec3d rotate(Vec3d vec, Vec3d axis, Rotation rotation)
    {
        return rotate(vec, axis, toAngle(rotation));
    }

    public static Vec3d rotate(Vec3d vec, Vec3d axis, Double angle)
    {
        Matrix4d matrix = new Matrix4d();
        matrix.setIdentity();
        matrix.setRotation(new AxisAngle4d(axis.x, axis.y, axis.z, angle));
        Vector4d wrapped = new Vector4d(vec.x, vec.y, vec.z, 1);
        matrix.transform(wrapped);
        return new Vec3d(wrapped.x, wrapped.y, wrapped.z);
    }

    public static double toAngle(Rotation rotation)
    {
        switch (rotation)
        {
            case COUNTERCLOCKWISE_90:
                return Math.PI / 2;
            case CLOCKWISE_90:
                return -Math.PI / 2;
            case CLOCKWISE_180:
                return -Math.PI;
            default:
                return 0;
        }
    }

    public static Rotation getRotation(EnumFacing facing)
    {
        return getRotation(facing, EnumFacing.NORTH);
    }

    public static Rotation getRotation(EnumFacing facing, EnumFacing relativeTo)
    {
        if (facing == relativeTo || facing.getAxis() == EnumFacing.Axis.Y || relativeTo.getAxis() == EnumFacing.Axis.Y)
            return Rotation.NONE;
        if (facing == relativeTo.getOpposite())
        {
            return Rotation.CLOCKWISE_180;
        }
        if (facing == relativeTo.rotateY())
        {
            return Rotation.CLOCKWISE_90;
        }
        return Rotation.COUNTERCLOCKWISE_90;
    }
}
