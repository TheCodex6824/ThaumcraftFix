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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import thecodex6824.coremodlib.PatchBuilders.TransformerBuilder;

public class PatchStateMachine {

    protected static class InstructionStateMachineNode {

	private final InstructionMatcher matcher;
	private final ImmutableList<MatchTransformer> transformers;
	private final ImmutableList<MatchAction> actions;

	public InstructionStateMachineNode(InstructionMatcher matcher, List<MatchTransformer> transformers,
		List<MatchAction> actions) {
	    this.matcher = matcher;
	    this.transformers = ImmutableList.copyOf(transformers);
	    this.actions = ImmutableList.copyOf(actions);
	}

	public MatchResult matches(MethodNode method, AbstractInsnNode node) {
	    MatchResult result = matcher.matches(method, node);
	    if (result.matched()) {
		for (MatchTransformer transformer : transformers) {
		    transformer.transformMatch(result);
		}
	    }

	    return result;
	}

	public Collection<AbstractInsnNode> runMatchActions(MethodNode method, MatchDetails result, List<? extends MatchDetails> matches) {
	    ImmutableList.Builder<AbstractInsnNode> builder = ImmutableList.builder();
	    for (MatchAction action : actions) {
		builder.addAll(action.onMatch(method, result, matches));
	    }

	    return builder.build();
	}

	public String describe(MatchSnapshot result, List<MatchSnapshot> matches) {
	    StringBuilder builder = new StringBuilder();
	    builder.append(String.format("    Condition: %s%n", matcher.toString()));
	    if (result.originalStart() != result.matchStart() || result.originalEnd() != result.matchEnd()) {
		builder.append(String.format("    Match was modified:%n    Before:%s%n%n    After:%s%n",
			ASMUtil.dumpBytecode(result.originalStart(), result.originalEnd()), ASMUtil.dumpBytecode(result.matchStart(), result.matchEnd())));
	    }

	    if (result.matched()) {
		builder.append("    Executed actions:" + System.lineSeparator());
		for (MatchAction action : actions) {
		    builder.append("        " + action.describe(result, matches));
		}
	    }
	    else {
		builder.append(String.format("    Condition was not matched%n"));
	    }

	    return builder.toString();
	}

    }

    protected static class EvaluationStep {

	private final InstructionStateMachineNode node;
	private final List<MatchSnapshot> allMatches;
	private final InsnList instructions;

	private String cachedBytecode;

	public EvaluationStep(InstructionStateMachineNode nodeChecked, InsnList currentInstructions, List<MatchSnapshot> matches) {
	    node = nodeChecked;
	    instructions = ASMUtil.cloneInsnList(currentInstructions);
	    allMatches = ImmutableList.copyOf(matches);
	}

	public String getBytecodeAfterEvaluation() {
	    if (cachedBytecode == null) {
		StringWriter writer = new StringWriter();
		TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
		instructions.accept(visitor);
		visitor.p.print(new PrintWriter(writer));
		cachedBytecode = writer.toString();
	    }

	    return cachedBytecode;
	}

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    MatchSnapshot result = null;
	    if (!allMatches.isEmpty()) {
		result = allMatches.get(allMatches.size() - 1);
	    }
	    else {
		result = new MatchSnapshot(null, null, null, null);
	    }

	    builder.append(String.format("Match details: %s", node.describe(result, allMatches)));
	    if (result.matched()) {
		builder.append(String.format("Resulting bytecode: %n%n%s%n", getBytecodeAfterEvaluation()));
	    }

	    return builder.toString();
	}

    }

    public static class EvaluationResult {

	private final boolean completed;
	private final List<EvaluationStep> steps;

	protected EvaluationResult(boolean completed, List<EvaluationStep> steps) {
	    this.completed = completed;
	    this.steps = ImmutableList.copyOf(steps);
	}

	public boolean evaluationFullyCompleted() {
	    return completed;
	}

	public boolean evaluationHadAnyMatch() {
	    if (!steps.isEmpty()) {
		for (MatchSnapshot result : steps.get(steps.size() - 1).allMatches) {
		    if (result.matched()) {
			return true;
		    }
		}
	    }

	    return false;
	}

	public List<? extends MatchDetails> matchResults() {
	    return !steps.isEmpty() ? steps.get(steps.size() - 1).allMatches : ImmutableList.of();
	}

	@Override
	public String toString() {
	    StringBuilder output = new StringBuilder();
	    for (int i = 0; i < steps.size(); ++i) {
		output.append(String.format("Results of step %d%n", i + 1));
		output.append(steps.get(i).toString());
		output.append(System.lineSeparator());
	    }

	    return output.toString();
	}

    }

    private final MethodDefinition method;
    private final ImmutableList<InstructionStateMachineNode> nodes;

    protected PatchStateMachine(MethodDefinition method, ImmutableList<InstructionStateMachineNode> nodes) {
	this.method = method;
	this.nodes = nodes;
    }

    public EvaluationResult evaluate(MethodNode methodNode) {
	return evaluate(methodNode, ImmutableSet.of());
    }

    public EvaluationResult evaluate(MethodNode methodNode, Set<AbstractInsnNode> ignoreMatches) {
	ArrayList<MatchResult> liveMatches = new ArrayList<>();
	ArrayList<MatchSnapshot> snapshotMatches = new ArrayList<>();
	ArrayList<EvaluationStep> steps = new ArrayList<>();
	InstructionStateMachineNode lastNode = null;
	boolean complete = false;
	int currentNode = 0;
	for (int i = 0; i < methodNode.instructions.size(); ++i) {
	    AbstractInsnNode node = methodNode.instructions.get(i);
	    if (!ignoreMatches.contains(node)) {
		lastNode = nodes.get(currentNode);
		MatchResult result = lastNode.matches(methodNode, node);
		if (result.matched()) {
		    liveMatches.add(result);
		    snapshotMatches.add(result.makeSnapshot());
		    ignoreMatches.add(node);
		    ignoreMatches.addAll(lastNode.runMatchActions(methodNode, result, liveMatches));
		    steps.add(new EvaluationStep(lastNode, methodNode.instructions, snapshotMatches));
		    ++currentNode;
		    if (currentNode >= nodes.size()) {
			complete = true;
			break;
		    }
		}
	    }
	}

	if (!complete) {
	    steps.add(new EvaluationStep(lastNode, methodNode.instructions, snapshotMatches));
	}

	return new EvaluationResult(complete, steps);
    }

    public MethodDefinition targetMethod() {
	return method;
    }

    public static TransformerBuilder builder(MethodDefinition method) {
	return new TransformerBuilder(method);
    }

}
