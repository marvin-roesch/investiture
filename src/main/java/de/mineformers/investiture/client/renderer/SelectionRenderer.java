package de.mineformers.investiture.client.renderer;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.mineformers.investiture.block.ComplexBounds;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectionRenderer
{
    @SubscribeEvent
    public void onRenderSelection(DrawBlockHighlightEvent event)
    {
        if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK)
        {
            EntityPlayer player = event.getPlayer();
            World world = player.world;
            BlockPos pos = event.getTarget().getBlockPos();
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ComplexBounds)
            {
                List<AxisAlignedBB> boxes = ((ComplexBounds) state.getBlock()).getSelectionBoxes(world, pos, state);
                GlStateManager.pushMatrix();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos))
                {
                    AxisAlignedBB bounds = calculateExtreme(boxes);
                    AxisAlignedBB scaledBounds = bounds.grow(0.002);
                    double scaleX = (scaledBounds.maxX - scaledBounds.minX) / (bounds.maxX - bounds.minX);
                    double scaleY = (scaledBounds.maxY - scaledBounds.minY) / (bounds.maxX - bounds.minX);
                    double scaleZ = (scaledBounds.maxZ - scaledBounds.minZ) / (bounds.maxX - bounds.minX);
                    Set<Line> lines = splitBoxLines(boxes);
                    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
                    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
                    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
                    GlStateManager.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
//                    GlStateManager.scale(scaleX, scaleY, scaleZ);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                    for (Line line : lines)
                    {
                        Vec3d start = line.a;
                        Vec3d end = line.b;
                        buffer.pos(start.x, start.y, start.z).color(0, 0, 0, 0.4f).endVertex();
                        buffer.pos(end.x, end.y, end.z).color(0, 0, 0, 0.4f).endVertex();
                    }
                    tessellator.draw();
                }
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                event.setCanceled(true);
            }
        }
    }

    private AxisAlignedBB calculateExtreme(Iterable<AxisAlignedBB> boxes)
    {
        double minX, minY, minZ;
        double maxX, maxY, maxZ;
        minX = minY = minZ = Double.POSITIVE_INFINITY;
        maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
        for (AxisAlignedBB box : boxes)
        {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private Set<Line> splitBoxLines(List<AxisAlignedBB> boxes)
    {
        List<Line> lines = boxes.stream().flatMap(this::getBoxEdges).collect(Collectors.toList());
        List<Line> oldResult = Lists.newArrayList(lines);
        List<Line> result = lines;
        while (!oldResult.equals(result = splitLines(result)))
        {
            oldResult = result;
        }
        return Sets.newHashSet(result);
    }

    private List<Line> splitLines(Iterable<Line> lines)
    {
        List<Line> result = Lists.newArrayList();
        for (Line line : lines)
        {
            result.addAll(line.split(lines));
        }
        return result.stream().filter(l -> !l.a.equals(l.b)).collect(Collectors.toList());
    }

    private Stream<Line> getBoxEdges(AxisAlignedBB box)
    {
        Vec3d p1 = new Vec3d(box.minX, box.minY, box.minZ);
        Vec3d p2 = new Vec3d(box.maxX, box.minY, box.minZ);
        Vec3d p3 = new Vec3d(box.minX, box.maxY, box.minZ);
        Vec3d p4 = new Vec3d(box.minX, box.minY, box.maxZ);
        Vec3d p5 = new Vec3d(box.maxX, box.maxY, box.minZ);
        Vec3d p6 = new Vec3d(box.maxX, box.minY, box.maxZ);
        Vec3d p7 = new Vec3d(box.minX, box.maxY, box.maxZ);
        Vec3d p8 = new Vec3d(box.maxX, box.maxY, box.maxZ);
        ImmutableList.Builder<Line> lines = new ImmutableList.Builder<>();
        lines.add(new Line(p1, p2), new Line(p1, p4), new Line(p2, p6), new Line(p4, p6));
        lines.add(new Line(p3, p5), new Line(p3, p7), new Line(p7, p8), new Line(p5, p8));
        lines.add(new Line(p1, p3), new Line(p4, p7), new Line(p6, p8), new Line(p2, p5));
        return lines.build().stream();
    }

    public static void main(String[] args)
    {
        List<AxisAlignedBB> boxes = ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 0.5, 1), new AxisAlignedBB(0, 0.5, 0.5, 1, 1, 1));
        System.out.println(new SelectionRenderer().splitBoxLines(boxes));
    }

    public static class Line
    {
        public final Vec3d a;
        public final Vec3d b;
        public final Vec3d dir;

        private Line(Vec3d a, Vec3d b)
        {
            double aLength = a.lengthSquared();
            double bLength = b.lengthSquared();
            this.a = aLength <= bLength ? a : b;
            this.b = aLength <= bLength ? b : a;
            this.dir = this.b.subtract(this.a);
        }

        public Set<Line> split(Iterable<Line> lines)
        {
            Set<Line> result = Sets.newHashSet();
            for (Line other : lines)
            {
                if (this == other)
                {
                    continue;
                }
                if (this.equals(other))
                {
                    return Sets.newHashSet();
                }
                Vec3d dirEquivalence = dir.scale(other.dir.dotProduct(dir)).subtract(other.dir.scale(dir.dotProduct(dir)));
                if (Objects.equal(dirEquivalence, Vec3d.ZERO))
                {
                    double tA = calculateParameter(other.a.subtract(a), dir);
                    double tB = calculateParameter(other.b.subtract(a), dir);
                    if (!Double.isNaN(tA) && !Double.isNaN(tB) &&
                        !(tA >= 1 && tB >= 1) && !(tA <= 0 && tB <= 0))
                    {
                        if ((tA <= 0 && tB >= 1) || (tA >= 1 && tB <= 0))
                            return Sets.newHashSet();
                        if (tA <= tB)
                        {
                            result.add(new Line(a, other.a));
                            result.add(new Line(other.b, b));
                        }
                        else
                        {
                            result.add(new Line(other.a, b));
                            result.add(new Line(a, other.b));
                        }
                    }
                }
            }
            if (result.isEmpty())
            {
                result.add(this);
            }
            return result.stream().collect(Collectors.toSet());
        }

        private static double calculateParameter(Vec3d delta, Vec3d dir)
        {
            double t1;
            if (dir.x != 0)
            {
                t1 = delta.x / dir.x;
            }
            else
            {
                t1 = delta.x == 0 ? 0 : Double.NaN;
            }
            double t2;
            if (dir.y != 0)
            {
                t2 = delta.y / dir.y;
            }
            else
            {
                t2 = delta.y == 0 ? 0 : Double.NaN;
            }
            double t3;
            if (dir.z != 0)
            {
                t3 = delta.z / dir.z;
            }
            else
            {
                t3 = delta.z == 0 ? 0 : Double.NaN;
            }
            if (t1 == t2 && t2 == t3)
            {
                return t1;
            }
            if (t1 == 0 && t2 == 0)
            {
                return t3;
            }
            if (t1 == 0 && t3 == 0)
            {
                return t2;
            }
            if (t2 == 0 && t3 == 0)
            {
                return t1;
            }
            if (t1 == 0 && t2 == t3)
            {
                return t2;
            }
            if (t2 == 0 && t1 == t3)
            {
                return t1;
            }
            if (t3 == 0 && t1 == t2)
            {
                return t1;
            }
            return Double.NaN;
        }

        @Override
        public String toString()
        {
            return a + " -> " + b;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Line line = (Line) o;
            return (Objects.equal(a, line.a) && Objects.equal(b, line.b)) || (Objects.equal(a, line.b) && Objects.equal(b, line.a));
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(a, b);
        }
    }
}
