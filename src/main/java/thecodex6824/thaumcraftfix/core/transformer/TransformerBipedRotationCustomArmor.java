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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;

import thecodex6824.coremodlib.ASMUtil;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;

/**
 * Patches the ModelCustomArmor class to call the super method in setRotationAngles to fix
 * a whole lot of issues with other mods that hook into ModelBiped. Azanor apparently wanted to change
 * some rotation points on the model, which caused the copy+pasted code to be there instead of a super call.
 * Since other mods just ASM into ModelBiped and not TC's class, calling the super method is important here.
 * This is done through adding the super call here, and in another transformer injecting a static method
 * call to update rotation points. Sadly, just setting rotation points at the end or after it returns is too late
 * for some mods that need accurate rotation points.
 */
public class TransformerBipedRotationCustomArmor implements ITransformer {

    private static final String CLASS = "thaumcraft.client.renderers.models.gear.ModelCustomArmor";

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return CLASS.equals(transformedName);
    }

    private static boolean doTransform(MethodNode rot) {
	// if someone else touched things here (or anywhere else), just leave
	if (rot.instructions.size() != 1009 || rot.localVariables.size() != 15) {
	    return false;
	}

	int ret = TransformUtil.findLineNumber(rot, 38);
	if (ret == -1) {
	    return false;
	}

	InsnList toAdd = new InsnList();
	toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 1));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 2));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 3));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 4));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 5));
	toAdd.add(new VarInsnNode(Opcodes.FLOAD, 6));
	toAdd.add(new VarInsnNode(Opcodes.ALOAD, 7));
	toAdd.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
		"net/minecraft/client/model/ModelBiped",
		rot.name,
		rot.desc,
		false
		));
	AbstractInsnNode insertAfter = rot.instructions.get(ret).getNext();
	ret += toAdd.size() + 2;
	rot.instructions.insert(insertAfter, toAdd);

	// remove copied+pasted code that is now done in super call
	// PSA: don't remove instructions normally, it breaks coremods :(
	// or if you need to do this, use Mixin instead (just use Mixins in general instead)
	int end = TransformUtil.findLineNumber(rot, 206);
	if (end == -1) {
	    return false;
	}

	for (int i = 0; i < end - ret - 1; ++i) {
	    rot.instructions.remove(rot.instructions.get(ret));
	}

	ArrayList<LocalVariableNode> toRemove = new ArrayList<>();
	for (LocalVariableNode local : rot.localVariables) {
	    if (local != null && local.index > 7) {
		toRemove.add(local);
	    }
	}

	rot.localVariables.removeAll(toRemove);
	return true;
    }

    private static final String WARN_OUTPUT = "ModelCustomArmor#setRotationPoints appears to have been modified."
	    + " To avoid potentially breaking other mods, Thaumcraft fix will not patch it."
	    + " Custom model rotations/animations may not work correctly with Thaumcraft armor (such as robes).";

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode rot = TransformUtil.findMethod(classNode, TransformUtil.remapMethod(new MethodDefinition(
		CLASS,
		false,
		"func_78087_a",
		Type.VOID_TYPE,
		Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE,
		Types.ENTITY
		)));
	boolean success = false;
	if (rot != null) {
	    // if we have to bail, we will restore these backups of things we might have broke
	    // we return false to try to get the new bytecode to not save, but if some other transformer
	    // wants to save this class it will save anyway
	    InsnList backupInstructions = ASMUtil.cloneInsnList(rot.instructions);
	    List<LocalVariableNode> backupLocals = ImmutableList.copyOf(rot.localVariables);

	    success = doTransform(rot);
	    if (!success) {
		rot.instructions = backupInstructions;
		rot.localVariables.clear();
		rot.localVariables.addAll(backupLocals);
	    }
	}

	if (!success) {
	    ThaumcraftFixCore.getLogger().error(WARN_OUTPUT);
	    ThaumcraftFixCore.getLogger().error("Class dump for debugging:");
	    ThaumcraftFixCore.getLogger().error(ASMUtil.dumpClass(classNode));
	}

	return success;
    }

}
