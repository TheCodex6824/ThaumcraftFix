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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;

import thecodex6824.coremodlib.ASMUtil;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.core.transformer.custom.ChangeVariableTypeTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class CastingTransformers {

    private static final String HOOKS = "thecodex6824/thaumcraftfix/core/transformer/hooks/CastingTransformersHooks";

    public static final ITransformer EXCHANGE_MOD_INTERFACEIFY = new ThrowingTransformerWrapper(
	    new ChangeVariableTypeTransformer(
		    new MethodDefinition(
			    "thaumcraft/common/items/casters/foci/FocusEffectExchange",
			    false,
			    "execute",
			    Type.BOOLEAN_TYPE,
			    Type.getType("Lnet/minecraft/util/math/RayTraceResult;"), Type.getType("Lthaumcraft/api/casters/Trajectory;"),
			    Type.FLOAT_TYPE, Type.INT_TYPE
			    ),
		    Types.ITEM_CASTER,
		    Types.I_CASTER,
		    true
		    ));

    public static final ITransformer FOCUS_PACKAGE_INIT = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    Types.FOCUS_PACKAGE.getInternalName(),
			    false,
			    "initialize",
			    Type.VOID_TYPE,
			    Types.ENTITY_LIVING_BASE
			    )
		    )
	    .findNextOpcode(Opcodes.RETURN)
	    .insertInstructionsBefore(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ALOAD, 1),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS,
			    "initializeFocusPackage",
			    Type.getMethodDescriptor(Type.VOID_TYPE, Types.FOCUS_PACKAGE, Types.ENTITY_LIVING_BASE),
			    false
			    )
		    )
	    .build()
	    );

    public static final ITransformer FOCUS_PACKAGE_SET_CASTER_UUID = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    Types.FOCUS_PACKAGE.getInternalName(),
			    false,
			    "setCasterUUID",
			    Type.VOID_TYPE,
			    Types.UUID
			    )
		    )
	    .findNextOpcode(Opcodes.RETURN)
	    .insertInstructionsBefore(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new InsnNode(Opcodes.ACONST_NULL),
		    new FieldDefinition(
			    Types.FOCUS_PACKAGE.getInternalName(),
			    "caster",
			    Types.ENTITY_LIVING_BASE
			    ).asFieldInsnNode(Opcodes.PUTFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ALOAD, 1),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS,
			    "setFocusPackageCasterUUID",
			    Type.getMethodDescriptor(Type.VOID_TYPE, Types.FOCUS_PACKAGE, Types.UUID),
			    false
			    )
		    )
	    .build()
	    );

    private static final MethodDefinition TOUCH_GET_PACKAGE = new MethodDefinition(
	    "thaumcraft/common/items/casters/foci/FocusMediumTouch",
	    false,
	    "getPackage",
	    Types.FOCUS_PACKAGE
	    );
    private static final MethodDefinition PACKAGE_GET_CASTER = new MethodDefinition(
	    Types.FOCUS_PACKAGE.getInternalName(),
	    false,
	    "getCaster",
	    Types.ENTITY_LIVING_BASE
	    );

    public static final ITransformer TOUCH_MOD_AVOID_PLAYER_CAST_TRAJECTORY = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(new MethodDefinition(
		    "thaumcraft/common/items/casters/foci/FocusMediumTouch",
		    false,
		    "supplyTrajectories",
		    Type.getType("[Lthaumcraft/api/casters/Trajectory;")
		    ))
	    .findConsecutive()
	    .findNextLocalAccess(0)
	    .findNextMethodCall(TOUCH_GET_PACKAGE)
	    .findNextMethodCall(PACKAGE_GET_CASTER)
	    .endConsecutive()
	    .insertInstructionsBefore(new LdcInsnNode(4.0))
	    .findConsecutive()
	    .findNextCheckCast(Types.ENTITY_PLAYER)
	    .findNextInstructionType(MethodInsnNode.class)
	    .findNextInstructionType(LabelNode.class)
	    .endConsecutive()
	    .insertInstructions((node, matches) -> {
		JumpInsnNode jump = new JumpInsnNode(Opcodes.IFEQ, (LabelNode) matches.get(matches.size() - 1).matchEnd());
		InsnList toAdd = ASMUtil.arrayToInsnList(
			new TypeInsnNode(Opcodes.INSTANCEOF, Types.ENTITY_PLAYER.getInternalName()),
			jump,
			new InsnNode(Opcodes.POP2),
			new VarInsnNode(Opcodes.ALOAD, 0),
			TOUCH_GET_PACKAGE.asMethodInsnNode(Opcodes.INVOKEVIRTUAL),
			PACKAGE_GET_CASTER.asMethodInsnNode(Opcodes.INVOKEVIRTUAL)
			);
		ImmutableList<AbstractInsnNode> added = ImmutableList.copyOf(toAdd.iterator());
		node.instructions.insertBefore(matches.get(matches.size() - 1).matchStart(), toAdd);
		return added;
	    })
	    .build(), true, 1);

    public static final ITransformer TOUCH_MOD_AVOID_PLAYER_CAST_TARGET = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(new MethodDefinition(
		    "thaumcraft/common/items/casters/foci/FocusMediumTouch",
		    false,
		    "supplyTargets",
		    Type.getType("[Lnet/minecraft/util/math/RayTraceResult;")
		    ))
	    .findNextInstanceOf(Types.ENTITY_PLAYER)
	    .insertInstructionsAfter(
		    new InsnNode(Opcodes.POP),
		    new InsnNode(Opcodes.ICONST_1)
		    )
	    .findConsecutive()
	    .findNextLocalAccess(0)
	    .findNextMethodCall(TOUCH_GET_PACKAGE)
	    .findNextMethodCall(PACKAGE_GET_CASTER)
	    .endConsecutive()
	    .insertInstructionsBefore(new LdcInsnNode(4.0))
	    .findConsecutive()
	    .findNextCheckCast(Types.ENTITY_PLAYER)
	    .findNextInstructionType(MethodInsnNode.class)
	    .findNextInstructionType(LabelNode.class)
	    .endConsecutive()
	    .insertInstructions((node, matches) -> {
		JumpInsnNode jump = new JumpInsnNode(Opcodes.IFEQ, (LabelNode) matches.get(matches.size() - 1).matchEnd());
		InsnList toAdd = ASMUtil.arrayToInsnList(
			new TypeInsnNode(Opcodes.INSTANCEOF, Types.ENTITY_PLAYER.getInternalName()),
			jump,
			new InsnNode(Opcodes.POP2),
			new VarInsnNode(Opcodes.ALOAD, 0),
			TOUCH_GET_PACKAGE.asMethodInsnNode(Opcodes.INVOKEVIRTUAL),
			PACKAGE_GET_CASTER.asMethodInsnNode(Opcodes.INVOKEVIRTUAL)
			);
		ImmutableList<AbstractInsnNode> added = ImmutableList.copyOf(toAdd.iterator());
		node.instructions.insertBefore(matches.get(matches.size() - 1).matchStart(), toAdd);
		return added;
	    })
	    .build(), true, 1);

}
