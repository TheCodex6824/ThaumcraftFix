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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.coremodlib.ASMUtil;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;

public class PacketNoteHandlerRewriteTransformer implements ITransformer {

    public static final String ORIGINAL_METHOD_REDIRECT_NAME = "onMessageOriginalRedirectedByTCFix";

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.lib.network.misc.PacketNote".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode method = TransformUtil.findMethod(classNode, new MethodDefinition(
		"thaumcraft/common/lib/network/misc/PacketNote",
		false,
		"onMessage",
		Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;"),
		Type.getType("Lthaumcraft/common/lib/network/misc/PacketNote;"), Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")
		));
	boolean success = false;
	if (method != null) {
	    MethodNode wrapper = new MethodNode(method.access, method.name, method.desc, method.signature,
		    method.exceptions.toArray(new String[0]));
	    // change name of old method
	    method.name = ORIGINAL_METHOD_REDIRECT_NAME;

	    wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	    wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
	    wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		    TransformUtil.HOOKS_COMMON,
		    "handlePacketNote",
		    Type.getMethodDescriptor(Type.VOID_TYPE,
			    Type.getType("Lthaumcraft/common/lib/network/misc/PacketNote;"),
			    Type.getType("Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;")),
		    false
		    ));
	    wrapper.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
	    wrapper.instructions.add(new InsnNode(Opcodes.ARETURN));

	    classNode.methods.add(wrapper);
	    success = true;
	}

	if (!success) {
	    ThaumcraftFixCore.getLogger().error("Class dump for debugging:");
	    ThaumcraftFixCore.getLogger().error(ASMUtil.dumpClass(classNode));
	    throw new RuntimeException("Could not patch PacketNote");
	}

	return true;
    }

}
