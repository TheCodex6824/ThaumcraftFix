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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

}
