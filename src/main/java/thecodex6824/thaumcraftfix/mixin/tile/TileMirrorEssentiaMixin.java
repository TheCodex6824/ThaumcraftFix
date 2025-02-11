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

package thecodex6824.thaumcraftfix.mixin.tile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;

@Mixin(TileMirrorEssentia.class)
public class TileMirrorEssentiaMixin extends TileThaumcraft {

    private TileEntity getTileIfWorldExists(World world, BlockPos pos, Operation<TileEntity> original) {
	return world != null ? original.call(world, pos) : null;
    }

    @WrapOperation(method = "doesContainerAccept", at = @At(value = "INVOKE", target =
	    "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"), remap = false)
    private TileEntity wrapDoesContainerAcceptGetTile(World world, BlockPos pos, Operation<TileEntity> original) {
	return getTileIfWorldExists(world, pos, original);
    }

    @WrapOperation(method = "addToContainer", at = @At(value = "INVOKE", target =
	    "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"), remap = false)
    private TileEntity wrapAddToContainerGetTile(World world, BlockPos pos, Operation<TileEntity> original) {
	return getTileIfWorldExists(world, pos, original);
    }

    @WrapOperation(method = "takeFromContainer(Lthaumcraft/api/aspects/Aspect;I)Z", at = @At(value = "INVOKE", target =
	    "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"), remap = false)
    private TileEntity wrapTakeFromContainerGetTile(World world, BlockPos pos, Operation<TileEntity> original) {
	return getTileIfWorldExists(world, pos, original);
    }

    @WrapOperation(method = "doesContainerContainAmount", at = @At(value = "INVOKE", target =
	    "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"), remap = false)
    private TileEntity wrapDoesContainerContainAmountGetTile(World world, BlockPos pos, Operation<TileEntity> original) {
	return getTileIfWorldExists(world, pos, original);
    }

}
