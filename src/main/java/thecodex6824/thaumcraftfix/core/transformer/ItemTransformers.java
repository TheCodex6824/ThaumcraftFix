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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import baubles.api.cap.BaublesCapabilities;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.common.items.casters.ItemFocus;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.core.transformer.custom.ChangeEventPriorityTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.PrimordialPearlAnvilEventTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.PrimordialPearlDurabilityBarTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class ItemTransformers {

    public static final class Hooks {

	public static boolean shouldAllowRunicShield(ItemStack stack) {
	    return stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
	}

	private static int[] getValidMetadata(Item item) {
	    IntRBTreeSet visitedMeta = new IntRBTreeSet();
	    for (CreativeTabs tab : item.getCreativeTabs()) {
		NonNullList<ItemStack> stacks = NonNullList.create();
		item.getSubItems(tab, stacks);
		for (ItemStack stack : stacks) {
		    if (stack.getItem() == item)
			visitedMeta.add(stack.getMetadata());
		}
	    }

	    return visitedMeta.toIntArray();
	}

	public static ItemStack cycleItemStack(ItemStack fallback, Object thing, int counter) {
	    if (thing instanceof ItemStack) {
		ItemStack stack = (ItemStack) thing;
		if (!stack.isEmpty() && stack.getHasSubtypes() && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
		    int[] validMeta = getValidMetadata(stack.getItem());
		    if (validMeta.length > 0) {
			int timer = 5000 / validMeta.length;
			int metaIndex = (int) ((counter + System.currentTimeMillis() / timer) % validMeta.length);
			ItemStack copy = stack.copy();
			copy.setItemDamage(validMeta[metaIndex]);
			return copy;
		    }
		}
	    }

	    return fallback;
	}

	public static void setFocusStackColor(ItemStack maybeFocus) {
	    Item item = maybeFocus.getItem();
	    if (item instanceof ItemFocus) {
		((ItemFocus) item).getFocusColor(maybeFocus);
	    }
	}

	public static int nullCheckTags(NBTTagCompound prime, NBTTagCompound other) {
	    int result = 0;
	    if (prime == null) {
		result = 0b10;
	    }
	    else if (other == null) {
		result = 0b1;
	    }

	    // what is returned here has 2 meanings:
	    // if nonzero, we want to exit early - return value will be this shifted right by 1
	    // this lets us effectively return 2 booleans from 1 function, since internally booleans are just ints
	    return result;
	}

	public static void fixPrimordialPearlItem(Item pearl) {
	    pearl.setMaxDamage(0);
	    ((RegistrySimple<?, ?>) pearl.properties).registryObjects.remove(new ResourceLocation("damage"));
	    ((RegistrySimple<?, ?>) pearl.properties).registryObjects.remove(new ResourceLocation("damaged"));
	    pearl.setHasSubtypes(true);
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    public static final Supplier<ITransformer> COMPARE_TAGS_RELAXED_NULL_CHECK = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/api/ThaumcraftInvHelper",
				false,
				"compareTagsRelaxed",
				Type.BOOLEAN_TYPE,
				Types.NBT_TAG_COMPOUND, Types.NBT_TAG_COMPOUND
				)
			)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"nullCheckTags",
				Type.getMethodDescriptor(Type.INT_TYPE, Types.NBT_TAG_COMPOUND, Types.NBT_TAG_COMPOUND),
				false
				),
			new InsnNode(Opcodes.DUP),
			new JumpInsnNode(Opcodes.IFEQ, newLabel),
			new InsnNode(Opcodes.ICONST_1),
			new InsnNode(Opcodes.IUSHR),
			new InsnNode(Opcodes.IRETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] { Opcodes.INTEGER }),
			new InsnNode(Opcodes.POP)
			)
		.build(), true, 1
		);
    };

    // to allow wildcard metadata in required research items when they are non-damageable
    public static final ITransformer CYCLE_ITEM_NON_DAMAGEABLE = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    "thaumcraft/common/lib/utils/InventoryUtils",
			    false,
			    "cycleItemStack",
			    Types.ITEM_STACK,
			    Types.OBJECT, Type.INT_TYPE
			    )
		    )
	    .findNextOpcode(Opcodes.ARETURN)
	    .insertInstructionsBefore(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new VarInsnNode(Opcodes.ILOAD, 1),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS,
			    "cycleItemStack",
			    Type.getMethodDescriptor(Types.ITEM_STACK, Types.ITEM_STACK, Types.OBJECT, Type.INT_TYPE),
			    false
			    )
		    )
	    .build()
	    );

    public static final Supplier<ITransformer> FOCUS_COLOR_NBT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/items/casters/ItemFocus",
				false,
				"setPackage",
				Type.VOID_TYPE,
				Types.ITEM_STACK, Types.FOCUS_PACKAGE
				)
			)
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"setFocusStackColor",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ITEM_STACK),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> INFUSION_ENCHANTMENT_DROPS_PRIORITY = () -> {
	return new ChangeEventPriorityTransformer(Types.TOOL_EVENTS, ImmutableMap.of(
		new MethodDefinition(Types.TOOL_EVENTS.getInternalName(), false, "harvestBlockEvent",
			Type.VOID_TYPE, Type.getType("Lnet/minecraftforge/event/world/BlockEvent$HarvestDropsEvent;")), "LOWEST",
		new MethodDefinition(Types.TOOL_EVENTS.getInternalName(), false, "livingDrops",
			Type.VOID_TYPE, Type.getType("Lnet/minecraftforge/event/entity/living/LivingDropsEvent;")), "LOWEST"
		));
    };

    public static final Supplier<ITransformer> PRIMORDIAL_PEARL_ANVIL_DUPE_DURABILITY_BAR = () -> new ThrowingTransformerWrapper(
	    new PrimordialPearlDurabilityBarTransformer());

    public static final Supplier<ITransformer> PRIMORDIAL_PEARL_ANVIL_DUPE_EVENT = () -> new ThrowingTransformerWrapper(
	    new PrimordialPearlAnvilEventTransformer());

    public static final Supplier<ITransformer> PRIMORDIAL_PEARL_ANVIL_DUPE_PROPS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/items/curios/ItemPrimordialPearl",
				false,
				"<init>",
				Type.VOID_TYPE
				)
			)
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"fixPrimordialPearlItem",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ITEM),
				false
				)
			)
		.build()
		);
    };

    // makes runic shielding infusion work on items with baubles capability
    // TC only checks for the interface on the item...
    public static final ITransformer RUNIC_SHIELD_INFUSION_BAUBLE_CAP = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    "thaumcraft/common/lib/crafting/InfusionRunicAugmentRecipe",
			    false,
			    "matches",
			    Type.BOOLEAN_TYPE,
			    Type.getType("Ljava/util/List;"), Types.ITEM_STACK, Types.WORLD, Types.ENTITY_PLAYER
			    )
		    )
	    .findNext(node -> node.getOpcode() == Opcodes.INSTANCEOF && node instanceof TypeInsnNode && ((TypeInsnNode) node).desc.equals("baubles/api/IBauble") && node.getNext() instanceof JumpInsnNode)
	    .insertInstructions((node, matches) -> {
		InsnList toAdd = new InsnList();
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
		toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			HOOKS,
			"shouldAllowRunicShield",
			Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ITEM_STACK),
			false
			));
		toAdd.add(new JumpInsnNode(Opcodes.IFNE, ((JumpInsnNode) matches.get(0).matchStart().getNext()).label));

		ImmutableList<AbstractInsnNode> added = ImmutableList.copyOf(toAdd.iterator());
		node.instructions.insert(matches.get(0).matchStart().getNext(), toAdd);
		return added;
	    })
	    .build()
	    );

}
