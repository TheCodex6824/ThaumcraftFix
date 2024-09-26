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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import thaumcraft.common.items.curios.ItemPrimordialPearl;

@Mixin(ItemPrimordialPearl.class)
public class ItemPrimordialPearlMixin extends Item {

    // overwrite reason: method doesn't exist in the original class
    // note: these 2 methods are forge methods so aren't obfuscated
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
	return stack.getItemDamage() > 0;
    }

    // overwrite reason: method doesn't exist in the original class,
    // and the super implementation is dangerous (divide by zero risk)
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
	return stack.getItemDamage() / 8.0;
    }

    @Inject(method = "<init>()V", at = @At("RETURN"), remap = false)
    private void construct(CallbackInfo info) {
	setMaxDamage(0);
	((RegistrySimple<?, ?>) properties).registryObjects.remove(new ResourceLocation("damage"));
	((RegistrySimple<?, ?>) properties).registryObjects.remove(new ResourceLocation("damaged"));
	setHasSubtypes(true);
    }

}
