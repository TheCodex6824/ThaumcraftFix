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

package thecodex6824.thaumcraftfix.common.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class NoEquipSoundFakePlayer extends FakePlayer {

    public NoEquipSoundFakePlayer(WorldServer world, GameProfile profile) {
	super(world, profile);
    }

    @Override
    public void setHeldItem(EnumHand hand, ItemStack stack) {
	switch (hand) {
	case MAIN_HAND:
	    inventory.mainInventory.set(inventory.currentItem, stack);
	    break;
	case OFF_HAND:
	    inventory.offHandInventory.set(0, stack);
	    break;
	default: throw new IllegalArgumentException("Invalid hand " + hand);
	}
    }

}
