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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;
import thecodex6824.thaumcraftfix.core.transformer.Types;

public class BlockPillarDropFixTransformer implements ITransformer {

    private static final String PILLAR = "thaumcraft/common/blocks/basic/BlockPillar";

    public static final class Hooks {

	public static Item itemDropped(IBlockState state) {
	    Item ret = null;
	    if (state.getBlock() == BlocksTC.pillarAncient) {
		ret = Item.getItemFromBlock(BlocksTC.stoneAncient);
	    }
	    else if (state.getBlock() == BlocksTC.pillarEldritch) {
		ret = Item.getItemFromBlock(BlocksTC.stoneEldritchTile);
	    }
	    else {
		ret = Item.getItemFromBlock(BlocksTC.stoneArcane);
	    }

	    return ret;
	}

    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.blocks.basic.BlockPillar".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode itemDropped = TransformUtil.findMethod(classNode, TransformUtil.remapMethod(new MethodDefinition(
		PILLAR,
		false,
		"func_180660_a",
		Types.ITEM,
		Types.I_BLOCK_STATE, Types.RANDOM, Type.INT_TYPE
		)));
	itemDropped.instructions.clear();
	itemDropped.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	itemDropped.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
		Type.getInternalName(Hooks.class),
		"itemDropped",
		Type.getMethodDescriptor(Types.ITEM, Types.I_BLOCK_STATE),
		false
		));
	itemDropped.instructions.add(new InsnNode(Opcodes.ARETURN));

	MethodNode quantityDropped = TransformUtil.remapMethod(new MethodDefinition(
		PILLAR,
		false,
		"func_149745_a",
		Type.INT_TYPE,
		Types.RANDOM
		)).createNewMethodNode(Opcodes.ACC_PUBLIC);
	quantityDropped.instructions.add(new InsnNode(Opcodes.ICONST_2));
	quantityDropped.instructions.add(new InsnNode(Opcodes.IRETURN));

	MethodDefinition breakBlock = TransformUtil.remapMethod(new MethodDefinition(
		PILLAR,
		false,
		"func_180663_b",
		Type.VOID_TYPE,
		Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE
		));
	classNode.methods.removeIf(m -> m.name.equals(breakBlock.name()) &&
		m.desc.equals(breakBlock.desc()));
	classNode.methods.add(quantityDropped);

	return true;
    }

}
