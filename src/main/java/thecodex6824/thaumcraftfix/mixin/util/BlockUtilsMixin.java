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

package thecodex6824.thaumcraftfix.mixin.util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.common.lib.utils.BlockUtils;

@Mixin(BlockUtils.class)
public class BlockUtilsMixin {

    @Redirect(method = "breakFurthestBlock",
	    at = @At(value = "INVOKE",
	    target = "harvestBlockSkipCheck(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;)Z",
	    remap = false),
	    remap = false)
    private static boolean redirectHarvestBlock(World world, EntityPlayer player, BlockPos pos) {
	return BlockUtils.harvestBlock(world, player, pos, false, false, 0, false);
    }

    @WrapOperation(method = "harvestBlock(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;ZZIZ)Z",
	    at = @At(value = "INVOKE",
	    target = "Lnet/minecraftforge/common/ForgeHooks;onBlockBreakEvent(Lnet/minecraft/world/World;Lnet/minecraft/world/GameType;Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/util/math/BlockPos;)I",
	    remap = false),
	    remap = false)
    private static int wrapBlockBreakEvent(World world, GameType type, EntityPlayerMP player, BlockPos pos, Operation<Integer> original) {
	if (player instanceof FakePlayer) {
	    // do stuff the hook does but don't send packets
	    boolean preCancelEvent = false;
	    ItemStack stack = player.getHeldItemMainhand();
	    if (type.isCreative() && !stack.isEmpty()
		    && !stack.getItem().canDestroyBlockInCreative(world, pos, stack, player))
		preCancelEvent = true;

	    if (type.hasLimitedInteractions() &&
		    (type == GameType.SPECTATOR ||
		    (!player.isAllowEdit() && (stack.isEmpty() || !stack.canDestroy(world.getBlockState(pos).getBlock()))))) {
		preCancelEvent = true;
	    }

	    BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
	    event.setCanceled(preCancelEvent);
	    MinecraftForge.EVENT_BUS.post(event);
	    return event.isCanceled() ? -1 : event.getExpToDrop();
	}
	else {
	    return original.call(world, type, player, pos);
	}
    }

}