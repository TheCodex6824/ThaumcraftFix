/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

@SideOnly(Side.CLIENT)
public class BlockTransformersHooksClient {

    public static boolean modifyFocalManipulatorCraftValid(boolean origResult, int totalComplexity,
	    int maxComplexity, TileFocalManipulator table, boolean emptyNodes, boolean validCrystals) {

	EntityPlayer player = Minecraft.getMinecraft().player;
	if (!player.isCreative()) {
	    return origResult;
	}

	// waive crystal and xp requirement in creative
	return totalComplexity <= maxComplexity && !emptyNodes;
    }

}
