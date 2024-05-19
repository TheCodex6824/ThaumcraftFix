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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import thecodex6824.coremodlib.ASMUtil;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.coremodlib.PatchStateMachine.EvaluationResult;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;

public class GenericStateMachineTransformer implements ITransformer {

    private PatchStateMachine machine;
    private boolean required;
    private int limit;
    private String cachedTargetClass;

    public GenericStateMachineTransformer(PatchStateMachine machine) {
	this(machine, true, -1);
    }

    public GenericStateMachineTransformer(PatchStateMachine machine, boolean requireMatch, int limit) {
	this.machine = machine;
	this.required = requireMatch;
	this.limit = limit;
	cachedTargetClass = machine.targetMethod().declaringClass().replace("/", ".");
    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return cachedTargetClass.equals(transformedName);
    }

    private void printTransformerReport(MethodDefinition method, InsnList originalBytecode, List<EvaluationResult> results) {
	StringJoiner output = new StringJoiner(System.lineSeparator());
	try {
	    output.add("Begin bytecode transformer execution report:");
	    output.add(String.format("Target method: %s", method.toString()));

	    output.add("Original bytecode:");
	    TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
	    originalBytecode.accept(visitor);
	    StringWriter writer = new StringWriter();
	    visitor.p.print(new PrintWriter(writer));
	    output.add(writer.toString());

	    output.add("Patch state machine execution trace:");
	    for (EvaluationResult result : results) {
		output.add(result.toString());
	    }
	}
	finally {
	    ThaumcraftFixCore.getLogger().error(output.toString());
	}
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode theMethod = TransformUtil.findMethod(classNode, machine.targetMethod());
	if (theMethod == null) {
	    throw new IllegalArgumentException(String.format(
		    "Target method %s does not exist in the provided class %s",
		    machine.targetMethod().toString(), classNode.name
		    ));
	}

	InsnList instructionsStartingCopy = ASMUtil.cloneInsnList(theMethod.instructions);
	InsnList instructionsWorkingCopy = instructionsStartingCopy;

	boolean didSomething = false;
	boolean incomplete = false;
	ArrayList<EvaluationResult> results = new ArrayList<>();
	Set<AbstractInsnNode> visitedNodes = Collections.newSetFromMap(new IdentityHashMap<AbstractInsnNode, Boolean>());
	int count = 0;
	while (true) {
	    EvaluationResult result = machine.evaluate(theMethod, visitedNodes);
	    results.add(result);
	    if (!result.evaluationFullyCompleted()) {
		incomplete = result.evaluationHadAnyMatch();
		break;
	    }

	    didSomething = true;
	    if (limit >= 0 && ++count >= limit) {
		break;
	    }

	    instructionsWorkingCopy = ASMUtil.cloneInsnList(theMethod.instructions);
	}

	boolean crashGame = !didSomething && required;
	if (crashGame || ThaumcraftFixCore.isDebugEnabled()) {
	    if (results.size() > 1 && limit < 0) {
		results.remove(results.size() - 1);
	    }

	    printTransformerReport(machine.targetMethod(), instructionsStartingCopy, results);
	    if (crashGame) {
		throw new RuntimeException("Fatal class transformer error");
	    }
	}

	if (incomplete) {
	    theMethod.instructions = instructionsWorkingCopy;
	}

	return didSomething;
    }

}
