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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.BaublesContainer;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.events.PlayerEvents;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.api.research.ResearchCategoryTheorycraftFilter;
import thecodex6824.thaumcraftfix.common.internal.DefaultApiImplementation;
import thecodex6824.thaumcraftfix.testlib.lib.MockPlayer;
import thecodex6824.thaumcraftfix.testlib.lib.MockWorld;

public class TestPlayerEvents {

    @Test
    @ResourceLock(value = TestConstants.RESOURCE_RESEARCH, mode = ResourceAccessMode.READ)
    void testHeadbandNormal() {
	assertDoesNotThrow(() -> {
	    MockWorld world = new MockWorld();
	    MockPlayer player = new MockPlayer(world, new GameProfile(UUID.randomUUID(), "test"));
	    player.addCapability(BaublesCapabilities.CAPABILITY_BAUBLES, new BaublesContainer() {
		@Override
		public ItemStack getStackInSlot(int slot) {
		    return new ItemStack(ItemsTC.bandCuriosity);
		}
	    });
	    EntityXPOrb orb = new EntityXPOrb(world, 0, 0, 0, 1000);
	    PlayerEvents.pickupXP(new PlayerPickupXpEvent(player, orb));
	});
    }

    @Test
    @ResourceLock(value = TestConstants.RESOURCE_RESEARCH, mode = ResourceAccessMode.READ_WRITE)
    void testHeadbandFilter() {
	Set<ResearchCategory> old = ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategories();
	DefaultApiImplementation impl = (DefaultApiImplementation) ThaumcraftFixApiBridge.implementation();
	try {
	    impl.setAllowedTheorycraftCategories(ImmutableSet.of());
	    // if there are no filtered categories to choose from, it will throw an IllegalArgumentException
	    // we can use this to determine if the filter is working by filtering out everything
	    assertThrows(IllegalArgumentException.class, () -> {
		MockWorld world = new MockWorld();
		MockPlayer player = new MockPlayer(world, new GameProfile(UUID.randomUUID(), "test"));
		player.addCapability(BaublesCapabilities.CAPABILITY_BAUBLES, new BaublesContainer() {
		    @Override
		    public ItemStack getStackInSlot(int slot) {
			return new ItemStack(ItemsTC.bandCuriosity);
		    }
		});
		EntityXPOrb orb = new EntityXPOrb(world, 0, 0, 0, 1000);
		PlayerEvents.pickupXP(new PlayerPickupXpEvent(player, orb));
	    });
	}
	finally {
	    impl.setAllowedTheorycraftCategories(ImmutableSet.copyOf(old));
	}
    }

}
