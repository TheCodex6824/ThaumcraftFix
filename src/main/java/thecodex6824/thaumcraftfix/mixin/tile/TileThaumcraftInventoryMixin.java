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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.entity.player.EntityPlayerMP;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.TileThaumcraftInventory;

@Mixin(TileThaumcraftInventory.class)
public class TileThaumcraftInventoryMixin extends TileThaumcraft {

    @WrapMethod(method = "syncSlots", remap = false)
    private void wrapSyncSlots(EntityPlayerMP player, Operation<Void> original) {
	if (!world.isRemote) {
	    original.call(player);
	}
    }

}
