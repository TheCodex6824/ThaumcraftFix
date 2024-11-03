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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.curios.ItemPrimordialPearl;
import thecodex6824.thaumcraftfix.ThaumcraftFix;

@Mixin(ItemPrimordialPearl.class)
public class ItemPrimordialPearlMixin extends Item {

    private boolean fixEnabled() {
	return ThaumcraftFix.instance.getConfig().item.primordialPearlDamageFix.value();
    }

    /*
     * All methods overwritten in this Mixin don't exist in ItemPrimordialPearl
     */

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
	return fixEnabled() ? stack.getItemDamage() > 0 : super.showDurabilityBar(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
	return fixEnabled() ? stack.getItemDamage() / 8.0 : super.getDurabilityForDisplay(stack);
    }

    @Override
    @Deprecated
    public int getMaxDamage() {
	return fixEnabled() ? 0 : super.getMaxDamage();
    }

    @Override
    public boolean getHasSubtypes() {
	return fixEnabled() ? true : super.getHasSubtypes();
    }

}
