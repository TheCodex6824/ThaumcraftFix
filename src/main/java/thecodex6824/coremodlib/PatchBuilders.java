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

package thecodex6824.coremodlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableList;

import thecodex6824.coremodlib.PatchStateMachine.InstructionStateMachineNode;

public class PatchBuilders {

    @SuppressWarnings("unchecked")
    private static abstract class MatchBuilder<T extends MatchBuilder<?>> {

	protected ArrayList<InstructionMatcher> matchers;

	public MatchBuilder() {
	    matchers = new ArrayList<>();
	}

	public T findAny() {
	    matchers.add(new PrefabMatchers.AlwaysMatch());
	    return (T) this;
	}

	public T findNext(Function<AbstractInsnNode, Boolean> matcher) {
	    matchers.add(new PrefabMatchers.GenericMatch(matcher));
	    return (T) this;
	}

	public T findNextOpcode(int opcode) {
	    matchers.add(new PrefabMatchers.OpcodeMatch(opcode));
	    return (T) this;
	}

	public T findNextInstructionType(Class<? extends AbstractInsnNode> clazz) {
	    matchers.add(new PrefabMatchers.ClassMatch(clazz));
	    return (T) this;
	}

	public T findNextLocalAccess(int index) {
	    matchers.add(new PrefabMatchers.LocalVariableMatchByIndex(index));
	    return (T) this;
	}

	public T findNextLocalAccess(LocalVariableDefinition local) {
	    matchers.add(new PrefabMatchers.LocalVariableMatchByDefinition(local));
	    return (T) this;
	}

	public T findNextFieldAccess(FieldDefinition fieldDef) {
	    matchers.add(new PrefabMatchers.FieldMatch(fieldDef));
	    return (T) this;
	}

	public T findNextFieldAccessOfType(Type fieldType) {
	    matchers.add(new PrefabMatchers.FieldTypeMatch(fieldType));
	    return (T) this;
	}

	public T findNextMethodCall(MethodDefinition methodDef) {
	    matchers.add(new PrefabMatchers.MethodCallMatch(methodDef));
	    return (T) this;
	}

	public T findNextCheckCast(Type castDesc) {
	    matchers.add(new PrefabMatchers.TypeInsnMatch(Opcodes.CHECKCAST, castDesc));
	    return (T) this;
	}

	public T findNextInstanceOf(Type castDesc) {
	    matchers.add(new PrefabMatchers.TypeInsnMatch(Opcodes.INSTANCEOF, castDesc));
	    return (T) this;
	}
    }

    public static class ConsecutiveMatchBuilder extends MatchBuilder<ConsecutiveMatchBuilder> {

	private TransformerBuilder parent;

	protected ConsecutiveMatchBuilder(TransformerBuilder builder) {
	    parent = builder;
	}

	public TransformerBuilder endConsecutive() {
	    parent.matchers.add(new PrefabMatchers.ConsecutiveMatch(matchers));
	    return parent;
	}

    }

    public static class SurroundingActionBuilder {

	private TransformerBuilder parent;
	private InsnList beforeList;
	private InsnList afterList;

	protected SurroundingActionBuilder(TransformerBuilder builder) {
	    parent = builder;
	}

	public SurroundingActionBuilder before(AbstractInsnNode... toInsert) {
	    return before(ASMUtil.arrayToInsnList(toInsert));
	}

	public SurroundingActionBuilder before(InsnList toInsert) {
	    beforeList = toInsert;
	    return this;
	}

	public SurroundingActionBuilder after(AbstractInsnNode... toInsert) {
	    return after(ASMUtil.arrayToInsnList(toInsert));
	}

	public SurroundingActionBuilder after(InsnList toInsert) {
	    afterList = toInsert;
	    return this;
	}

	public TransformerBuilder endAction() {
	    if (beforeList == null || afterList == null) {
		throw new IllegalStateException("Cannot build surrounding action without calling before() and after()");
	    }

	    parent.getActionList().add(new PrefabMatchActions.InsertInstructionsSurroundingMatch(beforeList, afterList));
	    return parent;
	}

    }

    public static class TransformerBuilder extends MatchBuilder<TransformerBuilder> {

	private MethodDefinition method;
	private Map<InstructionMatcher, List<MatchAction>> matchToActions;

	public TransformerBuilder(MethodDefinition targetMethod) {
	    super();
	    method = targetMethod;
	    matchToActions = new IdentityHashMap<>();
	}

	private List<MatchAction> getActionList() {
	    if (matchers.isEmpty()) {
		matchers.add(new PrefabMatchers.AlwaysMatch());
	    }

	    return matchToActions.computeIfAbsent(matchers.get(matchers.size() - 1), m -> new ArrayList<>());
	}

	public ConsecutiveMatchBuilder findConsecutive() {
	    return new ConsecutiveMatchBuilder(this);
	}

	public TransformerBuilder insertInstructionsBefore(AbstractInsnNode... toInsert) {
	    return insertInstructionsBefore(ASMUtil.arrayToInsnList(toInsert));
	}

	public TransformerBuilder insertInstructionsBefore(InsnList toInsert) {
	    getActionList().add(new PrefabMatchActions.InsertInstructionsBeforeMatch(toInsert));
	    return this;
	}

	public TransformerBuilder insertInstructionsAfter(AbstractInsnNode... toInsert) {
	    return insertInstructionsAfter(ASMUtil.arrayToInsnList(toInsert));
	}

	public TransformerBuilder insertInstructionsAfter(InsnList toInsert) {
	    getActionList().add(new PrefabMatchActions.InsertInstructionsAfterMatch(toInsert));
	    return this;
	}

	public SurroundingActionBuilder insertInstructionsSurrounding() {
	    return new SurroundingActionBuilder(this);
	}

	public TransformerBuilder insertInstructions(BiFunction<MethodNode, List<? extends MatchDetails>, Collection<AbstractInsnNode>> inserter) {
	    getActionList().add(new PrefabMatchActions.GenericAction(inserter));
	    return this;
	}

	public PatchStateMachine build() {
	    ImmutableList.Builder<InstructionStateMachineNode> nodeBuilder = ImmutableList.builder();
	    for (InstructionMatcher matcher : matchers) {
		nodeBuilder.add(new InstructionStateMachineNode(matcher, matchToActions.getOrDefault(matcher, Collections.emptyList())));
	    }

	    return new PatchStateMachine(method, nodeBuilder.build());
	}

    }

}
