package thecodex6824.thaumcraftfix.mixin.network;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.common.lib.network.fx.PacketFXPollute;

@Mixin(value = PacketFXPollute.class, remap = false)
public class PacketFXPolluteMixin {

    /**
     * @author Invadermonky
     * @reason Fixes a client-side crash that occurs when the float 'amt' parameter is larger than
     *         127 or smaller than -128.
     */
    @Inject(method = "<init>(Lnet/minecraft/util/math/BlockPos;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getX()I"))
    private void clampAmountMixin(BlockPos pos, float amt, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef ref) {
        ref.set(MathHelper.clamp(ref.get(), Byte.MIN_VALUE, Byte.MAX_VALUE));
    }
}
