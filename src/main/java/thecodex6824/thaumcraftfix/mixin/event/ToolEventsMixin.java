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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.common.lib.events.ToolEvents;

@Mixin(ToolEvents.class)
public class ToolEventsMixin {

    @ModifyExpressionValue(method = "breakBlockEvent(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V",
	    at = @At(value = "INVOKE",
	    target = "Lnet/minecraftforge/common/ForgeHooks;isToolEffective(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)Z",
	    remap = false),
	    remap = false)
    private static boolean isToolEffectiveForBurrowing(boolean original, BlockEvent.BreakEvent event) {
	if (original) {
	    return true;
	}

	IBlockState state = event.getWorld().getBlockState(event.getPos());
	return state.getBlock().getHarvestTool(state) == null;
    }

    private static final ThreadLocal<Boolean> processingBurrowing = ThreadLocal.withInitial(() -> false);

    @WrapOperation(method = "breakBlockEvent(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V",
	    at = @At(value = "INVOKE",
	    target = "isValidBurrowBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
	    remap = false),
	    remap = false)
    private static boolean wrapIsValidBurrowBlock(World world, BlockPos pos, Operation<Boolean> original) {
	if (!processingBurrowing.get()) {
	    processingBurrowing.set(true);
	    boolean result = false;
	    try {
		result = original.call(world, pos);
	    }
	    finally {
		if (!result) {
		    processingBurrowing.set(false);
		}
	    }

	    return result;
	}

	return false;
    }

    @WrapOperation(method = "breakBlockEvent(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V",
	    at = @At(value = "INVOKE",
	    target = "Lthaumcraft/common/lib/utils/BlockUtils;breakFurthestBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;)Z",
	    remap = false),
	    remap = false)
    private static boolean wrapBreakFurthestBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player,
	    Operation<Boolean> original) {

	boolean result = false;
	try {
	    result = original.call(world, pos, state, player);
	}
	finally {
	    processingBurrowing.set(false);
	}

	return result;
    }

}
