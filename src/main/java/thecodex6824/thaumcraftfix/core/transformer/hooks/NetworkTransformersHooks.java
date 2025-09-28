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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.common.container.ContainerFocalManipulator;
import thaumcraft.common.container.ContainerThaumatorium;
import thaumcraft.common.lib.network.misc.PacketLogisticsRequestToServer;
import thaumcraft.common.lib.network.misc.PacketNote;
import thaumcraft.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.core.transformer.custom.PacketNoteHandlerRewriteTransformer;

public class NetworkTransformersHooks {

    private static boolean validatePosition(EntityPlayerMP player, BlockPos target, String component) {
	if (!player.getEntityWorld().isBlockLoaded(target)) {
	    ThaumcraftFix.instance.getLogger().warn("Player {} ({}) sent {} for unloaded position {}",
		    player.getName(), player.getUniqueID().toString(), component, target.toString());
	    return false;
	}

	Vec3d start = player.getPositionEyes(1.0f);
	Vec3d end = new Vec3d(target).add(0.5, 0.5, 0.5);
	// we add 1 block extra to compensate for lag and so on
	double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;
	double distance = start.squareDistanceTo(end);
	if (distance > reach * reach) {
	    ThaumcraftFix.instance.getLogger().warn("Player {} ({}) sent {} for a position too far away. Pos = {}, distance = {}",
		    player.getName(), player.getUniqueID().toString(), component, target.toString(), Math.sqrt(distance));
	    return false;
	}

	return true;
    }

    private static Field logisticsRequestPosition;

    public static boolean validateLogisticsRequest(PacketLogisticsRequestToServer message, MessageContext ctx) throws Exception {
	if (logisticsRequestPosition == null) {
	    logisticsRequestPosition = PacketLogisticsRequestToServer.class.getDeclaredField("pos");
	    logisticsRequestPosition.setAccessible(true);
	}

	EntityPlayerMP player = ctx.getServerHandler().player;
	boolean ok = player.isEntityAlive();
	BlockPos pos = (BlockPos) logisticsRequestPosition.get(message);
	if (ok && pos != null) {
	    ok = validatePosition(player, pos, "logistics request");
	}

	return ok;
    }

    // this can be used by both client and server threads (and is created on network thread)
    // it will only be read after creation so volatile is ok
    private static volatile Method originalPacketNoteHandle;

    // these will only be used by server thread
    private static Field packetNoteDim;
    private static Field packetNoteX;
    private static Field packetNoteY;
    private static Field packetNoteZ;

    public static void handlePacketNote(PacketNote message, MessageContext ctx) throws Exception {
	if (originalPacketNoteHandle == null) {
	    originalPacketNoteHandle = PacketNote.class.getMethod(PacketNoteHandlerRewriteTransformer.ORIGINAL_METHOD_REDIRECT_NAME, PacketNote.class, MessageContext.class);
	}

	ThaumcraftFix.proxy.scheduleTask(ctx.side, () -> {
	    try {
		boolean ok = true;
		if (ctx.side == Side.SERVER) {
		    EntityPlayerMP player = ctx.getServerHandler().player;
		    if (packetNoteDim == null) {
			packetNoteDim = PacketNote.class.getDeclaredField("dim");
			packetNoteDim.setAccessible(true);
		    }

		    int dim = packetNoteDim.getInt(message);
		    if (dim != player.dimension) {
			ok = false;
			ThaumcraftFix.instance.getLogger().warn("Player {} ({}) tried to get note status of a dimension they weren't in. Player dim = {}, requested dim = {}",
				player.getName(), player.getUniqueID().toString(), player.dimension, dim);
		    }

		    if (ok) {
			if (packetNoteX == null) {
			    packetNoteX = PacketNote.class.getDeclaredField("x");
			    packetNoteX.setAccessible(true);
			    packetNoteY = PacketNote.class.getDeclaredField("y");
			    packetNoteY.setAccessible(true);
			    packetNoteZ = PacketNote.class.getDeclaredField("z");
			    packetNoteZ.setAccessible(true);
			}

			BlockPos pos = new BlockPos(packetNoteX.getInt(message), packetNoteY.getInt(message), packetNoteZ.getInt(message));
			ok = validatePosition(player, pos, "note status");
		    }
		}

		if (ok) {
		    originalPacketNoteHandle.invoke(message, message, ctx);
		}
	    }
	    catch (Exception ex) {
		// all of the checked exceptions are reflection related and there is no recovery
		throw new RuntimeException(ex);
	    }
	});
    }

    private static Field thaumatoriumRecipePosition;
    private static Field containerThaumatoriumTile;

    public static boolean validateThaumatoriumSelection(PacketSelectThaumotoriumRecipeToServer message, MessageContext ctx) throws Exception {
	if (thaumatoriumRecipePosition == null) {
	    thaumatoriumRecipePosition = PacketSelectThaumotoriumRecipeToServer.class.getDeclaredField("pos");
	    thaumatoriumRecipePosition.setAccessible(true);
	}

	EntityPlayerMP player = ctx.getServerHandler().player;
	boolean ok = player.isEntityAlive();
	BlockPos pos = BlockPos.fromLong(thaumatoriumRecipePosition.getLong(message));
	if (ok && pos != null) {
	    ok = validatePosition(player, pos, "thaumatorium recipe selection");
	    if (ok) {
		if (containerThaumatoriumTile == null) {
		    containerThaumatoriumTile = ContainerThaumatorium.class.getDeclaredField("thaumatorium");
		    containerThaumatoriumTile.setAccessible(true);
		}

		ok = player.openContainer instanceof ContainerThaumatorium &&
			((TileEntity) containerThaumatoriumTile.get(player.openContainer)).getPos().equals(pos);
	    }
	}

	return ok;
    }

    private static Field focusNodesPosition;
    private static Field containerFocalManipulatorTile;

    public static boolean validateFocalManipulatorNodeData(PacketFocusNodesToServer message, MessageContext ctx) throws Exception {
	if (focusNodesPosition == null) {
	    focusNodesPosition = PacketFocusNodesToServer.class.getDeclaredField("loc");
	    focusNodesPosition.setAccessible(true);
	}

	EntityPlayerMP player = ctx.getServerHandler().player;
	boolean ok = player.isEntityAlive();
	BlockPos pos = BlockPos.fromLong(focusNodesPosition.getLong(message));
	if (ok && pos != null) {
	    ok = validatePosition(player, pos, "focal manipulator node selections") &&
		    player.openContainer instanceof ContainerFocalManipulator;
	    if (ok) {
		if (containerFocalManipulatorTile == null) {
		    containerFocalManipulatorTile = ContainerFocalManipulator.class.getDeclaredField("table");
		    containerFocalManipulatorTile.setAccessible(true);
		}

		TileFocalManipulator tile = ((TileFocalManipulator) containerFocalManipulatorTile.get(player.openContainer));
		ok = tile.getPos().equals(pos) && tile.vis <= 0.0F;
	    }
	}

	return ok;
    }

    public static Set<String> filterResearchAids(Set<String> toFilter, EntityPlayer player, TileEntity table) {
	// just in case we somehow get passed an ImmutableSet / to not clobber the input set
	Set<String> ret = new HashSet<>(toFilter);
	// Thaumcraft already checked this was a research table
	ret.retainAll(((TileResearchTable) table).checkSurroundingAids());
	return ret;
    }

}
