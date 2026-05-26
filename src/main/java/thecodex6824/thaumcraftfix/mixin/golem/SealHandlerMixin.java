package thecodex6824.thaumcraftfix.mixin.golem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;

@Mixin(value = SealHandler.class, remap = false)
public class SealHandlerMixin {

    private static void markDirty(World world, BlockPos pos) {
	if (!world.isRemote) {
	    world.markChunkDirty(pos, null);
	}
    }

    @Inject(method = "removeSealEntity", at = @At(
	    value = "INVOKE",
	    target = "Lthaumcraft/common/golems/seals/SealHandler;markChunkAsDirty(ILnet/minecraft/util/math/BlockPos;)V",
	    remap = false
	    ))
    private static void markChunkAsDirtyRemove(World world, SealPos pos, boolean quiet, CallbackInfo ci) {
	markDirty(world, pos.pos);
    }

    @Inject(method = "addSealEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lthaumcraft/api/golems/seals/ISeal;Lnet/minecraft/entity/player/EntityPlayer;)Z", at = @At(
	    value = "INVOKE",
	    target = "Lthaumcraft/common/golems/seals/SealHandler;markChunkAsDirty(ILnet/minecraft/util/math/BlockPos;)V",
	    remap = false
	    ))
    private static void markChunkAsDirtyAddFiveArg(World world, BlockPos pos, EnumFacing face, ISeal seal, EntityPlayer player, CallbackInfoReturnable<Boolean> ci) {
	markDirty(world, pos);
    }

    // note: this overload is called when the chunk loads
    // normally it would make no sense to immediately mark the chunk as dirty, but TC already tries to do it...
    // I also don't know if addons are calling this overload, so just mark it as dirty anyway
    @Inject(method = "addSealEntity(Lnet/minecraft/world/World;Lthaumcraft/common/golems/seals/SealEntity;)Z", at = @At(
	    value = "INVOKE",
	    target = "Lthaumcraft/common/golems/seals/SealHandler;markChunkAsDirty(ILnet/minecraft/util/math/BlockPos;)V",
	    remap = false
	    ))
    private static void markChunkAsDirtyAddTwoArg(World world, SealEntity seal, CallbackInfoReturnable<Boolean> ci) {
	markDirty(world, seal.getSealPos().pos);
    }

}
