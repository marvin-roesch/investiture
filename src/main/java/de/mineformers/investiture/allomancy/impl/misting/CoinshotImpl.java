package de.mineformers.investiture.allomancy.impl.misting;

import de.mineformers.investiture.allomancy.api.misting.Coinshot;

/**
 * ${JDOC}
 */
public class CoinshotImpl extends AbstractMetalManipulator implements Coinshot
{
    @Override
    public void update()
    {
        super.update();
//        List<EntityItem> items = entity.worldObj
//            .getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.fromBounds(entity.posX - 10, entity.posY - 10, entity.posZ - 10,
//                                                                              entity.posX + 10, entity.posY + 10, entity.posZ + 10));
//        Vec3 entityPos = entity.getPositionVector();
//        for (EntityItem item : items)
//        {
//            Vec3 itemPos = item.getPositionVector();
//            double distance = itemPos.distanceTo(entityPos);
//            double factor = 1 / distance * 0.1;
//            Vec3 diff = itemPos.subtract(entityPos).normalize();
//            item.addVelocity(diff.xCoord * factor, diff.yCoord * factor, diff.zCoord * factor);
//        }
    }
}
