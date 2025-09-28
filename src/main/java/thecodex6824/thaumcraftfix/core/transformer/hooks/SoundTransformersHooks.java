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

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.container.ContainerFocalManipulator;
import thaumcraft.common.lib.SoundsTC;

public class SoundTransformersHooks {

    public static void fixupPlayerSound(EntityLivingBase player, SoundEvent sound, float volume, float pitch) {
	if (player instanceof EntityPlayerMP) {
	    // send the sound to the originating player, since the server won't do it
	    SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
	    ((EntityPlayerMP) player).connection.sendPacket(packet);
	}
    }

    private static Field containerFocalManipulatorTile;

    public static void fixupFocalManipulatorCraftFail(EntityPlayer player) throws Exception {
	if (player instanceof EntityPlayerMP) {
	    if (containerFocalManipulatorTile == null) {
		containerFocalManipulatorTile = ContainerFocalManipulator.class.getDeclaredField("table");
		containerFocalManipulatorTile.setAccessible(true);
	    }

	    TileEntity tile = ((TileEntity) containerFocalManipulatorTile.get(player.openContainer));
	    BlockPos pos = tile.getPos();
	    SPacketSoundEffect sound = new SPacketSoundEffect(SoundsTC.craftfail, SoundCategory.BLOCKS, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.33F, 1.0F);
	    ((EntityPlayerMP) player).connection.sendPacket(sound);
	}
    }

}
