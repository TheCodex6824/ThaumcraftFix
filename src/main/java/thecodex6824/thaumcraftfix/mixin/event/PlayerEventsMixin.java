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

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.common.lib.events.PlayerEvents;
import thecodex6824.thaumcraftfix.api.research.ResearchCategoryTheorycraftFilter;

@Mixin(PlayerEvents.class)
public class PlayerEventsMixin {

    @ModifyExpressionValue(
	    method = "pickupXP",
	    at = @At(value = "INVOKE", target = "Ljava/util/Set;toArray([Ljava/lang/Object;)[Ljava/lang/Object;"),
	    remap = false
	    )
    private static Object[] filterCategories(Object[] originalCats) {
	String[] original = (String[]) originalCats;
	ArrayList<String> retained = new ArrayList<>();
	Set<String> keep = ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategoryKeys();
	for (String cat : original) {
	    if (keep.contains(cat)) {
		retained.add(cat);
	    }
	}

	return retained.toArray(new String[retained.size()]);
    }

	@Inject(
			method = "getRunicCharge(Lnet/minecraft/item/ItemStack;)I",
			at = @At("HEAD"),
			remap = false,
			cancellable = true
	)
	private static void modifyRunicCharge(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		int base = 0;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("TC.RUNIC")) {
			base += stack.getTagCompound().getInteger("TC.RUNIC");
		}
		cir.setReturnValue(base);
	}

	@Inject(
			method = "getFinalWarp",
			at = @At("HEAD"),
			remap = false,
			cancellable = true
	)
	private static void mixinGetFinalWarp(ItemStack stack, EntityPlayer player, CallbackInfoReturnable<Integer> cir) {
		if (stack != null && !stack.isEmpty()) {
			int warp = 0;
			if (stack.getItem() instanceof IWarpingGear) {
				IWarpingGear armor = (IWarpingGear)stack.getItem();
				warp += armor.getWarp(stack, player);
			}

			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("TC.WARP")) {
				warp += stack.getTagCompound().getInteger("TC.WARP");
			}

			cir.setReturnValue(warp);
		} else {
			cir.setReturnValue(0);
		}
	}
}
