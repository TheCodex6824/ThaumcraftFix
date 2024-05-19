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

import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class NetworkTransformers {

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
				TransformUtil.HOOKS_COMMON,
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

    public static final Supplier<ITransformer> NOTE_HANDLER = () -> new PacketNoteHandlerRewriteTransformer();

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
				TransformUtil.HOOKS_COMMON,
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
				TransformUtil.HOOKS_COMMON,
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

}
