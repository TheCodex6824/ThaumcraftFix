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

import baubles.api.cap.BaublesCapabilities;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.tools.ItemHandMirror;

public class ItemTransformersHooksCommon {

    public static boolean shouldAllowRunicShield(ItemStack stack) {
	return stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
    }

    private static int[] getValidMetadata(Item item) {
	IntRBTreeSet visitedMeta = new IntRBTreeSet();
	for (CreativeTabs tab : item.getCreativeTabs()) {
	    NonNullList<ItemStack> stacks = NonNullList.create();
	    item.getSubItems(tab, stacks);
	    for (ItemStack stack : stacks) {
		if (stack.getItem() == item)
		    visitedMeta.add(stack.getMetadata());
	    }
	}

	return visitedMeta.toIntArray();
    }

    public static ItemStack cycleItemStack(ItemStack fallback, Object thing, int counter) {
	if (thing instanceof ItemStack) {
	    ItemStack stack = (ItemStack) thing;
	    if (!stack.isEmpty() && stack.getHasSubtypes() && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
		int[] validMeta = getValidMetadata(stack.getItem());
		if (validMeta.length > 0) {
		    int timer = 5000 / validMeta.length;
		    int metaIndex = (int) ((counter + System.currentTimeMillis() / timer) % validMeta.length);
		    ItemStack copy = stack.copy();
		    copy.setItemDamage(validMeta[metaIndex]);
		    return copy;
		}
	    }
	}

	return fallback;
    }

    public static void setFocusStackColor(ItemStack maybeFocus) {
	Item item = maybeFocus.getItem();
	if (item instanceof ItemFocus) {
	    ((ItemFocus) item).getFocusColor(maybeFocus);
	}
    }

    public static int nullCheckTags(NBTTagCompound prime, NBTTagCompound other) {
	int result = 0;
	if (prime == null) {
	    result = 0b10;
	}
	else if (other == null) {
	    result = 0b1;
	}

	// what is returned here has 2 meanings:
	// if nonzero, we want to exit early - return value will be this shifted right by 1
	// this lets us effectively return 2 booleans from 1 function, since internally booleans are just ints
	return result;
    }

    public static ItemStack getHandMirrorStack(ItemStack original, InventoryPlayer playerInv) {
	EntityPlayer player = playerInv.player;
	ItemStack mirror = player.getHeldItemMainhand();
	if (!(mirror.getItem() instanceof ItemHandMirror)) {
	    mirror = player.getHeldItemOffhand();
	    if (!(mirror.getItem() instanceof ItemHandMirror)) {
		mirror = ItemStack.EMPTY;
	    }
	}

	if (mirror.isEmpty()) {
	    player.closeScreen();
	}
	return mirror;
    }

    public static int getItemConsumeAmount(int original, EntityLivingBase entity) {
	return entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative() ?
		0 : original;
    }

    public static boolean skipGivingPhial(EntityPlayer player) {
	return player.isCreative();
    }

}
