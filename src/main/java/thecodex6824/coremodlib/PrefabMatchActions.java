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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class PrefabMatchActions {

    private static final String INSN_SEPARATOR = String.copyValueOf(
	    ArrayUtils.toPrimitive(Collections.nCopies(80, '-').toArray(new Character[80])));
    private static final String SURROUND_SEPARATOR = String.copyValueOf(
	    ArrayUtils.toPrimitive(Collections.nCopies(80, '=').toArray(new Character[80])));

    public static class DoNothing implements MatchAction {

	@Override
	public Collection<AbstractInsnNode> onMatch(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    return ImmutableList.of();
	}

	@Override
	public String describe(MatchDetails result, List<? extends MatchDetails> matches) {
	    return "No action (no-op)";
	}

    }

    public static class GenericAction implements MatchAction {

	private final BiFunction<MethodNode, List<? extends MatchDetails>, Collection<AbstractInsnNode>> action;

	public GenericAction(BiFunction<MethodNode, List<? extends MatchDetails>, Collection<AbstractInsnNode>> customAction) {
	    action = customAction;
	}

	@Override
	public Collection<AbstractInsnNode> onMatch(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    return action.apply(method, matches);
	}

	@Override
	public String describe(MatchDetails result, List<? extends MatchDetails> matches) {
	    return "Generic action, no details :(";
	}

    }

    public static class InsertInstructionsBeforeMatch implements MatchAction {

	private final InsnList instructions;

	public InsertInstructionsBeforeMatch(InsnList list) {
	    instructions = list;
	}

	@Override
	public Collection<AbstractInsnNode> onMatch(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    InsnList workingCopy = ASMUtil.cloneInsnList(instructions);
	    Collection<AbstractInsnNode> ret = ImmutableList.copyOf(workingCopy.iterator());
	    method.instructions.insertBefore(result.matchStart(), workingCopy);
	    return ret;
	}

	@Override
	public String describe(MatchDetails result, List<? extends MatchDetails> matches) {
	    StringBuilder output = new StringBuilder();
	    output.append("Inserting instructions BEFORE target:");
	    output.append(System.lineSeparator());

	    AbstractInsnNode target = result.matchStart();

	    StringWriter traceOutput = new StringWriter();
	    TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	    AbstractInsnNode prev = target.getPrevious();
	    for (int i = 0; prev != null && i < instructions.size(); ++i) {
		prev = prev.getPrevious();
	    }

	    if (prev != null) {
		prev.accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<start of instruction list>");
		output.append(System.lineSeparator());
	    }

	    output.append(INSN_SEPARATOR);
	    output.append(System.lineSeparator());
	    instructions.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();
	    output.append(System.lineSeparator());
	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());

	    target.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();
	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());
	    if (target.getNext() != null) {
		target.getNext().accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<end of instruction list>");
		output.append(System.lineSeparator());
	    }

	    return output.toString();
	}

    }

    public static class InsertInstructionsAfterMatch implements MatchAction {

	private final InsnList instructions;

	public InsertInstructionsAfterMatch(InsnList list) {
	    instructions = list;
	}

	@Override
	public Collection<AbstractInsnNode> onMatch(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    InsnList workingCopy = ASMUtil.cloneInsnList(instructions);
	    Collection<AbstractInsnNode> ret = ImmutableList.copyOf(workingCopy.iterator());
	    method.instructions.insert(result.matchEnd(), workingCopy);
	    return ret;
	}

	@Override
	public String describe(MatchDetails result, List<? extends MatchDetails> matches) {
	    StringBuilder output = new StringBuilder();
	    output.append("Inserting instructions AFTER target:");
	    output.append(System.lineSeparator());

	    AbstractInsnNode target = result.matchEnd();

	    StringWriter traceOutput = new StringWriter();
	    TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	    if (target.getPrevious() != null) {
		target.getPrevious().accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<start of instruction list>");
		output.append(System.lineSeparator());
	    }

	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());
	    target.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();

	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());
	    instructions.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();
	    output.append(INSN_SEPARATOR);
	    output.append(System.lineSeparator());

	    AbstractInsnNode next = target.getNext();
	    for (int i = 0; next != null && i < instructions.size(); ++i) {
		next = next.getNext();
	    }

	    if (next != null) {
		next.accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<end of instruction list>");
		output.append(System.lineSeparator());
	    }

	    return output.toString();
	}

    }

    public static class InsertInstructionsSurroundingMatch implements MatchAction {

	private final InsnList before;
	private final InsnList after;

	public InsertInstructionsSurroundingMatch(InsnList before, InsnList after) {
	    this.before = before;
	    this.after = after;
	}

	@Override
	public Collection<AbstractInsnNode> onMatch(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    Map<LabelNode, LabelNode> labelMap = ASMUtil.makeLabelCloneMap(before.iterator(), ImmutableMap.of());
	    labelMap = ASMUtil.makeLabelCloneMap(after.iterator(), labelMap);

	    ImmutableList.Builder<AbstractInsnNode> ret = ImmutableList.builder();
	    InsnList beforeWorking = ASMUtil.cloneInsnList(before, labelMap);
	    ret.addAll(beforeWorking.iterator());
	    method.instructions.insertBefore(result.matchStart(), beforeWorking);

	    InsnList afterWorking = ASMUtil.cloneInsnList(after, labelMap);
	    ret.addAll(afterWorking.iterator());
	    method.instructions.insert(result.matchEnd(), afterWorking);
	    return ret.build();
	}

	@Override
	public String describe(MatchDetails result, List<? extends MatchDetails> matches) {
	    StringBuilder output = new StringBuilder();
	    output.append("Inserting instructions SURROUNDING (before and after) target:");
	    output.append(System.lineSeparator());

	    AbstractInsnNode target = result.matchStart();

	    StringWriter traceOutput = new StringWriter();
	    TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	    AbstractInsnNode prev = target.getPrevious();
	    for (int i = 0; prev != null && i < before.size(); ++i) {
		prev = prev.getPrevious();
	    }

	    if (prev != null) {
		prev.accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<start of instruction list>");
		output.append(System.lineSeparator());
	    }

	    output.append(INSN_SEPARATOR);
	    output.append(System.lineSeparator());
	    before.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();
	    output.append(System.lineSeparator());

	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());
	    while (target != result.matchEnd().getNext()) {
		target.accept(visitor);
		target = target.getNext();
	    }
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();

	    output.append(SURROUND_SEPARATOR);
	    output.append(System.lineSeparator());
	    after.accept(visitor);
	    visitor.p.print(new PrintWriter(traceOutput));
	    output.append(traceOutput.toString());
	    visitor.p.text.clear();
	    traceOutput = new StringWriter();
	    output.append(INSN_SEPARATOR);
	    output.append(System.lineSeparator());

	    AbstractInsnNode next = target;
	    for (int i = 0; next != null && i < after.size(); ++i) {
		next = next.getNext();
	    }

	    if (next != null) {
		next.accept(visitor);
		visitor.p.print(new PrintWriter(traceOutput));
		output.append(traceOutput.toString());
		visitor.p.text.clear();
		traceOutput = new StringWriter();
	    }
	    else {
		output.append("<end of instruction list>");
		output.append(System.lineSeparator());
	    }

	    return output.toString();
	}

    }

}
