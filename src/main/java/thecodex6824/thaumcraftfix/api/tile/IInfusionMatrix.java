package thecodex6824.thaumcraftfix.api.tile;

import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.AspectList;

public interface IInfusionMatrix {

    public boolean isCrafting();
    
    public AspectList essentiaRemaining();
    
    public TileEntity asTileEntity();
    
}
