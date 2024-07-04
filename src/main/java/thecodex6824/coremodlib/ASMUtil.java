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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.collect.ImmutableMap;

public final class ASMUtil {

    private ASMUtil() {}

    public static InsnList arrayToInsnList(AbstractInsnNode... toInsert) {
	InsnList list = new InsnList();
	for (AbstractInsnNode node : toInsert) {
	    list.add(node);
	}

	return list;
    }

    public static Map<LabelNode, LabelNode> makeLabelCloneMap(Iterator<? extends AbstractInsnNode> iterator,
	    Map<LabelNode, LabelNode> predefinedLabels) {

	Map<LabelNode, LabelNode> clonedLabels = new IdentityHashMap<>();
	clonedLabels.putAll(predefinedLabels);
	while (iterator.hasNext()) {
	    AbstractInsnNode node = iterator.next();
	    LabelNode oldLabel = null;
	    if (node instanceof LabelNode) {
		oldLabel = (LabelNode) node;
	    }
	    else if (node instanceof JumpInsnNode) {
		oldLabel = ((JumpInsnNode) node).label;
	    }

	    if (oldLabel != null && !clonedLabels.containsKey(oldLabel)) {
		LabelNode newLabel = new LabelNode(new Label());
		if (oldLabel.invisibleTypeAnnotations != null) {
		    newLabel.invisibleTypeAnnotations = new ArrayList<>();
		    for (TypeAnnotationNode annotation : oldLabel.invisibleTypeAnnotations) {
			newLabel.invisibleTypeAnnotations.add(new TypeAnnotationNode(Opcodes.ASM5,
				annotation.typeRef, annotation.typePath, annotation.desc));
		    }
		}
		if (oldLabel.visibleTypeAnnotations != null) {
		    newLabel.visibleTypeAnnotations = new ArrayList<>();
		    for (TypeAnnotationNode annotation : oldLabel.visibleTypeAnnotations) {
			newLabel.visibleTypeAnnotations.add(new TypeAnnotationNode(Opcodes.ASM5,
				annotation.typeRef, annotation.typePath, annotation.desc));
		    }
		}

		clonedLabels.put(oldLabel, newLabel);
	    }
	}

	return clonedLabels;
    }

    public static InsnList cloneNodeRangeAndDependencies(AbstractInsnNode start, AbstractInsnNode end) {
	Set<LabelNode> neededLabels = Collections.newSetFromMap(new IdentityHashMap<LabelNode, Boolean>());
	Set<LabelNode> resolvedLabels = Collections.newSetFromMap(new IdentityHashMap<LabelNode, Boolean>());
	ArrayList<AbstractInsnNode> nodes = new ArrayList<>();
	boolean reachedEnd = false;
	AbstractInsnNode cursor = start;
	while (!reachedEnd || !neededLabels.isEmpty()) {
	    if (cursor == end) {
		reachedEnd = true;
	    }
	    else if (cursor == null) {
		throw new IllegalStateException("Could not resolve match range or dependencies");
	    }

	    nodes.add(cursor);
	    if (cursor instanceof JumpInsnNode) {
		LabelNode label = ((JumpInsnNode) cursor).label;
		if (!resolvedLabels.contains(label)) {
		    neededLabels.add(label);
		}
	    }
	    else if (cursor instanceof LabelNode) {
		neededLabels.remove(cursor);
		resolvedLabels.add((LabelNode) cursor);
	    }

	    cursor = cursor.getNext();
	}

	Map<LabelNode, LabelNode> clonedLabels = makeLabelCloneMap(resolvedLabels.iterator(), ImmutableMap.of());
	InsnList newList = new InsnList();
	for (AbstractInsnNode node : nodes) {
	    newList.add(node.clone(clonedLabels));
	}

	return newList;
    }

    public static InsnList cloneInsnList(InsnList toClone) {
	return cloneInsnList(toClone, ImmutableMap.of());
    }

    public static InsnList cloneInsnList(InsnList toClone, Map<LabelNode, LabelNode> predefinedLabels) {
	Map<LabelNode, LabelNode> clonedLabels = makeLabelCloneMap(toClone.iterator(), predefinedLabels);
	InsnList instructionsCopy = new InsnList();
	for (int i = 0; i < toClone.size(); ++i) {
	    instructionsCopy.add(toClone.get(i).clone(clonedLabels));
	}

	return instructionsCopy;
    }

