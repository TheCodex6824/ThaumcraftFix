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

package thecodex6824.thaumcraftfix.testlib.lib;

import java.util.HashMap;
import java.util.Map;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class MockPlayer extends EntityPlayer {

    // TODO: use the normal CapabilityDispatcher instead (requires reflection)
    private final Map<Capability<?>, Object> caps;

    public MockPlayer(World world, GameProfile profile) {
	super(world, profile);
	caps = new HashMap<>();
    }

    @Override
    public boolean isCreative() {
	return true;
    }

    @Override
    public boolean isSpectator() {
	return false;
    }

    public <T> void addCapability(Capability<T> capability, T value) {
	caps.put(capability, value);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
	return caps.containsKey(capability);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
	if (hasCapability(capability, facing)) {
	    return capability.cast((T) caps.get(capability));
	}

	return null;
    }

}
