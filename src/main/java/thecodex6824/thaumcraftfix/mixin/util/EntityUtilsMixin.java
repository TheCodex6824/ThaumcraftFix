package thecodex6824.thaumcraftfix.mixin.util;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.lib.utils.EntityUtils;

@Mixin(value = EntityUtils.class, remap = false)
public class EntityUtilsMixin {
    /**
     * @author Invadermonky
     * @reason Fixes Primordial Pearls spawning 3-4 blocks in the air after enemy is slain.
     */
    @Inject(method = "entityDropSpecialItem", at = @At("HEAD"))
    private static void modifyDropHeightMixin(Entity entity, ItemStack stack, float dropheight, CallbackInfoReturnable<EntityItem> cir, @Local(argsOnly = true) LocalFloatRef ref) {
        ref.set(0);
    }
}
