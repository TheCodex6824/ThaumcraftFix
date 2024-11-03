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

package thecodex6824.thaumcraftfix.test.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumcraftfix.IProxy;

public class MockProxy implements IProxy {

    @Override
    public void construction() {}

    @Override
    public EntityPlayer getClientPlayer() {
	throw new UnsupportedOperationException();
    }

    @Override
    public File getGameDirectory() {
	return new File(".");
    }

    @Override
    public InputStream resolveResource(ResourceLocation loc) throws IOException {
	throw new UnsupportedOperationException();
    }

    @Override
    public void scheduleTask(Side intendedSide, Runnable task) {
	task.run();
    }

    @Override
    public boolean isServerRunning() {
	return true;
    }

}
