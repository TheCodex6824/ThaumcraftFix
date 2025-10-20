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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.LocalVariableDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.core.transformer.custom.BlockApplyOffsetTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class BlockTransformers {

    private static final String HOOKS_BASE = "thecodex6824/thaumcraftfix/core/transformer/hooks/BlockTransformersHooks";
    private static final String HOOKS_COMMON = HOOKS_BASE + "Common";
    private static final String HOOKS_CLIENT = HOOKS_BASE + "Client";

    public static final Supplier<ITransformer> ARCANE_WORKBENCH_NO_CONCURRENT_USE = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/crafting/BlockArcaneWorkbench",
				false,
				"func_180639_a",
				Type.BOOLEAN_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY_PLAYER,
				Types.ENUM_HAND, Types.ENUM_FACING, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE
				)))
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.WORLD.getInternalName(),
			"field_72995_K",
			Type.BOOLEAN_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isArcaneWorkbenchNotAllowed",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.WORLD, Types.BLOCK_POS, Types.ENTITY_PLAYER),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> ARCANE_WORKBENCH_NO_CONCURRENT_USE_CHARGER = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/crafting/BlockArcaneWorkbenchCharger",
				false,
				"func_180639_a",
				Type.BOOLEAN_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY_PLAYER,
				Types.ENUM_HAND, Types.ENUM_FACING, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE
				)))
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.WORLD.getInternalName(),
			"field_72995_K",
			Type.BOOLEAN_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isArcaneWorkbenchNotAllowedForCharger",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.WORLD, Types.BLOCK_POS, Types.ENTITY_PLAYER),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> BRAIN_JAR_EAT_DELAY = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/devices/TileJarBrain",
				false,
				"func_73660_a",
				Type.VOID_TYPE
				)
				))
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition(
			"ents",
			Types.LIST
			))
		.findNextMethodCall(new MethodDefinition(
			"java/util/List",
			true,
			"iterator",
			Types.ITERATOR
			))
		.findNextOpcode(Opcodes.ASTORE)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new LdcInsnNode(10),
			new FieldDefinition(
				"thaumcraft/common/tiles/devices/TileJarBrain",
				"eatDelay",
				Type.INT_TYPE
				).asFieldInsnNode(Opcodes.PUTFIELD)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_FOCUS_SLOT = () -> {
	FieldDefinition vis = new FieldDefinition(
		Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
		"vis",
		Type.FLOAT_TYPE
		);
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				false,
				"func_70299_a",
				Type.VOID_TYPE,
				Type.INT_TYPE, Types.ITEM_STACK
				)
				))
		.findNextFieldAccess(vis)
		.insertInstructionsBefore(
			new InsnNode(Opcodes.POP),
			new VarInsnNode(Opcodes.ALOAD, 0),
			vis.asFieldInsnNode(Opcodes.GETFIELD)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_SERVER_CHECKS = () -> {
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
				HOOKS_COMMON,
				"checkFocus",
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
			    HOOKS_COMMON,
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
			    HOOKS_CLIENT,
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

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_EXCLUSIVE_NODES_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				GUI_MANIPULATOR_CLASS,
				false,
				"gatherPartsList",
				Type.VOID_TYPE
				)
			)
		.findConsecutive()
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"data",
			Types.HASH_MAP
			))
		.findNextMethodCall(new MethodDefinition(
			Types.HASH_MAP.getInternalName(),
			false,
			"values",
			Types.COLLECTION
			))
		.findNextMethodCall(new MethodDefinition(
			Types.COLLECTION.getInternalName(),
			true,
			"iterator",
			Types.ITERATOR
			))
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				GUI_MANIPULATOR_CLASS,
				"table",
				Types.TILE_FOCAL_MANIPULATOR
				).asFieldInsnNode(Opcodes.GETFIELD),
			new FieldDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				"data",
				Types.HASH_MAP
				).asFieldInsnNode(Opcodes.GETFIELD),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				GUI_MANIPULATOR_CLASS,
				"selectedNode",
				Type.INT_TYPE
				).asFieldInsnNode(Opcodes.GETFIELD),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getNodesInTree",
				Type.getMethodDescriptor(Types.ITERATOR, Types.ITERATOR, Types.HASH_MAP, Type.INT_TYPE),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_VIS_FP_ISSUES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			false,
			"func_73660_a",
			Type.VOID_TYPE
			)))
		.findConsecutive()
		.findNextLocalAccess(2)
		.findNextOpcode(Opcodes.FSUB)
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"vis",
			Type.FLOAT_TYPE
			))
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsBefore(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"handleFocalManipulatorVis",
				Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE),
				false
				)
			)
		.build()
		);
    };

    public static final ITransformer FOCAL_MANIPULATOR_XP_COST_GUI = new GenericStateMachineTransformer(
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
		    "costXp",
		    Type.INT_TYPE
		    ))
	    .insertInstructionsBefore(
		    new InsnNode(Opcodes.POP),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "totalComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_COMMON,
			    "recalcManipulatorXpCost",
			    Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE),
			    false
			    )
		    )
	    .build()
	    );

    public static final Supplier<ITransformer> INFERNAL_FURNACE_DESTROY_EFFECTS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/devices/TileInfernalFurnace",
				false,
				"destroyItem",
				Type.VOID_TYPE
				)))
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"destroyItemEffectsThatActuallyWork",
				Type.VOID_TYPE,
				Type.getType("Lthaumcraft/common/tiles/devices/TileInfernalFurnace;")
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> INFERNAL_FURNACE_ITEM_CHECKS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/devices/BlockInfernalFurnace",
				false,
				"func_180634_a",
				Type.VOID_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY
				)))
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/tiles/devices/TileInfernalFurnace",
			false,
			"addItemsToInventory",
			Types.ITEM_STACK,
			Types.ITEM_STACK
			))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"passStackToFurnace",
				Types.ITEM_STACK,
				Types.ITEM_STACK, Types.ENTITY
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ENTITY_ITEM.getInternalName(),
			false,
			"func_92058_a",
			Type.VOID_TYPE,
			Types.ITEM_STACK
			)))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"getStackToSet",
				Types.ITEM_STACK,
				Types.ITEM_STACK, Types.ENTITY
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> PLANT_CINDERPEARL_OFFSET = () -> new ThrowingTransformerWrapper(
	    new BlockApplyOffsetTransformer("thaumcraft/common/blocks/world/plants/BlockPlantCinderpearl"));

    public static final Supplier<ITransformer> PLANT_SHIMMERLEAF_OFFSET = () -> new ThrowingTransformerWrapper(
	    new BlockApplyOffsetTransformer("thaumcraft/common/blocks/world/plants/BlockPlantShimmerleaf"));

    public static final Supplier<ITransformer> RESEARCH_TABLE_SHIFT_CLICK = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/crafting/TileResearchTable",
				false,
				"func_94041_b",
				Type.BOOLEAN_TYPE,
				Type.INT_TYPE, Types.ITEM_STACK
				)
				))
		.findNextOpcode(Opcodes.IRETURN)
		.findNextOpcode(Opcodes.IRETURN)
		.findNextOpcode(Opcodes.IRETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ILOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isResearchTableItemValidForSlot",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Type.INT_TYPE),
				false
				)
			)
		.build()
		);
    };

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
				HOOKS_COMMON,
				"getTableBlockFaceShape",
				Type.getMethodDescriptor(Types.BLOCK_FACE_SHAPE, Types.BLOCK_FACE_SHAPE, Types.ENUM_FACING),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> THAUMATORIUM_TOP_EMPTY = () -> {
	LabelNode jumpTarget = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/crafting/TileThaumatoriumTop",
				false,
				"func_191420_l",
				Type.BOOLEAN_TYPE
				)
				))
		.findConsecutive()
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/tiles/crafting/TileThaumatorium",
			false,
			"func_191420_l",
			Type.BOOLEAN_TYPE
			)))
		.findNextOpcode(Opcodes.IRETURN)
		.endConsecutive()
		.insertInstructionsSurrounding()
		.before(
			new InsnNode(Opcodes.DUP),
			new JumpInsnNode(Opcodes.IFNULL, jumpTarget)
			)
		.after(
			jumpTarget,
			new FrameNode(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "thaumcraft/common/tiles/crafting/TileThaumatorium" }),
			new InsnNode(Opcodes.POP),
			new InsnNode(Opcodes.ICONST_1),
			new InsnNode(Opcodes.IRETURN)
			)
		.endAction()
		.build(), true, 1
		);
    };

}
