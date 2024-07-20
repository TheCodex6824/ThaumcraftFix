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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;

public class EntityAspectPrefixRemoverTransformer implements ITransformer {

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.config.ConfigAspects".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode register = TransformUtil.findMethod(classNode, new MethodDefinition(
		"thaumcraft/common/config/ConfigAspects",
		false,
		"registerEntityAspects",
		Type.VOID_TYPE
		));
	boolean didSomething = false;
	for (int i = 0; i < register.instructions.size(); ++i) {
	    AbstractInsnNode node = register.instructions.get(i);
	    if (node instanceof LdcInsnNode) {
		LdcInsnNode ldc = (LdcInsnNode) node;
		// the entity name registrations do not have a "Thaumcraft." prefix, so remove it to match
		if (ldc.cst instanceof String && ((String) ldc.cst).startsWith("Thaumcraft.")) {
		    ldc.cst = ((String) ldc.cst).replaceFirst("Thaumcraft.", "");
		    didSomething = true;
		}
	    }
	}

	return didSomething;
    }

}
