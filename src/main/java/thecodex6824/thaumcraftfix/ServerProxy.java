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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;

public class ServerProxy implements IProxy {

    @Override
    public void construction() {}

    @Override
    public void scheduleTask(Side intendedSide, Runnable task) {
	if (intendedSide == Side.CLIENT) {
	    throw new IllegalArgumentException("Cannot run task intended for client side on a dedicated server");
	}

	FMLServerHandler.instance().getServer().addScheduledTask(task);
    }

    @Override
    public EntityPlayer getClientPlayer() {
	throw new UnsupportedOperationException("Can't get client player on dedicated server");
    }

    @Override
    public File getGameDirectory() {
	return FMLServerHandler.instance().getServer().getDataDirectory();
    }

    @Override
    public InputStream resolveResource(ResourceLocation loc) throws IOException {
	return ServerProxy.class.getResourceAsStream("/assets/" + loc.getNamespace() + "/" + loc.getPath());
    }

}
