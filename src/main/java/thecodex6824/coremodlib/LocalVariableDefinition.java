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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LocalVariableDefinition {

    private final String varName;
    private final Type type;
    private final String desc;

    public LocalVariableDefinition(String varName, Type type) {
	this.varName = varName;
	this.type = type;
	desc = type.getDescriptor();
    }

    public String name() {
	return varName;
    }

    public String desc() {
	return desc;
    }

    public Type type() {
	return type;
    }

    public VarInsnNode asVarInsnNode(int opcode, MethodNode method) {
	return new VarInsnNode(opcode, method.localVariables.stream()
		.filter(v -> v.name.equals(varName) && v.desc.equals(desc))
		.findAny()
		.get()
		.index
		);
    }

    @Override
    public String toString() {
	return String.format("%s %s", type.getClassName(), varName);
    }

}
