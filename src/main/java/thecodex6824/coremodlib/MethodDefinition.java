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

import java.util.StringJoiner;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

public class MethodDefinition {

    private final String className;
    private final boolean itf;
    private final String methodName;
    private final Type returnType;
    private final Type[] args;
    private final String desc;

    public MethodDefinition(String className, boolean classIsInterface, String methodName, Type returnType, Type... args) {
	this.className = className;
	this.itf = classIsInterface;
	this.methodName = methodName;
	this.returnType = returnType;
	this.args = args;
	desc = Type.getMethodDescriptor(returnType, args);
    }

    public String name() {
	return methodName;
    }

    public String declaringClass() {
	return className;
    }

    public boolean declaringClassIsInterface() {
	return itf;
    }

    public String desc() {
	return desc;
    }

    public Type returnType() {
	return returnType;
    }

    public Type[] argumentTypes() {
	return args;
    }

    public MethodInsnNode asMethodInsnNode(int opcode) {
	return new MethodInsnNode(opcode, className, methodName, desc, itf);
    }

    @Override
    public String toString() {
	StringJoiner argJoiner = new StringJoiner(", ");
	for (Type t : args) {
	    argJoiner.add(t.getClassName());
	}

	return String.format("%s %s.%s(%s)", returnType.getClassName(),
		className, methodName, argJoiner.toString());
    }

}