    private static boolean nodeAnnotationsEqual(AbstractInsnNode node1, AbstractInsnNode node2) {
	boolean visibleEqual = (node1.visibleTypeAnnotations == null && node2.visibleTypeAnnotations == null) ||
		(node1.visibleTypeAnnotations != null && node2.visibleTypeAnnotations != null && node1.visibleTypeAnnotations.equals(node2.visibleTypeAnnotations));
	boolean invisibleEqual = (node1.invisibleTypeAnnotations == null && node2.invisibleTypeAnnotations == null) ||
		(node1.invisibleTypeAnnotations != null && node2.invisibleTypeAnnotations != null && node1.invisibleTypeAnnotations.equals(node2.invisibleTypeAnnotations));
	return visibleEqual && invisibleEqual;
    }

    private static <T> boolean listsSafeEquals(List<T> list1, List<T> list2) {
	return (list1 == null && list2 == null) || (list1 != null && list2 != null && list1.equals(list2));
    }

    public static boolean nodesEqualByValue(AbstractInsnNode first, AbstractInsnNode second) {
	boolean match = false;
	if (first.getType() == second.getType() && first.getOpcode() == second.getOpcode() && nodeAnnotationsEqual(first, second)) {
	    switch (first.getType()) {
	    case AbstractInsnNode.INSN: {
		match = true;
		break;
	    }
	    case AbstractInsnNode.INT_INSN: {
		IntInsnNode node1 = (IntInsnNode) first;
		IntInsnNode node2 = (IntInsnNode) second;
		match = node1.operand == node2.operand;
		break;
	    }
	    case AbstractInsnNode.VAR_INSN: {
		VarInsnNode node1 = (VarInsnNode) first;
		VarInsnNode node2 = (VarInsnNode) second;
		match = node1.var == node2.var;
		break;
	    }
	    case AbstractInsnNode.TYPE_INSN: {
		TypeInsnNode node1 = (TypeInsnNode) first;
		TypeInsnNode node2 = (TypeInsnNode) second;
		match = node1.desc.equals(node2.desc);
		break;
	    }
	    case AbstractInsnNode.FIELD_INSN: {
		FieldInsnNode node1 = (FieldInsnNode) first;
		FieldInsnNode node2 = (FieldInsnNode) second;
		match = node1.name.equals(node2.name) && node1.owner.equals(node2.owner) &&
			node1.desc.equals(node2.desc);
		break;
	    }
	    case AbstractInsnNode.METHOD_INSN: {
		MethodInsnNode node1 = (MethodInsnNode) first;
		MethodInsnNode node2 = (MethodInsnNode) second;
		match = node1.itf == node2.itf && node1.name.equals(node2.name) &&
			node1.owner.equals(node2.owner) && node1.desc.equals(node2.desc);
		break;
	    }
	    case AbstractInsnNode.INVOKE_DYNAMIC_INSN: {
		InvokeDynamicInsnNode node1 = (InvokeDynamicInsnNode) first;
		InvokeDynamicInsnNode node2 = (InvokeDynamicInsnNode) second;
		match = node1.name.equals(node2.name) && node1.desc.equals(node2.desc) &&
			node1.bsm.equals(node2.bsm) && Arrays.deepEquals(node1.bsmArgs, node2.bsmArgs);
		break;
	    }
	    case AbstractInsnNode.JUMP_INSN: {
		JumpInsnNode node1 = (JumpInsnNode) first;
		JumpInsnNode node2 = (JumpInsnNode) second;
		match = nodesEqualByValue(node1.label, node2.label);
		break;
	    }
	    case AbstractInsnNode.LABEL: {
		LabelNode node1 = (LabelNode) first;
		LabelNode node2 = (LabelNode) second;
		match = node1.getLabel() == node2.getLabel();
		break;
	    }
	    case AbstractInsnNode.LDC_INSN: {
		LdcInsnNode node1 = (LdcInsnNode) first;
		LdcInsnNode node2 = (LdcInsnNode) second;
		match = node1.cst.equals(node2.cst);
		break;
	    }
	    case AbstractInsnNode.IINC_INSN: {
		IincInsnNode node1 = (IincInsnNode) first;
		IincInsnNode node2 = (IincInsnNode) second;
		match = node1.var == node2.var && node1.incr == node2.incr;
		break;
	    }
	    case AbstractInsnNode.TABLESWITCH_INSN: {
		TableSwitchInsnNode node1 = (TableSwitchInsnNode) first;
		TableSwitchInsnNode node2 = (TableSwitchInsnNode) second;
		match = node1.min == node2.min && node2.max == node2.max &&
			node1.labels.size() == node2.labels.size() && nodesEqualByValue(node1.dflt, node2.dflt);
		if (match) {
		    for (int labelIndex = 0; labelIndex < node1.labels.size(); ++labelIndex) {
			match &= nodesEqualByValue(node1.labels.get(labelIndex), node2.labels.get(labelIndex));
		    }
		}

		break;
	    }
	    case AbstractInsnNode.LOOKUPSWITCH_INSN: {
		LookupSwitchInsnNode node1 = (LookupSwitchInsnNode) first;
		LookupSwitchInsnNode node2 = (LookupSwitchInsnNode) second;
		match = node1.keys.size() == node2.keys.size() && node1.labels.size() == node2.labels.size() &&
			nodesEqualByValue(node1.dflt, node2.dflt);
		if (match) {
		    match = node1.keys.equals(node2.keys);
		    if (match) {
			for (int labelIndex = 0; labelIndex < node1.labels.size(); ++labelIndex) {
			    match &= nodesEqualByValue(node1.labels.get(labelIndex), node2.labels.get(labelIndex));
			}
		    }
		}
		break;
	    }
	    case AbstractInsnNode.MULTIANEWARRAY_INSN: {
		MultiANewArrayInsnNode node1 = (MultiANewArrayInsnNode) first;
		MultiANewArrayInsnNode node2 = (MultiANewArrayInsnNode) second;
		match = node1.dims == node2.dims && node1.desc.equals(node2.desc);
		break;
	    }
	    case AbstractInsnNode.FRAME: {
		FrameNode node1 = (FrameNode) first;
		FrameNode node2 = (FrameNode) second;
		match = node1.type == node2.type &&
			listsSafeEquals(node1.local, node2.local) && listsSafeEquals(node1.stack, node2.stack);
		break;
	    }
	    case AbstractInsnNode.LINE: {
		LineNumberNode node1 = (LineNumberNode) first;
		LineNumberNode node2 = (LineNumberNode) second;
		match = node1.line == node2.line && nodesEqualByValue(node1.start, node2.start);
		break;
	    }
	    default: {
		throw new IllegalArgumentException("Cannot compare nodes of unknown type");
	    }
	    }
	}

	return match;
    }

