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

package thecodex6824.thaumcraftfix.core.transformer.custom;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.common.collect.ImmutableSet;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;

public class ChangeVariableTypeTransformer implements ITransformer {

    private final MethodDefinition def;
    private final String classWithDots;
    private final Type from;
    private final Type to;
    private final boolean itf;
    private final boolean ignoreNew;
    private final Set<String> ignored;

    public ChangeVariableTypeTransformer(MethodDefinition target, Type fromType, Type toType,
	    boolean toIsInterface) {
	this(target, fromType, toType, toIsInterface, Collections.emptyList(), false);
    }

    public ChangeVariableTypeTransformer(MethodDefinition target, Type fromType, Type toType,
	    boolean toIsInterface, Collection<String> ignoreMethods, boolean ignoreInstantiation) {
	def = target;
	classWithDots = target.declaringClass().replace("/", ".");
	from = fromType;
	to = toType;
	itf = toIsInterface;
	ignored = ImmutableSet.copyOf(ignoreMethods);
	ignoreNew = ignoreInstantiation;
    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return classWithDots.equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode method = TransformUtil.findMethod(classNode, def);
	boolean didSomething = false;
	for (int i = 0; i < method.instructions.size(); ++i) {
	    AbstractInsnNode node = method.instructions.get(i);
	    if (node instanceof TypeInsnNode && (!ignoreNew || node.getOpcode() != Opcodes.NEW)) {
		TypeInsnNode typeNode = (TypeInsnNode) node;
		if (typeNode.desc.equals(from.getDescriptor())) {
		    typeNode.desc = to.getDescriptor();
		    didSomething = true;
		}
	    }
	    else if (node instanceof MethodInsnNode && (!ignoreNew || node.getOpcode() != Opcodes.INVOKESPECIAL)) {
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (!ignored.contains(methodNode.name) && methodNode.owner.equals(from.getInternalName())) {
		    methodNode.owner = to.getInternalName();
		    methodNode.itf = itf;
		    methodNode.setOpcode(itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL);
		    didSomething = true;
		}
	    }
	    else if (node instanceof FieldInsnNode) {
		FieldInsnNode field = (FieldInsnNode) node;
		if (field.owner.equals(from.getInternalName())) {
		    field.owner = to.getInternalName();
		    didSomething = true;
		}
	    }
	    else if (node instanceof FrameNode) {
		FrameNode frame = (FrameNode) node;
		if (frame.stack != null) {
		    for (int var = 0; var < frame.stack.size(); ++var) {
			if (from.getInternalName().equals(frame.stack.get(var))) {
			    frame.stack.set(var, to.getInternalName());
			    didSomething = true;
			}
		    }
		}

		if (frame.local != null) {
		    for (int var = 0; var < frame.local.size(); ++var) {
			if (from.getInternalName().equals(frame.local.get(var))) {
			    frame.local.set(var, to.getInternalName());
			    didSomething = true;
			}
		    }
		}
	    }
	}

	return didSomething;
    }

}
