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

package thecodex6824.thaumcraftfix;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy implements IProxy {

    @Override
    public void scheduleTask(Side intendedSide, Runnable task) {
	Minecraft mc = Minecraft.getMinecraft();
	if (intendedSide == Side.SERVER) {
	    if (mc.getIntegratedServer() == null) {
		throw new IllegalArgumentException("Cannot run task on server when running a dedicated client");
	    }

	    mc.getIntegratedServer().addScheduledTask(task);
	}
	else {
	    mc.addScheduledTask(task);
	}
    }

    @Override
    public EntityPlayer getClientPlayer() {
	return Minecraft.getMinecraft().player;
    }

}
