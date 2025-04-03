/**
 *  Thaumcraft Fix
 *  Copyright (c) 2024 TheCodex6824.
 *
 *  This file is part of Thaumcraft Fix.
 *
 *  Thaumcraft Fix is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumcraft Fix is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumcraft Fix.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumcraftfix.mixin.event;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Predicate;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.lib.events.ServerEvents.VirtualSwapper;
import thaumcraft.common.world.aura.AuraThread;
import thecodex6824.thaumcraftfix.api.aura.CapabilityAuraProcessor;
import thecodex6824.thaumcraftfix.common.aura.GenericAuraThread;

@Mixin(ServerEvents.class)
@SuppressWarnings("deprecation")
public class ServerEventsMixin {

    @Redirect(method = "worldTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$WorldTickEvent;)V",
	    at = @At(value = "NEW", target = "(I)Lthaumcraft/common/world/aura/AuraThread;", remap = false), remap = false)
    private static AuraThread redirectCreateAuraThread(int dim, TickEvent.WorldTickEvent event) {
	return new GenericAuraThread(dim,
		event.world.getCapability(CapabilityAuraProcessor.AURA_PROCESSOR, null));
    }

    private static ThreadLocal<Field> sourceField = new ThreadLocal<>();
    private static ThreadLocal<Field> targetField = new ThreadLocal<>();
    private static ThreadLocal<Field> playerField = new ThreadLocal<>();

    private static Object getSwapperSource(VirtualSwapper vs) {
	try {
	    if (sourceField.get() == null) {
		Field field = VirtualSwapper.class.getDeclaredField("source");
		field.setAccessible(true);
		sourceField.set(field);
	    }

	    return sourceField.get().get(vs);
	}
	catch (ReflectiveOperationException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static ItemStack getSwapperTarget(VirtualSwapper vs) {
	try {
	    if (targetField.get() == null) {
		Field field = VirtualSwapper.class.getDeclaredField("target");
		field.setAccessible(true);
		targetField.set(field);
	    }

	    return (ItemStack) targetField.get().get(vs);
	}
	catch (ReflectiveOperationException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static EntityPlayer getSwapperPlayer(VirtualSwapper vs) {
	try {
	    if (playerField.get() == null) {
		Field field = VirtualSwapper.class.getDeclaredField("player");
		field.setAccessible(true);
		playerField.set(field);
	    }

	    return (EntityPlayer) playerField.get().get(vs);
	}
	catch (ReflectiveOperationException ex) {
	    throw new RuntimeException(ex);
	}
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "NEW",
		    target = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraftforge/common/util/BlockSnapshot;",
		    remap = false),
	    remap = false)
    private static BlockSnapshot wrapCreateSnapshot(World world, BlockPos pos, IBlockState oldStateIgnore,
	    Operation<BlockSnapshot> originalThatIsWrong,
	    @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef, @Local(ordinal = 0) VirtualSwapper vs) {

	IBlockState toPlace = null;
	ItemStack target = getSwapperTarget(vs);
	if (target != null && !target.isEmpty()) {
	    Block block = Block.getBlockFromItem(target.getItem());
	    if (block != null && block != Blocks.AIR) {
		EntityPlayer placer = getSwapperPlayer(vs);
		toPlace = block.getStateForPlacement(world, pos, EnumFacing.UP, 0.5f, 0.5f, 0.5f,
			target.getMetadata(), placer, placer.getActiveHand());
	    }
	}

	BlockSnapshot newSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
	snapshotRef.set(newSnapshot);
	if (toPlace != null) {
	    world.setBlockState(pos, toPlace);
	}
	else {
	    world.setBlockToAir(pos);
	}

	return newSnapshot;
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerBlockPlace(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraftforge/common/util/BlockSnapshot;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/EnumHand;)Lnet/minecraftforge/event/world/BlockEvent$PlaceEvent;",
		    remap = false),
	    remap = false)
    private static net.minecraftforge.event.world.BlockEvent.PlaceEvent wrapPlaceEvent(@Nonnull EntityPlayer player,
	    @Nonnull BlockSnapshot snapshot, @Nonnull EnumFacing direction, @Nonnull EnumHand hand,
	    Operation<net.minecraftforge.event.world.BlockEvent.PlaceEvent> original) {

	// the full name is used to not fail the build for importing a deprecated thing
	net.minecraftforge.event.world.BlockEvent.PlaceEvent result = original.call(player, snapshot, direction, hand);
	if (result.isCanceled()) {
	    snapshot.restore(true, false);
	}

	return result;
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z",
		    remap = false,
		    ordinal = 0),
	    remap = false)
    private static boolean wrapSwapperPredicateCheck(Predicate<Boolean> predicate, @Nullable Object input,
	    Operation<Boolean> original, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {

	boolean result = original.call(predicate, input);
	if (!result) {
	    snapshotRef.get().restore(true, false);
	}

	return result;
    }

    @Inject(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "FIELD",
		    target = "Lthaumcraft/common/lib/events/ServerEvents$VirtualSwapper;source:Ljava/lang/Object;",
		    ordinal = 10,
		    opcode = Opcodes.GETFIELD),
	    remap = false)
    private static void handleRestoreIfNeeded(World world, CallbackInfo info,
	    @Local(ordinal = 0) VirtualSwapper vs, @Local(ordinal = 1) boolean matches,
	    @Local(ordinal = 1) int slot, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {

	if ((getSwapperSource(vs) != null && !matches) || slot < 0) {
	    snapshotRef.get().restore(true, false);
	}
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"
		    ),
	    remap = false)
    private static boolean wrapOriginalSetBlockstate(World world, BlockPos pos, IBlockState state,
	    int flags, Operation<Boolean> original, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {

	boolean result = true;
	// if snapshot is null some mixin got messed up, so set the state to not break even more
	if (snapshotRef.get() == null) {
	    result = original.call(world, pos, state, flags);
	}

	return result;
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"
		    ),
	    remap = false)
    private static boolean wrapOriginalSetBlockToAir(World world, BlockPos pos,
	    Operation<Boolean> original, @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {

	boolean result = true;
	if (snapshotRef.get() == null) {
	    result = original.call(world, pos);
	}

	return result;
    }

}
