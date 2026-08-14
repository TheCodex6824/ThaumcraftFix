package thecodex6824.thaumcraftfix.core.transformer.hooks;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thecodex6824.thaumcraftfix.api.event.InfusionRemainingStackEvent;
import thecodex6824.thaumcraftfix.api.tile.IInfusionMatrix;

public class TileTransformersHooks {

    public static ItemStack getInfusionRemainingItem(IInfusionMatrix matrix, ItemStack originalResult, TileEntity pedestal) {
	ItemStack pedestalItem = ((TilePedestal) pedestal).getStackInSlot(0);
	InfusionRemainingStackEvent event = new InfusionRemainingStackEvent(matrix, pedestalItem, originalResult);
	MinecraftForge.EVENT_BUS.post(event);
	return event.getRemainingStack();
    }
    
}
