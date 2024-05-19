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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class ItemTransformers {

    public static final Supplier<ITransformer> COMPARE_TAGS_RELAXED_NULL_CHECK = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/api/ThaumcraftInvHelper",
				false,
				"compareTagsRelaxed",
				Type.BOOLEAN_TYPE,
				Types.NBT_TAG_COMPOUND, Types.NBT_TAG_COMPOUND
				)
			)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"nullCheckTags",
				Type.getMethodDescriptor(Type.INT_TYPE, Types.NBT_TAG_COMPOUND, Types.NBT_TAG_COMPOUND),
				false
				),
			new InsnNode(Opcodes.DUP),
			new JumpInsnNode(Opcodes.IFEQ, newLabel),
			new InsnNode(Opcodes.ICONST_1),
			new InsnNode(Opcodes.IUSHR),
			new InsnNode(Opcodes.IRETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] { Opcodes.INTEGER }),
			new InsnNode(Opcodes.POP)
			)
		.build(), true, 1
		);
    };

    // to allow wildcard metadata in required research items when they are non-damageable
    public static final ITransformer CYCLE_ITEM_NON_DAMAGEABLE = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    "thaumcraft/common/lib/utils/InventoryUtils",
			    false,
			    "cycleItemStack",
			    Types.ITEM_STACK,
			    Types.OBJECT, Type.INT_TYPE
			    )
		    )
	    .findNextOpcode(Opcodes.ARETURN)
	    .insertInstructionsBefore(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ILOAD, 1),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    TransformUtil.HOOKS_COMMON,
			    "cycleItemStack",
			    Type.getMethodDescriptor(Types.ITEM_STACK, Types.ITEM_STACK, Types.OBJECT, Type.INT_TYPE),
			    false
			    )
		    )
	    .build()
	    );

    // makes runic shielding infusion work on items with baubles capability
    // TC only checks for the interface on the item...
    public static final ITransformer RUNIC_SHIELD_INFUSION_BAUBLE_CAP = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    "thaumcraft/common/lib/crafting/InfusionRunicAugmentRecipe",
			    false,
			    "matches",
			    Type.BOOLEAN_TYPE,
			    Type.getType("Ljava/util/List;"), Types.ITEM_STACK, Types.WORLD, Types.ENTITY_PLAYER
			    )
		    )
	    .findNext(node -> node.getOpcode() == Opcodes.INSTANCEOF && node instanceof TypeInsnNode && ((TypeInsnNode) node).desc.equals("baubles/api/IBauble") && node.getNext() instanceof JumpInsnNode)
	    .insertInstructions((node, matches) -> {
		InsnList toAdd = new InsnList();
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
		toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			TransformUtil.HOOKS_COMMON,
			"shouldAllowRunicShield",
			Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ITEM_STACK),
			false
			));
		toAdd.add(new JumpInsnNode(Opcodes.IFNE, ((JumpInsnNode) matches.get(0).matchStart().getNext()).label));

		ImmutableList<AbstractInsnNode> added = ImmutableList.copyOf(toAdd.iterator());
		node.instructions.insert(matches.get(0).matchStart().getNext(), toAdd);
		return added;
	    })
	    .build()
	    );

}
