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

public class BlockTransformers {

    public static final ITransformer FOCAL_MANIPULATOR_BLACK_FOCUS_GLITCH = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    TransformUtil.remapMethod(new MethodDefinition(
			    Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			    false,
			    "func_70299_a",
			    Type.VOID_TYPE,
			    Type.INT_TYPE, Types.ITEM_STACK
			    )
			    ))
	    .findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
		    "net/minecraft/item/ItemStack",
		    false,
		    "func_190926_b",
		    Type.BOOLEAN_TYPE
		    )))
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ALOAD, 3),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    TransformUtil.HOOKS_COMMON,
			    "shouldFocalManipulatorClearState",
			    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.TILE_FOCAL_MANIPULATOR, Types.ITEM_STACK),
			    false
			    )
		    )
	    .findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
		    "net/minecraft/item/ItemStack",
		    false,
		    "func_77989_b",
		    Type.BOOLEAN_TYPE,
		    Types.ITEM_STACK, Types.ITEM_STACK
		    )))
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ALOAD, 3),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    TransformUtil.HOOKS_COMMON,
			    "shouldFocalManipulatorClearStateInverted",
			    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.TILE_FOCAL_MANIPULATOR, Types.ITEM_STACK),
			    false
			    )
		    )
	    .build(), true, 1
	    );

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_MAX_COMPLEXITY = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				false,
				"startCraft",
				Type.BOOLEAN_TYPE,
				Type.INT_TYPE, Types.ENTITY_PLAYER
				)
			)
		.findConsecutive()
		.findNextOpcode(Opcodes.I2F)
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"vis",
			Type.FLOAT_TYPE)
			)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"checkFocusComplexity",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.TILE_FOCAL_MANIPULATOR, Types.ENTITY_PLAYER, Type.INT_TYPE, Type.INT_TYPE),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, newLabel),
			new InsnNode(Opcodes.ICONST_0),
			new InsnNode(Opcodes.IRETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)
		.build(), true, 1
		);
    };

    // makes focal manipulator not require any materials in creative mode
    public static final ITransformer FOCAL_MANIPULATOR_COMPONENTS = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			    false,
			    "startCraft",
			    Type.BOOLEAN_TYPE,
			    Type.INT_TYPE, Types.ENTITY_PLAYER
			    )
		    )
	    .findConsecutive()
	    .findNextLocalAccess(6)
	    .findNextOpcode(Opcodes.ARRAYLENGTH)
	    .endConsecutive()
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new InsnNode(Opcodes.DUP),
		    new FieldDefinition(
			    Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			    "crystals",
			    Type.getType("Lthaumcraft/api/aspects/AspectList;")
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 2),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    TransformUtil.HOOKS_COMMON,
			    "modifyManipulatorComponentCount",
			    Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.TILE_FOCAL_MANIPULATOR,
				    Type.getType("Lthaumcraft/api/aspects/AspectList;"), Types.ENTITY_PLAYER),
			    false
			    )
		    )
	    .build(), true, 1
	    );

    private static final String GUI_MANIPULATOR_CLASS = "thaumcraft/client/gui/GuiFocalManipulator";

    public static final ITransformer FOCAL_MANIPULATOR_COMPONENTS_CLIENT = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    GUI_MANIPULATOR_CLASS,
			    false,
			    "gatherInfo",
			    Type.VOID_TYPE,
			    Type.BOOLEAN_TYPE
			    )
		    )
	    .findNextFieldAccess(new FieldDefinition(
		    GUI_MANIPULATOR_CLASS,
		    "valid",
		    Type.BOOLEAN_TYPE
		    ))
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new InsnNode(Opcodes.DUP),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "valid",
			    Type.BOOLEAN_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "totalComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "maxComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "table",
			    Types.TILE_FOCAL_MANIPULATOR
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ILOAD, 4),
		    new VarInsnNode(Opcodes.ILOAD, 6),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    TransformUtil.HOOKS_CLIENT,
			    "modifyFocalManipulatorCraftValid",
			    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE,
				    Types.TILE_FOCAL_MANIPULATOR, Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE),
			    false
			    ),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "valid",
			    Type.BOOLEAN_TYPE
			    ).asFieldInsnNode(Opcodes.PUTFIELD)
		    )
	    .build()
	    );

    public static final Supplier<ITransformer> TABLE_TOP_SOLID = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/basic/BlockTable",
				false,
				"func_193383_a",
				Types.BLOCK_FACE_SHAPE,
				Types.I_BLOCK_ACCESS, Types.I_BLOCK_STATE,
				Types.BLOCK_POS, Types.ENUM_FACING

				)
				))
		.findNextOpcode(Opcodes.ARETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"getTableBlockFaceShape",
				Type.getMethodDescriptor(Types.BLOCK_FACE_SHAPE, Types.BLOCK_FACE_SHAPE, Types.ENUM_FACING),
				false
				)
			)
		.build()
		);
    };

}
