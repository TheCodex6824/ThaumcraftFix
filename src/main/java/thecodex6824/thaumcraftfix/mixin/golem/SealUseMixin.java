package thecodex6824.thaumcraftfix.mixin.golem;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.seals.SealUse;

@Mixin(value = SealUse.class, remap = false)
public class SealUseMixin {
    /**
     * @author Invadermonky
     * @reason Fixes golems voiding their held item when interacting with a Use seal with "Can use empty eand" is enabled.
     */
    @Inject(
            method = "onTaskCompletion",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/golems/GolemInteractionHelper;golemClick(Lnet/minecraft/world/World;Lthaumcraft/api/golems/IGolemAPI;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/item/ItemStack;ZZ)V",
                    shift = At.Shift.AFTER
            )
    )
    private void replaceHeldItemMixin(World world, IGolemAPI golem, Task task, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) ItemStack stack) {
        golem.holdItem(stack);
    }
}
