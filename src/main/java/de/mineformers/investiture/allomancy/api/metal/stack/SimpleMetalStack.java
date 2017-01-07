package de.mineformers.investiture.allomancy.api.metal.stack;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class SimpleMetalStack implements ModifiableMetalStack, INBTSerializable<NBTTagCompound>
{
    private Metal metal;
    private float quantity;
    private float purity;

    public SimpleMetalStack(Metal metal, float quantity, float purity)
    {
        this.metal = metal;
        this.quantity = quantity;
        this.purity = purity;
    }

    @Override
    public Metal getMetal()
    {
        return metal;
    }

    @Override
    public float getQuantity()
    {
        return quantity;
    }

    @Override
    public float getPurity()
    {
        return purity;
    }

    @Override
    public void setPurity(float purity)
    {
        this.purity = purity;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Metal", metal.id());
        tag.setFloat("Quantity", quantity);
        tag.setFloat("Purity", purity);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.metal = Metals.get(nbt.getString("Metal"));
        this.quantity = nbt.getFloat("Quantity");
        this.purity = nbt.getFloat("Purity");
    }
}
