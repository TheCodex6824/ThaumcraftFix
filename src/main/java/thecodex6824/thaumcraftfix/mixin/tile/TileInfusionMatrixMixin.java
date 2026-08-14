package thecodex6824.thaumcraftfix.mixin.tile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thecodex6824.thaumcraftfix.api.tile.IInfusionMatrix;
import thecodex6824.thaumcraftfix.core.transformer.hooks.TileTransformersHooks;

@Mixin(value = TileInfusionMatrix.class, remap = false)
public class TileInfusionMatrixMixin extends TileEntity implements IInfusionMatrix {

    @Shadow
    private boolean crafting;
    @Shadow
    private AspectList recipeEssentia;
    
    @ModifyExpressionValue(method = "craftCycle", at = @At(
	    value = "INVOKE",
	    target = "Lnet/minecraft/item/Item;getContainerItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
	    ))
    private ItemStack getRemainingItem(ItemStack originalResult, @Local(ordinal = 0) TileEntity pedestal) {
	return TileTransformersHooks.getInfusionRemainingItem(this, originalResult, pedestal);
    }
    
    @Override
    public AspectList essentiaRemaining() {
        return recipeEssentia;
    }
    
    @Override
    public boolean isCrafting() {
        return crafting;
    }
    
    @Override
    public TileEntity asTileEntity() {
        return this;
    }
    
}
