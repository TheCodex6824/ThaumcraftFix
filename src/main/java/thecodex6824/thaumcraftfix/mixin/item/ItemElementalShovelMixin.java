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

package thecodex6824.thaumcraftfix.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.items.tools.ItemElementalShovel;
import thaumcraft.common.lib.utils.InventoryUtils;

@Mixin(ItemElementalShovel.class)
public class ItemElementalShovelMixin {

    @ModifyExpressionValue(method = "onItemUse", at = @At(value = "INVOKE",
	    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
	    ordinal = 0))
    private IBlockState saveClickedBlock(IBlockState state, @Share("clickedState") LocalRef<IBlockState> clickedState) {
	clickedState.set(state);
	return state;
    }

    @ModifyExpressionValue(method = "onItemUse", at = @At(value = "INVOKE",
	    target = "Lnet/minecraft/block/Block;canPlaceBlockAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
	    ordinal = 0))
    private boolean saveItemWeAreTaking(boolean original, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
	    float hitX, float hitY, float hitZ, @Share("clickedState") LocalRef<IBlockState> clickedState,
	    @Share("goodItem") LocalRef<ItemStack> goodItem) {

	IBlockState wantedState = clickedState.get();
	Vec3d fakeHit = new Vec3d(pos.getX() + 0.5 + side.getXOffset() / 2.0, pos.getY() + 0.5 + side.getYOffset() / 2.0, pos.getZ() + 0.5 + side.getZOffset() / 2.0);
	ItemStack picked = wantedState.getBlock().getPickBlock(wantedState, new RayTraceResult(fakeHit, side, pos), world, pos, player);
	goodItem.set(picked);
	return original;
    }

    @Redirect(method = "onItemUse", at = @At(value = "INVOKE",
	    target = "Lthaumcraft/common/lib/utils/InventoryUtils;consumePlayerItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;I)Z",
	    ordinal = 0, remap = false))
    private boolean redirectConsumePlayerItemForBlockPlacement(EntityPlayer player, Item wrongItem, int wrongMeta,
	    @Share("goodItem") LocalRef<ItemStack> goodItem) {

	ItemStack stack = goodItem.get();
	return stack != null && !stack.isEmpty() ?
		InventoryUtils.consumePlayerItem(player, stack, false, false) : false;
    }

    @WrapOperation(method = "onItemUse", at = @At(value = "INVOKE",
	    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
	    ordinal = 0))
    private boolean wrapSetBlockStateForBlockPlacement(World world, BlockPos pos, IBlockState state, Operation<Boolean> op,
	    EntityPlayer player, World worldAgain, BlockPos origPos, EnumHand hand, EnumFacing side,
	    float hitX, float hitY, float hitZ,
	    @Share("goodItem") LocalRef<ItemStack> goodItem, @Share("didSomething") LocalBooleanRef didSomething) {

	boolean result = false;
	ItemStack item = goodItem.get();
	float modHitX = hitX + (pos.getX() - origPos.getX());
	float modHitY = hitY + (pos.getY() - origPos.getY());
	float modHitZ = hitZ + (pos.getZ() - origPos.getZ());
	IBlockState toPlace = state.getBlock().getStateForPlacement(world, pos, side, modHitX, modHitY,
		modHitZ, item.getMetadata(), player, hand);
	IBlockState existing = world.getBlockState(pos);
	if (existing.getBlock().isReplaceable(world, pos) && player.canPlayerEdit(pos, side, item) &&
		world.mayPlace(state.getBlock(), pos, false, side, player)) {

	    if (item.getItem() instanceof ItemBlock) {
		ItemBlock itemBlock = (ItemBlock) item.getItem();
		result = itemBlock.placeBlockAt(item, player, world, pos, side,
			modHitX, modHitY, modHitZ, toPlace);
	    }
	    else {
		result = op.call(toPlace, pos);
	    }
	}

	if (result) {
	    didSomething.set(true);
	}

	return result;
    }

    @ModifyReturnValue(method = "onItemUse", at = @At("RETURN"))
    private EnumActionResult changeActionResult(EnumActionResult original, @Share("didSomething") LocalBooleanRef didSomething) {
	return didSomething.get() ? EnumActionResult.SUCCESS : original;
    }

}
