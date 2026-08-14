package thecodex6824.thaumcraftfix.mixin.focus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.common.items.casters.ItemFocus;

@Mixin(value = ItemFocus.class, remap = false)
public class ItemFocusMixin {

    @ModifyExpressionValue(
	    method = "getSortingHelper",
	    at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTagCompound()Z", remap = true)
	    )
    private boolean checkFocusForPackage(boolean original, ItemStack stack) {
	return original && stack.getTagCompound().hasKey("package", NBT.TAG_COMPOUND);
    }
    
}
