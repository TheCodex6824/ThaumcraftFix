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

package thecodex6824.thaumcraftfix.core.mixin.block;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.basic.BlockPillar;

@Mixin(BlockPillar.class)
public abstract class BlockPillarMixin extends Block {

    private BlockPillarMixin(Material material) {
	super(material);
    }

    private static final int DROP_QUANTITY = 2;

    @Redirect(
	    method = "Lthaumcraft/common/blocks/basic/BlockPillar;breakBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
	    at = @At(value = "INVOKE", target = "Lthaumcraft/common/blocks/basic/BlockPillar;spawnAsEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V")
	    )
    private void doNotSpawnItemsInBreakBlock(World world, BlockPos pos, ItemStack stack) {
	// nope
    }

    // these 3 all don't exist in BlockPillar
    @Override
    public int quantityDropped(Random random) {
	return DROP_QUANTITY;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
	return DROP_QUANTITY;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
	return DROP_QUANTITY;
    }

    @ModifyReturnValue(
	    method = "getItemDropped(Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;I)Lnet/minecraft/item/Item;",
	    at = @At("RETURN")
	    )
    private Item setDroppedItem(Item original, IBlockState state) {
	if (original == Items.AIR) {
	    if (state.getBlock() == BlocksTC.pillarAncient) {
		original = Item.getItemFromBlock(BlocksTC.stoneAncient);
	    }
	    else if (state.getBlock() == BlocksTC.pillarEldritch) {
		original = Item.getItemFromBlock(BlocksTC.stoneEldritchTile);
	    }
	    else {
		original = Item.getItemFromBlock(BlocksTC.stoneArcane);
	    }
	}

	return original;
    }

}
