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

import net.minecraft.item.ItemStack;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.Types;

public class PrimordialPearlDurabilityBarTransformer implements ITransformer {

    public static final class Hooks {

	public static boolean showDurabilityBar(ItemStack stack) {
	    return stack.getItemDamage() > 0;
	}

	public static double getDurabilityForDisplay(ItemStack stack) {
	    return stack.getItemDamage() / 8.0;
	}

    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.items.curios.ItemPrimordialPearl".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	String hooksName = Type.getType(Hooks.class).getInternalName();
	MethodNode showBar = new MethodDefinition(
		"thaumcraft/common/items/curios/ItemPrimordialPearl",
		false,
		"showDurabilityBar",
		Type.BOOLEAN_TYPE,
		Types.ITEM_STACK
		).createNewMethodNode(Opcodes.ACC_PUBLIC);
	showBar.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	showBar.instructions.add(new MethodDefinition(
		hooksName,
		false,
		"showDurabilityBar",
		Type.BOOLEAN_TYPE,
		Types.ITEM_STACK
		).asMethodInsnNode(Opcodes.INVOKESTATIC));
	showBar.instructions.add(new InsnNode(Opcodes.IRETURN));
	classNode.methods.add(showBar);

	MethodNode barFill = new MethodDefinition(
		"thaumcraft/common/items/curios/ItemPrimordialPearl",
		false,
		"getDurabilityForDisplay",
		Type.DOUBLE_TYPE,
		Types.ITEM_STACK
		).createNewMethodNode(Opcodes.ACC_PUBLIC);
	barFill.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	barFill.instructions.add(new MethodDefinition(
		hooksName,
		false,
		"getDurabilityForDisplay",
		Type.DOUBLE_TYPE,
		Types.ITEM_STACK
		).asMethodInsnNode(Opcodes.INVOKESTATIC));
	barFill.instructions.add(new InsnNode(Opcodes.DRETURN));
	classNode.methods.add(barFill);
	return true;
    }

}
