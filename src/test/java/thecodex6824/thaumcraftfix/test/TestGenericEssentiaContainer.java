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

package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.items.ItemGenericEssentiaContainer;

public class TestGenericEssentiaContainer {

    private static final int FILLED_AMOUNT = 1;
    private static final String FAKE_ASPECT = "unobtanium";

    private NBTTagCompound createAspectTag(String... aspects) {
	NBTTagCompound tag = new NBTTagCompound();
	NBTTagList list = new NBTTagList();
	for (String s : aspects) {
	    NBTTagCompound aspect = new NBTTagCompound();
	    aspect.setString("key", s);
	    aspect.setInteger("amount", FILLED_AMOUNT);
	    list.appendTag(aspect);
	}

	tag.setTag("Aspects", list);
	return tag;
    }

    @BeforeAll
    static void setup() {
	// required for item operations to work
	// TODO: move most setups into common setup for all suites
	Bootstrap.register();
    }

    @Test
    void testRawNbtNormal() {
	ItemGenericEssentiaContainer item = new ItemGenericEssentiaContainer(1);
	ItemStack stack = new ItemStack(item, FILLED_AMOUNT);
	stack.setTagCompound(createAspectTag(Aspect.AIR.getTag()));
	assertNotNull(item.getAspects(stack));
    }

    @Test
    void testRawNbtMissingAspect() {
	ItemGenericEssentiaContainer item = new ItemGenericEssentiaContainer(1);
	ItemStack stack = new ItemStack(item, FILLED_AMOUNT);
	stack.setTagCompound(createAspectTag(FAKE_ASPECT));
	assertNull(item.getAspects(stack));
    }

    @Test
    void testRawNbtSomeMissing() {
	ItemGenericEssentiaContainer item = new ItemGenericEssentiaContainer(1);
	ItemStack stack = new ItemStack(item, FILLED_AMOUNT);
	stack.setTagCompound(createAspectTag(Aspect.AIR.getTag(), FAKE_ASPECT));
	AspectList aspects = item.getAspects(stack);
	assertNotNull(aspects);
	assertEquals(1, aspects.size());
	assertEquals(aspects.getAmount(Aspect.AIR), FILLED_AMOUNT);
    }

}
