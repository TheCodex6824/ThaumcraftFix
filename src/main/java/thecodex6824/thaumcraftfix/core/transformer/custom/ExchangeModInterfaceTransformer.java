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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;
import thecodex6824.thaumcraftfix.core.transformer.Types;

public class ExchangeModInterfaceTransformer implements ITransformer {

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.items.casters.foci.FocusEffectExchange".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode method = TransformUtil.findMethod(classNode, new MethodDefinition(
		"thaumcraft/common/items/casters/foci/FocusEffectExchange",
		false,
		"execute",
		Type.BOOLEAN_TYPE,
		Type.getType("Lnet/minecraft/util/math/RayTraceResult;"), Type.getType("Lthaumcraft/api/casters/Trajectory;"),
		Type.FLOAT_TYPE, Type.INT_TYPE
		));
	boolean didSomething = false;
	for (int i = 0; i < method.instructions.size(); ++i) {
	    AbstractInsnNode node = method.instructions.get(i);
	    if (node instanceof TypeInsnNode) {
		TypeInsnNode typeNode = (TypeInsnNode) node;
		if (typeNode.desc.equals(Types.ITEM_CASTER.getDescriptor())) {
		    typeNode.desc = Types.I_CASTER.getDescriptor();
		    didSomething = true;
		}
	    }
	    else if (node instanceof MethodInsnNode) {
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (methodNode.owner.equals(Types.ITEM_CASTER.getInternalName())) {
		    methodNode.owner = Types.I_CASTER.getInternalName();
		    methodNode.itf = true;
		    methodNode.setOpcode(Opcodes.INVOKEINTERFACE);
		    didSomething = true;
		}
	    }
	}

	return didSomething;
    }

}