    public static boolean insnListContainsByValue(InsnList list, AbstractInsnNode toFind) {
	for (int i = 0; i < list.size(); ++i) {
	    if (nodesEqualByValue(toFind, list.get(i))) {
		return true;
	    }
	}

	return false;
    }

    public static String dumpClass(ClassNode node) {
	StringWriter traceOutput = new StringWriter();
	TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(traceOutput));
	node.accept(visitor);
	return traceOutput.toString();
    }

    public static String dumpBytecode(MethodNode method) {
	StringWriter traceOutput = new StringWriter();
	TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	method.accept(visitor);
	visitor.p.print(new PrintWriter(traceOutput));
	return traceOutput.toString();
    }

    public static String dumpBytecode(InsnList instructions) {
	StringWriter traceOutput = new StringWriter();
	TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	instructions.accept(visitor);
	visitor.p.print(new PrintWriter(traceOutput));
	return traceOutput.toString();
    }

    public static String dumpBytecode(AbstractInsnNode start, AbstractInsnNode end) {
	StringWriter traceOutput = new StringWriter();
	TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	while (start != null) {
	    start.accept(visitor);
	    if (start == end) {
		break;
	    }

	    start = start.getNext();
	}

	visitor.p.print(new PrintWriter(traceOutput));
	return traceOutput.toString();
    }

}
