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
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldDefinition {

    private final String className;
    private final String fieldName;
    private final Type type;
    private final String desc;

    public FieldDefinition(String className, String fieldName, Type type) {
	this.className = className;
	this.fieldName = fieldName;
	this.type = type;
	desc = type.getDescriptor();
    }

    public String name() {
	return fieldName;
    }

    public String declaringClass() {
	return className;
    }

    public String desc() {
	return desc;
    }

    public Type type() {
	return type;
    }

    public FieldInsnNode asFieldInsnNode(int opcode) {
	return new FieldInsnNode(opcode, className, fieldName, desc);
    }

    @Override
    public String toString() {
	return String.format("%s %s.%s", type.getClassName(), className, fieldName);
    }

}
