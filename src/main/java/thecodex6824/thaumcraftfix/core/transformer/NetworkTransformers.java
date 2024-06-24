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

package thecodex6824.thaumcraftfix.core.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.common.container.ContainerFocalManipulator;
import thaumcraft.common.container.ContainerThaumatorium;
import thaumcraft.common.lib.network.misc.PacketLogisticsRequestToServer;
import thaumcraft.common.lib.network.misc.PacketNote;
import thaumcraft.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.core.transformer.custom.PacketNoteHandlerRewriteTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class NetworkTransformers {

    public static final class Hooks {

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

	public static int checkProgressSyncStage(int originalStage, EntityPlayer player, ResearchEntry entry) {
	    int logicStage = originalStage;
	    if (logicStage < 0 && ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
		logicStage = Integer.MAX_VALUE;
	    }

	    return logicStage;
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_DATA = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/playerdata/PacketFocusNodesToServer$1",
				false,
				"run",
				Type.VOID_TYPE
				)
			)
		.findAny()
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/playerdata/PacketFocusNodesToServer$1",
				"val$message",
				Type.getType("Lthaumcraft/common/lib/network/playerdata/PacketFocusNodesToServer;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/playerdata/PacketFocusNodesToServer$1",
				"val$ctx",
				Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"validateFocalManipulatorNodeData",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					Type.getType("Lthaumcraft/common/lib/network/playerdata/PacketFocusNodesToServer;"),
					Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, newLabel),
			new InsnNode(Opcodes.RETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)

		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> LOGISTICS_REQUEST = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/misc/PacketLogisticsRequestToServer$1",
				false,
				"run",
				Type.VOID_TYPE
				)
			)
		.findAny()
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/misc/PacketLogisticsRequestToServer$1",
				"val$message",
				Type.getType("Lthaumcraft/common/lib/network/misc/PacketLogisticsRequestToServer;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/misc/PacketLogisticsRequestToServer$1",
				"val$ctx",
				Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"validateLogisticsRequest",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					Type.getType("Lthaumcraft/common/lib/network/misc/PacketLogisticsRequestToServer;"),
					Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, newLabel),
			new InsnNode(Opcodes.RETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)

		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> NOTE_HANDLER = () -> new ThrowingTransformerWrapper(
	    new PacketNoteHandlerRewriteTransformer());

    public static final Supplier<ITransformer> RESEARCH_TABLE_AIDS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/misc/PacketStartTheoryToServer$1",
				false,
				"run",
				Type.VOID_TYPE
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/lib/network/misc/PacketStartTheoryToServer",
			false,
			"access$100",
			Types.SET,
			Type.getType("Lthaumcraft/common/lib/network/misc/PacketStartTheoryToServer;")
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"filterResearchAids",
				Type.getMethodDescriptor(Types.SET, Types.SET, Types.ENTITY_PLAYER, Types.TILE_ENTITY),
				false
				)
			)

		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> THAUMATORIUM_RECIPE_SELECTION = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer$1",
				false,
				"run",
				Type.VOID_TYPE
				)
			)
		.findNextInstanceOf(Types.ENTITY_PLAYER)
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer$1",
				"val$message",
				Type.getType("Lthaumcraft/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				"thaumcraft/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer$1",
				"val$ctx",
				Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")
				).asFieldInsnNode(Opcodes.GETFIELD),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"validateThaumatoriumSelection",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					Type.getType("Lthaumcraft/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer;"),
					Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")),
				false
				),
			new InsnNode(Opcodes.IAND)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> PROGRESS_SYNC_CHECKS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/playerdata/PacketSyncProgressToServer$1",
				false,
				"run",
				Type.VOID_TYPE
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/lib/network/playerdata/PacketSyncProgressToServer",
			false,
			"access$200",
			Type.BOOLEAN_TYPE,
			Type.getType("Lthaumcraft/common/lib/network/playerdata/PacketSyncProgressToServer;")
			))
		.insertInstructionsAfter(
			new InsnNode(Opcodes.POP),
			new InsnNode(Opcodes.ICONST_1)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> PROGRESS_SYNC_REQS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/network/playerdata/PacketSyncProgressToServer",
				false,
				"checkRequisites",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Types.STRING
				)
			)
		.findConsecutive()
		.findNextOpcode(Opcodes.ICONST_1)
		.findNextOpcode(Opcodes.ISUB)
		.findNextLocalAccess(4)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ILOAD, 4),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 3),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"checkProgressSyncStage",
				Type.getMethodDescriptor(Type.INT_TYPE,
					Type.INT_TYPE, Types.ENTITY_PLAYER,
					Type.getType("Lthaumcraft/api/research/ResearchEntry;")),
				false
				),
			new VarInsnNode(Opcodes.ISTORE, 4)
			)
		.build()
		);
    };

}
