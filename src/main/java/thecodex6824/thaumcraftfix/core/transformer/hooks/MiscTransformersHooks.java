/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.common.blocks.world.ore.ShardType;
import thaumcraft.common.lib.crafting.ContainerFake;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.common.inventory.FakeArcaneWorkbenchInventory;
import thecodex6824.thaumcraftfix.common.inventory.InventoryCraftingWrapper;

public class MiscTransformersHooks {

    public static boolean handleEmptyAspectList(ItemStack stack, AspectList list) {
	if (list == null || list.size() == 0) {
	    try {
		if (stack.isItemStackDamageable() || !stack.getHasSubtypes()) {
		    stack = stack.copy();
		    stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
		}
		CommonInternals.objectTags.putIfAbsent(CommonInternals.generateUniqueItemstackId(stack),
			new AspectList());
	    } catch (Exception ex) {}
	    return false;
	}

	return true;
    }

    public static InventoryCrafting makeVanillaRecipeWrapper(InventoryCrafting input) {
	return new InventoryCraftingWrapper(input);
    }

    private static final Set<ResourceLocation> BROKEN_RECIPES = new HashSet<ResourceLocation>();

    @Nullable
    public static InventoryCrafting createFilledInventoryForRecipe(IRecipe recipe) {
	if (recipe.isDynamic() || !recipe.canFit(3, 3)) {
	    return null;
	}

	InventoryCrafting ret = null;
	if (recipe instanceof IArcaneRecipe) {
	    ret = new FakeArcaneWorkbenchInventory(new ContainerFake(), 5, 3);
	}
	else {
	    ret = new InventoryCrafting(new ContainerFake(), 3, 3);
	}

	int recipeWidth = -1;
	if (recipe instanceof IShapedRecipe) {
	    recipeWidth = ((IShapedRecipe) recipe).getRecipeWidth();
	}

	// this will place the recipe in the upper left of the grid
	// if the recipe has a width of less than 3, then we make sure to skip slots
	// so the indices line up to match the actual recipe shape
	int slot = 0;
	boolean bail = false;
	try {
	    for (Ingredient ingredient : recipe.getIngredients()) {
		boolean isEmpty = ingredient == Ingredient.EMPTY;
		if (!isEmpty && ingredient.getMatchingStacks().length == 0) {
		    return null;
		}

		ItemStack stack = isEmpty ? ItemStack.EMPTY : ingredient.getMatchingStacks()[0].copy();
		ret.setInventorySlotContents(slot++, stack);
		if (recipeWidth > 0 && (slot % 3) % recipeWidth == 0) {
		    slot += 3 - recipeWidth;
		}
	    }
	}
	catch (Exception ex) {
	    if (BROKEN_RECIPES.add(recipe.getRegistryName())) {
		ThaumcraftFix.instance.getLogger().error("Failed setting crafting grid slots (recipe might have lied about fitting in a 3x3 grid)", ex);
		ThaumcraftFix.instance.getLogger().error("Note: future errors with this recipe will not be logged");
	    }
	    bail = true;
	}

	boolean matches = false;
	if (!bail) {
	    if (recipe instanceof IArcaneRecipe) {
		IArcaneRecipe arcane = (IArcaneRecipe) recipe;
		if (arcane.getCrystals() != null) {
		    for (ShardType shard : ShardType.values()) {
			if (shard.getMetadata() < 6 && arcane.getCrystals().getAmount(shard.getAspect()) > 0) {
			    ret.setInventorySlotContents(shard.getMetadata() + 9,
				    ThaumcraftApiHelper.makeCrystal(shard.getAspect(), arcane.getCrystals().getAmount(shard.getAspect())));
			}
		    }
		}
	    }

	    try {
		matches = recipe.matches(ret, null);
	    }
	    catch (Exception ex) {
		if (BROKEN_RECIPES.add(recipe.getRegistryName())) {
		    ThaumcraftFix.instance.getLogger().error("Failed calling IRecipe#matches", ex);
		    ThaumcraftFix.instance.getLogger().error("Note: future errors with this recipe will not be logged");
		}
	    }
	}

	return matches ? ret : null;
    }

}
