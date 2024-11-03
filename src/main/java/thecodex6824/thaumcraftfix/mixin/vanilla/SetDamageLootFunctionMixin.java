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

package thecodex6824.thaumcraftfix.mixin.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.functions.SetDamage;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumcraftfix.ThaumcraftFix;

@Mixin(SetDamage.class)
public class SetDamageLootFunctionMixin {

    @ModifyExpressionValue(method = "Lnet/minecraft/world/storage/loot/functions/SetDamage;apply("
	    + "Lnet/minecraft/item/ItemStack;"
	    + "Ljava/util/Random;"
	    + "Lnet/minecraft/world/storage/loot/LootContext;"
	    + ")Lnet/minecraft/item/ItemStack;",
	    at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemStackDamageable()Z"))
    private boolean isStackDamageable(boolean original, ItemStack stack) {
	return original || (stack.getItem() == ItemsTC.primordialPearl &&
		ThaumcraftFix.instance.getConfig().item.primordialPearlDamageFix.value());
    }

    @ModifyExpressionValue(method = "Lnet/minecraft/world/storage/loot/functions/SetDamage;apply("
	    + "Lnet/minecraft/item/ItemStack;"
	    + "Ljava/util/Random;"
	    + "Lnet/minecraft/world/storage/loot/LootContext;"
	    + ")Lnet/minecraft/item/ItemStack;",
	    at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxDamage()I"))
    private int getMaxDamage(int original, ItemStack stack) {
	if (stack.getItem() == ItemsTC.primordialPearl &&
		ThaumcraftFix.instance.getConfig().item.primordialPearlDamageFix.value()) {
	    original = 8;
	}

	return original;
    }

}
