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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Constant.Condition;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

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
import net.minecraftforge.event.ForgeEventFactory;
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

    private static ThreadLocal<Field> playerField = new ThreadLocal<>();
    private static ThreadLocal<Field> posField = new ThreadLocal<>();
    private static ThreadLocal<Field> targetField = new ThreadLocal<>();

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

    private static BlockPos getSwapperPos(VirtualSwapper vs) {
	try {
	    if (posField.get() == null) {
		Field field = VirtualSwapper.class.getDeclaredField("pos");
		field.setAccessible(true);
		posField.set(field);
	    }

	    return (BlockPos) posField.get().get(vs);
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

    @Redirect(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerBlockPlace(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraftforge/common/util/BlockSnapshot;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/EnumHand;)Lnet/minecraftforge/event/world/BlockEvent$PlaceEvent;",
		    remap = false),
	    remap = false)
    private static net.minecraftforge.event.world.BlockEvent.PlaceEvent redirectPlaceEvent(@Nonnull EntityPlayer player,
	    @Nonnull BlockSnapshot snapshot, @Nonnull EnumFacing direction, @Nonnull EnumHand hand,
	    @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef) {

	snapshotRef.set(snapshot);
	// this event just has to be not cancelled - don't post it and the fields don't really matter
	// fully qualified name is used to not fail build for importing a deprecated thing
	return new net.minecraftforge.event.world.BlockEvent.PlaceEvent(snapshot,
		snapshot.getReplacedBlock(), player, hand);
    }

    // this can be replaced with a MixinExtras expression once MixinBooter updates to a non-broken version
    @ModifyConstant(
	    method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    constant = @Constant(ordinal = 0, expandZeroConditions = Condition.GREATER_THAN_OR_EQUAL_TO_ZERO),
	    remap = false
	    )
    private static int wrapSlotCheck(int original, World world,
	    @Share("snapshot") LocalRef<BlockSnapshot> snapshotRef, @Local(ordinal = 0) VirtualSwapper vs,
	    @Local(ordinal = 1) int slot) {

	boolean placeAllowed = slot >= original;
	BlockSnapshot snapshot = snapshotRef.get();
	if (placeAllowed && snapshot != null) {
	    IBlockState toPlace = null;
	    ItemStack target = getSwapperTarget(vs);
	    if (target != null && !target.isEmpty()) {
		Block block = Block.getBlockFromItem(target.getItem());
		if (block != null && block != Blocks.AIR) {
		    toPlace = block.getStateFromMeta(target.getItemDamage());
		}
	    }

	    BlockPos pos = getSwapperPos(vs);
	    if (toPlace != null) {
		world.setBlockState(pos, toPlace);
		EntityPlayer player = getSwapperPlayer(vs);
		placeAllowed = !ForgeEventFactory.onPlayerBlockPlace(player, snapshot, EnumFacing.UP,
			EnumHand.MAIN_HAND).isCanceled();
		if (!placeAllowed) {
		    // we can't restore block snapshots since side effects of the block being destroyed already happened
		    // instead, just drop the old block as an item and leave it as air
		    world.setBlockToAir(pos);
		    if (!player.isCreative()) {
			snapshot.getReplacedBlock().getBlock().dropBlockAsItem(
				snapshot.getWorld(), snapshot.getPos(), snapshot.getReplacedBlock(), 0);
		    }
		}
	    }
	    else {
		world.setBlockToAir(pos);
		// there is no further event if the block is being set to air
	    }
	}

	return placeAllowed ? original : Integer.MAX_VALUE;
    }

    @WrapOperation(method = "tickBlockSwap(Lnet/minecraft/world/World;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
		    remap = true
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
		    target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z",
		    remap = true
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
