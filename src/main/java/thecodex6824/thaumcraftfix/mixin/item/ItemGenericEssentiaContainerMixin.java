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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.items.ItemGenericEssentiaContainer;

@Mixin(ItemGenericEssentiaContainer.class)
public class ItemGenericEssentiaContainerMixin {

    @ModifyReturnValue(
	    method = "getAspects",
	    at = @At("RETURN"),
	    remap = false
	    )
    private AspectList filterAspects(AspectList original) {
	if (original != null) {
	    // remove null aspects (that don't exist)
	    original.aspects.remove(null);
	    if (original.size() == 0) {
		// thaumcraft returns null if the item has 0 aspects
		original = null;
	    }
	}

	return original;
    }

}
