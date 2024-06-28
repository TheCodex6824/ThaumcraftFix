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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;
import thecodex6824.thaumcraftfix.core.transformer.Types;

public class BlockApplyOffsetTransformer implements ITransformer {

    public static final class Hooks {

	public static AxisAlignedBB offsetBoundingBox(AxisAlignedBB box, IBlockState state,
		IBlockAccess world, BlockPos pos) {
	    return box.offset(state.getOffset(world, pos));
	}

    }

    private String internalName;
    private String internalNameWithDots;

    public BlockApplyOffsetTransformer(String className) {
	internalName = className.replace('.', '/');
	internalNameWithDots = className.replace('/', '.');
    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return internalNameWithDots.equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	String hooksName = Type.getType(Hooks.class).getInternalName();
	MethodNode bb = TransformUtil.remapMethod(new MethodDefinition(
		internalName,
		false,
		"func_185496_a",
		Types.AXIS_ALIGNED_BB,
		Types.I_BLOCK_STATE, Types.I_BLOCK_ACCESS, Types.BLOCK_POS
		)).createNewMethodNode(Opcodes.ACC_PUBLIC);
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
	bb.instructions.add(TransformUtil.remapMethod(new MethodDefinition(
		"net/minecraft/block/BlockBush",
		false,
		"func_185496_a",
		Types.AXIS_ALIGNED_BB,
		Types.I_BLOCK_STATE, Types.I_BLOCK_ACCESS, Types.BLOCK_POS
		)).asMethodInsnNode(Opcodes.INVOKESPECIAL));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
	bb.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
	bb.instructions.add(new MethodDefinition(
		hooksName,
		false,
		"offsetBoundingBox",
		Types.AXIS_ALIGNED_BB,
		Types.AXIS_ALIGNED_BB, Types.I_BLOCK_STATE, Types.I_BLOCK_ACCESS, Types.BLOCK_POS
		).asMethodInsnNode(Opcodes.INVOKESTATIC));
	bb.instructions.add(new InsnNode(Opcodes.ARETURN));
	classNode.methods.add(bb);
	return true;
    }

}
