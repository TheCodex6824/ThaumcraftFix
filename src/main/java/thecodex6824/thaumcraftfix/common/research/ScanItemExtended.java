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

package thecodex6824.thaumcraftfix.common.research;

import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.research.IScanThing;

public class ScanItemExtended implements IScanThing {

    protected String research;
    protected ItemStack ref;
    protected boolean strict;

    @Nullable
    protected NBTTagCompound caps;

    public ScanItemExtended(String research, ItemStack stack) {
	this(research, stack, null, false);
    }

    public ScanItemExtended(String research, ItemStack stack, @Nullable NBTTagCompound capabilities, boolean strictMatching) {
	this.research = research;
	ref = stack;
	caps = capabilities;
	strict = strictMatching;
    }

    protected boolean tagsMatch(NBTBase tag1, NBTBase tag2) {
	if (strict)
	    return tag1.equals(tag2);

	if (tag1.getId() == tag2.getId()) {
	    if (tag1 instanceof NBTTagCompound) {
		NBTTagCompound compound1 = (NBTTagCompound) tag1;
		NBTTagCompound compound2 = (NBTTagCompound) tag2;
		for (String k : compound1.getKeySet()) {
		    if (!compound2.hasKey(k, compound1.getTagId(k)) ||
			    !tagsMatch(compound1.getTag(k), compound2.getTag(k))) {
			return false;
		    }
		}

		return true;
	    }
	    else if (tag1 instanceof NBTTagList) {
		NBTTagList list1 = (NBTTagList) tag1;
		NBTTagList list2 = (NBTTagList) tag2;
		for (NBTBase find1 : list1) {
		    boolean found = false;
		    for (NBTBase find2 : list2) {
			if (find1.equals(find2)) {
			    found = true;
			    break;
			}
		    }

		    if (!found)
			return false;
		}

		return true;
	    }
	    else
		return tag1.equals(tag2);
	}

	return false;
    }

    @SuppressWarnings("null")
    protected boolean matches(ItemStack stack) {
	if (!OreDictionary.itemMatches(ref, stack, strict))
	    return false;

	if (ref.hasTagCompound()) {
	    if (!stack.hasTagCompound() || !tagsMatch(ref.getTagCompound(), stack.getTagCompound()))
		return false;
	}

	// can't use ItemStack#areCapsCompatible directly because we want to allow extra capabilities
	NBTTagCompound stackTag = stack.serializeNBT();
	boolean refCaps = caps != null;
	boolean stackCaps = stackTag.hasKey("ForgeCaps", NBT.TAG_COMPOUND);
	if (!refCaps && !stackCaps)
	    return true;
	else if (refCaps ^ stackCaps)
	    return false;

	// copy over any extra caps not present in the json
	// this ensures that they will match in areCapsCompatible
	NBTTagCompound capsRef = caps.copy();
	NBTTagCompound capsStack = stackTag.getCompoundTag("ForgeCaps");
	for (String key : capsStack.getKeySet()) {
	    if (!capsRef.hasKey(key, capsStack.getTagId(key)))
		capsRef.setTag(key, capsStack.getTag(key));
	}

	NBTTagCompound refTag = ref.serializeNBT();
	refTag.setTag("ForgeCaps", capsRef);
	return new ItemStack(stackTag).areCapsCompatible(new ItemStack(refTag));
    }

    @Override
    public boolean checkThing(EntityPlayer player, Object thing) {
	if (thing == null)
	    return false;

	ItemStack stack = null;
	if (thing instanceof EntityItem)
	    stack = ((EntityItem) thing).getItem();
	else if (thing instanceof ItemStack)
	    stack = (ItemStack) thing;

	if (stack == null || stack.isEmpty())
	    return false;

	return matches(stack);
    }

    @Override
    public String getResearchKey(EntityPlayer player, Object thing) {
	return research;
    }

}
