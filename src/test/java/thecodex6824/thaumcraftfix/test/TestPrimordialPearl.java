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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.events.CraftingEvents;

public class TestPrimordialPearl {

    private static final int PEARL_MAX_META = 8;

    static Supplier<Stream<? extends Arguments>> testPearlDurabilityProperties = () ->
    IntStream.rangeClosed(0, PEARL_MAX_META)
    .mapToObj(i -> Arguments.of(i, (double) i / PEARL_MAX_META));

    @ParameterizedTest
    @FieldSource
    void testPearlDurabilityProperties(int metadata, double damageRatio) {
	ItemStack stack = new ItemStack(ItemsTC.primordialPearl, 1, metadata);
	assertEquals(metadata != 0, stack.getItem().showDurabilityBar(stack));
	assertEquals(damageRatio, stack.getItem().getDurabilityForDisplay(stack));
    }

    @Test
    void testPearlNotDamageable() {
	ItemStack pearl = new ItemStack(ItemsTC.primordialPearl);
	assertFalse(pearl.isItemStackDamageable());
	assertTrue(pearl.getHasSubtypes());
    }

    static final List<Arguments> testAnvilHandlerIsDisabled = ImmutableList.of(
	    Arguments.of(ItemStack.EMPTY, ItemStack.EMPTY),
	    Arguments.of(new ItemStack(ItemsTC.primordialPearl), ItemStack.EMPTY),
	    Arguments.of(ItemStack.EMPTY, new ItemStack(ItemsTC.primordialPearl)),
	    Arguments.of(new ItemStack(ItemsTC.primordialPearl), new ItemStack(ItemsTC.primordialPearl))
	    );

    @ParameterizedTest
    @FieldSource
    void testAnvilHandlerIsDisabled(ItemStack left, ItemStack right) {
	AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, "test", 1) {
	    @Override
	    public void setCanceled(boolean cancel) {
		fail("Anvil event handler not disabled");
	    }
	};
	CraftingEvents.onAnvil(event);
    }

}
