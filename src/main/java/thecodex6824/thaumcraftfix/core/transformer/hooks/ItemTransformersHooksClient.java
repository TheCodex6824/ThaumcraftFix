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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.items.tools.ItemHandMirror;

@SideOnly(Side.CLIENT)
public class ItemTransformersHooksClient {

    public static int fixupXSlot(int original, InventoryPlayer playerInv) {
	ItemStack maybeMirror = playerInv.player.getHeldItemMainhand();
	if (!(maybeMirror.getItem() instanceof ItemHandMirror)) {
	    // so we don't have to check the slot every frame or have 2 patches,
	    // just send the X to the shadow realm if we don't want it displayed
	    original = -Minecraft.getMinecraft().displayWidth - 64;
	}

	return original;
    }

}
