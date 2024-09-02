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
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLContainerHolder;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

public class ClientProxy implements IProxy {

    @Override
    public void construction() {
	// Forge does not sort the resource pack using mod dependencies
	// instead, it is just the order the mods are loaded
	// this means that in dev (where the mod is a folder and not jar) or if the jar is renamed,
	// our resources will *not* override Thaumcraft
	// this fixes that by moving our position in the list to override Thaumcraft if needed
	List<IResourcePack> packs = Minecraft.getMinecraft().defaultResourcePacks;
	int thaumcraftIndex = -1;
	int fixIndex = -1;
	for (int i = 0; i < packs.size(); ++i) {
	    IResourcePack pack = packs.get(i);
	    if (pack instanceof FMLContainerHolder) {
		ModContainer container = ((FMLContainerHolder) pack).getFMLContainer();
		if (container.getModId().equals("thaumcraft")) {
		    thaumcraftIndex = i;
		}
		else if (container.getModId().equals(ThaumcraftFixApi.MODID)) {
		    fixIndex = i;
		}

		if (thaumcraftIndex != -1 && fixIndex != -1) {
		    break;
		}
	    }
	}

	if (thaumcraftIndex != -1 && fixIndex != -1 && thaumcraftIndex > fixIndex) {
	    IResourcePack fix = packs.remove(fixIndex);
	    // everything after tc fix will have shifted left by 1, so thaumcraftIndex is now 1 after
	    // thaumcraftIndex == new list size is also acceptable here
	    packs.add(thaumcraftIndex, fix);
	}
    }

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

    @Override
    public File getGameDirectory() {
	return Minecraft.getMinecraft().gameDir;
    }

    @Override
    public InputStream resolveResource(ResourceLocation loc) throws IOException {
	return Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
    }

}
