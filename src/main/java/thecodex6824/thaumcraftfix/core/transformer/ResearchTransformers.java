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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class ResearchTransformers {

    public static final Supplier<ITransformer> KNOWLEDGE_GAIN_EVENT_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ResearchManager",
				false,
				"addKnowledge",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"),
				Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/api/capabilities/IPlayerKnowledge",
			true,
			"addKnowledge",
			Type.BOOLEAN_TYPE,
			Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"), Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"sendKnowledgeGainPacket",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_PLAYER,
					Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"),
					Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> RESEARCH_GAIN_EVENT_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ResearchManager",
				false,
				"progressResearch",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Types.STRING, Type.BOOLEAN_TYPE
				)
			)
		.findConsecutive()
		.findNextLocalAccess(2)
		.findNextInstructionType(JumpInsnNode.class)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"sendResearchGainPacket",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_PLAYER, Types.STRING),
				false
				)
			)
		.build(), true, 1
		);
    };

}
