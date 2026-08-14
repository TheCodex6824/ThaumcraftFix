package thecodex6824.thaumcraftfix.api.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;
import thecodex6824.thaumcraftfix.api.tile.IInfusionMatrix;

public class InfusionRemainingStackEvent extends Event {

    private IInfusionMatrix matrix;
    private ItemStack input;
    private ItemStack remaining;
    
    public InfusionRemainingStackEvent(IInfusionMatrix tile, ItemStack inputItem, ItemStack originalResult) {
	matrix = tile;
	input = inputItem.copy(); 
	remaining = originalResult.copy();
    }
    
    public IInfusionMatrix infusionMatrix() {
	return matrix;
    }
    
    public ItemStack getRemainingStack() {
	return remaining;
    }
    
    public void setRemainingStack(ItemStack newItem) {
	remaining = newItem.copy();
    }
    
    public ItemStack getInputStack() {
	return input;
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
    
    @Override
    public boolean hasResult() {
        return false;
    }
    
}
