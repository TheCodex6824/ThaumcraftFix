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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.common.blocks.world.ore.ShardType;
import thaumcraft.common.lib.crafting.ContainerFake;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.common.inventory.FakeArcaneWorkbenchInventory;
import thecodex6824.thaumcraftfix.common.inventory.InventoryCraftingWrapper;
import thecodex6824.thaumcraftfix.core.transformer.custom.ChangeVariableTypeTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class MiscTransformers {

    public static final class Hooks {

	public static boolean handleEmptyAspectList(ItemStack stack, AspectList list) {
	    if (list == null || list.size() == 0) {
		try {
		    if (stack.isItemStackDamageable() || !stack.getHasSubtypes()) {
			stack = stack.copy();
			stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
		    }
		    CommonInternals.objectTags.putIfAbsent(CommonInternals.generateUniqueItemstackId(stack),
			    new AspectList());
		} catch (Exception ex) {}
		return false;
	    }

	    return true;
	}

	public static InventoryCrafting makeVanillaRecipeWrapper(InventoryCrafting input) {
	    return new InventoryCraftingWrapper(input);
	}

	private static final Set<ResourceLocation> BROKEN_RECIPES = new HashSet<ResourceLocation>();

	@Nullable
	public static InventoryCrafting createFilledInventoryForRecipe(IRecipe recipe) {
	    if (recipe.isDynamic() || !recipe.canFit(3, 3)) {
		return null;
	    }

	    InventoryCrafting ret = null;
	    if (recipe instanceof IArcaneRecipe) {
		ret = new FakeArcaneWorkbenchInventory(new ContainerFake(), 5, 3);
	    }
	    else {
		ret = new InventoryCrafting(new ContainerFake(), 3, 3);
	    }

	    int recipeWidth = -1;
	    if (recipe instanceof IShapedRecipe) {
		recipeWidth = ((IShapedRecipe) recipe).getRecipeWidth();
	    }

	    // this will place the recipe in the upper left of the grid
	    // if the recipe has a width of less than 3, then we make sure to skip slots
	    // so the indices line up to match the actual recipe shape
	    int slot = 0;
	    for (Ingredient ingredient : recipe.getIngredients()) {
		boolean isEmpty = ingredient == Ingredient.EMPTY;
		if (!isEmpty && ingredient.getMatchingStacks().length == 0) {
		    return null;
		}

		ItemStack stack = isEmpty ? ItemStack.EMPTY : ingredient.getMatchingStacks()[0].copy();
		ret.setInventorySlotContents(slot++, stack);
		if (recipeWidth > 0 && (slot % 3) % recipeWidth == 0) {
		    slot += 3 - recipeWidth;
		}
	    }

	    if (recipe instanceof IArcaneRecipe) {
		IArcaneRecipe arcane = (IArcaneRecipe) recipe;
		if (arcane.getCrystals() != null) {
		    for (ShardType shard : ShardType.values()) {
			if (shard.getMetadata() < 6 && arcane.getCrystals().getAmount(shard.getAspect()) > 0) {
			    ret.setInventorySlotContents(shard.getMetadata() + 9,
				    ThaumcraftApiHelper.makeCrystal(shard.getAspect(), arcane.getCrystals().getAmount(shard.getAspect())));
			}
		    }
		}
	    }

	    boolean matches = false;
	    try {
		matches = recipe.matches(ret, null);
	    }
	    catch (Exception ex) {
		if (BROKEN_RECIPES.add(recipe.getRegistryName())) {
		    ThaumcraftFix.instance.getLogger().error("Failed calling IRecipe#matches", ex);
		    ThaumcraftFix.instance.getLogger().error("Note: future errors with this recipe will not be logged");
		}
	    }

	    return matches ? ret : null;
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

    public static final Supplier<ITransformer> ASPECT_RECIPE_MATCHES = () -> {
	LabelNode afterRet = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/crafting/ThaumcraftCraftingManager",
				false,
				"getAspectsFromIngredients",
				Types.ASPECT_LIST,
				Types.NON_NULL_LIST, Types.ITEM_STACK, Types.I_RECIPE, Types.ARRAY_LIST
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.I_RECIPE.getInternalName(),
			true,
			"func_179532_b",
			Types.NON_NULL_LIST,
			Types.INVENTORY_CRAFTING
			)))
		.insertInstructionsBefore(
			new InsnNode(Opcodes.POP),
			new MethodDefinition(
				HOOKS,
				false,
				"createFilledInventoryForRecipe",
				Types.INVENTORY_CRAFTING,
				Types.I_RECIPE
				).asMethodInsnNode(Opcodes.INVOKESTATIC),
			new InsnNode(Opcodes.DUP),
			new VarInsnNode(Opcodes.ASTORE, 7),
			new JumpInsnNode(Opcodes.IFNONNULL, afterRet),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new InsnNode(Opcodes.ARETURN),
			afterRet,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 7)
			)
		.build(), true, 1
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

    public static final Supplier<ITransformer> OBJ_MODEL_NO_SCALA = () -> new ThrowingTransformerWrapper(
	    new ChangeVariableTypeTransformer(new MethodDefinition(
		    "thaumcraft/client/lib/obj/MeshLoader",
		    false,
		    "addFace",
		    Type.VOID_TYPE,
		    Types.STRING
		    ),
		    Type.getType("Lscala/NotImplementedError;"),
		    Type.getType(UnsupportedOperationException.class),
		    false
		    ));

}
