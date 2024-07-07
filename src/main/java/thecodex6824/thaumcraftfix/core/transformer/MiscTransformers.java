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

import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.internal.CommonInternals;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.common.inventory.InventoryCraftingWrapper;
import thecodex6824.thaumcraftfix.core.transformer.custom.AuraChunkThreadSafetyTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class MiscTransformers {

    public static final class Hooks {

	public static boolean handleEmptyAspectList(ItemStack stack, AspectList list) {
	    if (list == null || list.size() == 0) {
		try {
		    CommonInternals.objectTags.remove(CommonInternals.generateUniqueItemstackId(stack));
		} catch (Exception ex) {}
		return false;
	    }

	    return true;
	}

	public static InventoryCrafting makeVanillaRecipeWrapper(InventoryCrafting input) {
	    return new InventoryCraftingWrapper(input);
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    public static final Supplier<ITransformer> ARCANE_WORKBENCH_RECIPE_COMPAT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/container/ContainerArcaneWorkbench",
			false,
			"func_192389_a",
			Type.VOID_TYPE,
			Types.WORLD, Types.ENTITY_PLAYER, Types.INVENTORY_CRAFTING, Types.INVENTORY_CRAFT_RESULT
			)))
		.findConsecutive()
		.findNextLocalAccess(3)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.I_RECIPE.getInternalName(),
			true,
			"func_77572_b",
			Types.ITEM_STACK,
			Types.INVENTORY_CRAFTING
			)))
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsBefore(
			new MethodDefinition(
				HOOKS,
				false,
				"makeVanillaRecipeWrapper",
				Types.INVENTORY_CRAFTING,
				Types.INVENTORY_CRAFTING
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> ASPECT_REGISTRY_LOOKUP = () -> {
	LabelNode registerLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/api/aspects/AspectEventProxy",
				false,
				"registerObjectTag",
				Type.VOID_TYPE,
				Types.ITEM_STACK, Types.ASPECT_LIST
				)
			)
		.findAny()
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodDefinition(
				HOOKS,
				false,
				"handleEmptyAspectList",
				Type.BOOLEAN_TYPE,
				Types.ITEM_STACK, Types.ASPECT_LIST
				).asMethodInsnNode(Opcodes.INVOKESTATIC),
			new JumpInsnNode(Opcodes.IFNE, registerLabel),
			new InsnNode(Opcodes.RETURN),
			registerLabel,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)
		.build(), true, 1
		);
    };

    public static final ITransformer AURA_CHUNK_THREAD_SAFETY = new ThrowingTransformerWrapper(
	    new AuraChunkThreadSafetyTransformer());

}
