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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;

public final class TransformUtil {

    private TransformUtil() {}

    public static Type remapType(Type input) {
	return Type.getType(FMLDeobfuscatingRemapper.INSTANCE.mapType(input.getDescriptor()));
    }

    public static FieldDefinition remapField(FieldDefinition input) {
	FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
	Type remappedType = remapType(input.type());
	String internal = remapper.map(input.declaringClass());
	String remapped = remapper.mapFieldName(internal, input.name(), remappedType.getDescriptor());
	return new FieldDefinition(internal, remapped, remappedType);
    }

    public static MethodDefinition remapMethod(MethodDefinition input) {
	FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
	Type remappedRet = remapType(input.returnType());
	Type[] remappedArgs = new Type[input.argumentTypes().length];
	for (int i = 0; i < remappedArgs.length; ++i) {
	    remappedArgs[i] = remapType(input.argumentTypes()[i]);
	}
	String internal = remapper.map(input.declaringClass());
	String remapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internal, input.name(), Type.getMethodDescriptor(remappedRet, remappedArgs));
	return new MethodDefinition(internal, input.declaringClassIsInterface(), remapped, remappedRet, remappedArgs);
    }

    public static MethodNode findMethod(ClassNode classNode, MethodDefinition def) {
	return findMethod(classNode, def.name(), def.desc());
    }

    public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
	for (MethodNode m : classNode.methods) {
	    if (m.name.equals(name) && m.desc.equals(desc) && (m.access & Opcodes.ACC_BRIDGE) == 0)
		return m;
	}

	return null;
    }

    public static int findLineNumber(MethodNode node, int number) {
	for (int i = 0; i < node.instructions.size(); ++i) {
	    AbstractInsnNode insn = node.instructions.get(i);
	    if (insn instanceof LineNumberNode && ((LineNumberNode) insn).line == number)
		return i;
	}

	return -1;
    }

}
