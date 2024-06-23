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

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;

import com.google.common.collect.ImmutableList;

class PrefabMatchers {

    public static class AlwaysMatch implements InstructionMatcher {

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    return MatchResult.matchSingleNode(node);
	}

	@Override
	public String toString() {
	    return "Always matches any and every input node";
	}

    }

    public static class GenericMatch implements InstructionMatcher {

	private final Function<AbstractInsnNode, Boolean> matcher;

	public GenericMatch(Function<AbstractInsnNode, Boolean> matcher) {
	    this.matcher = matcher;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    return matcher.apply(node) ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return "Generic node, no details :(";
	}

    }

    public static class ConsecutiveMatch implements InstructionMatcher {

	private final ImmutableList<InstructionMatcher> matchers;

	public ConsecutiveMatch(List<InstructionMatcher> matchers) {
	    this.matchers = ImmutableList.copyOf(matchers);
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    AbstractInsnNode current = node;
	    for (InstructionMatcher matcher : matchers) {
		if (current == null || !matcher.matches(method, current).matched()) {
		    return MatchResult.noMatch();
		}

		current = current.getNext();
	    }

	    return MatchResult.matchNodeRange(node, current.getPrevious());
	}

	@Override
	public String toString() {
	    StringJoiner output = new StringJoiner(System.lineSeparator());
	    for (InstructionMatcher matcher : matchers) {
		output.add("\t" + matcher.toString());
	    }

	    return String.format("Nodes must consecutively match:%n%s", output.toString());
	}

    }

    public static class OpcodeMatch implements InstructionMatcher {

	private final int opcode;

	public OpcodeMatch(int opcode) {
	    this.opcode = opcode;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    return node.getOpcode() == opcode ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node has an opcode of %s", Textifier.OPCODES[opcode]);
	}

    }

    public static class ClassMatch implements InstructionMatcher {

	private final Class<? extends AbstractInsnNode> cls;

	public ClassMatch(Class<? extends AbstractInsnNode> nodeClass) {
	    cls = nodeClass;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    return cls.isInstance(node) ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is an instance of %s", cls.getTypeName());
	}

    }

    public static class LocalVariableMatchByIndex implements InstructionMatcher {

	private final int index;

	public LocalVariableMatchByIndex(int varIndex) {
	    index = varIndex;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof VarInsnNode) {
		ret = ((VarInsnNode) node).var == index;
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is accessing local variable at index %d", index);
	}

    }

    public static class LocalVariableMatchByDefinition implements InstructionMatcher {

	private final LocalVariableDefinition local;

	public LocalVariableMatchByDefinition(LocalVariableDefinition localDef) {
	    local = localDef;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof VarInsnNode) {
		LocalVariableNode var = method.localVariables.stream()
			.filter(v -> v.desc.equals(local.desc()) && v.name.equals(local.name()))
			.findAny()
			.orElse(null);
		ret = var != null && ((VarInsnNode) node).var == var.index;
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is accessing local variable %s", local.toString());
	}

    }

    public static class FieldMatch implements InstructionMatcher {

	private final FieldDefinition fieldDef;

	public FieldMatch(FieldDefinition field) {
	    fieldDef = field;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof FieldInsnNode) {
		FieldInsnNode field = (FieldInsnNode) node;
		ret = field.name.equals(fieldDef.name()) && field.desc.equals(fieldDef.desc());
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is accessing field %s", fieldDef.toString());
	}

    }

    public static class FieldTypeMatch implements InstructionMatcher {

	private final Type fieldType;

	public FieldTypeMatch(Type type) {
	    fieldType = type;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof FieldInsnNode) {
		FieldInsnNode field = (FieldInsnNode) node;
		ret = field.desc.equals(fieldType.getDescriptor());
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is accessing field any field of type %s", fieldType.getClassName());
	}

    }

    public static class MethodCallMatch implements InstructionMatcher {

	private final MethodDefinition methodDef;

	public MethodCallMatch(MethodDefinition method) {
	    methodDef = method;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof MethodInsnNode) {
		MethodInsnNode methodInsn = (MethodInsnNode) node;
		ret = methodInsn.name.equals(methodDef.name()) && methodInsn.desc.equals(methodDef.desc());
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is calling method %s", methodDef.toString());
	}

    }

    public static class MethodReturnTypeMatch implements InstructionMatcher {

	private final Type returnType;

	public MethodReturnTypeMatch(Type type) {
	    returnType = type;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node instanceof MethodInsnNode) {
		MethodInsnNode methodInsn = (MethodInsnNode) node;
		ret = Type.getMethodType(methodInsn.desc).getReturnType().equals(returnType);
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node is calling a method returning type %s", returnType.getInternalName());
	}

    }

    public static class TypeInsnMatch implements InstructionMatcher {

	private final int opcode;
	private final Type desc;

	public TypeInsnMatch(int opcode, Type castDesc) {
	    this.opcode = opcode;
	    desc = castDesc;
	}

	@Override
	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    boolean ret = false;
	    if (node.getOpcode() == opcode && node instanceof TypeInsnNode) {
		TypeInsnNode typeNode = (TypeInsnNode) node;
		ret = typeNode.desc.equals(desc.getInternalName());
	    }

	    return ret ? MatchResult.matchSingleNode(node) : MatchResult.noMatch();
	}

	@Override
	public String toString() {
	    return String.format("Node has an opcode of %s and is casting to type %s", Textifier.OPCODES[opcode], desc.getInternalName());
	}

    }

}
