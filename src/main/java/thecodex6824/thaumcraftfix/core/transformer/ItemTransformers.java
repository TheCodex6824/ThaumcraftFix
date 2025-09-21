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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import baubles.api.cap.BaublesCapabilities;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.tools.ItemHandMirror;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.core.transformer.custom.ChangeEventPriorityTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class ItemTransformers {

    public static final class HooksCommon {

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

	public static ItemStack getHandMirrorStack(ItemStack original, InventoryPlayer playerInv) {
	    EntityPlayer player = playerInv.player;
	    ItemStack mirror = player.getHeldItemMainhand();
	    if (!(mirror.getItem() instanceof ItemHandMirror)) {
		mirror = player.getHeldItemOffhand();
		if (!(mirror.getItem() instanceof ItemHandMirror)) {
		    mirror = ItemStack.EMPTY;
		}
	    }

	    if (mirror.isEmpty()) {
		player.closeScreen();
	    }
	    return mirror;
	}

	public static int getItemConsumeAmount(int original, EntityLivingBase entity) {
	    return entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative() ?
		    0 : original;
	}

	public static boolean skipGivingPhial(EntityPlayer player) {
	    return player.isCreative();
	}

    }

    @SideOnly(Side.CLIENT)
    public static final class HooksClient {

	public static int fixupXSlot(int original, InventoryPlayer playerInv) {
	    ItemStack maybeMirror = playerInv.player.getHeldItemMainhand();
	    if (!(maybeMirror.getItem() instanceof ItemHandMirror)) {
		// so we don't have to check the slot every frame or have 2 patches,
		// just send the X to the shadow realm if we don't want it displayed
		original = -Minecraft.getMinecraft().displayWidth - 64;
	    }

	    return original;
	}

    }

    private static final String HOOKS_COMMON = Type.getInternalName(HooksCommon.class);

    @SideOnly(Side.CLIENT)
    private static final String HOOKS_CLIENT = Type.getInternalName(HooksClient.class);

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
				HOOKS_COMMON,
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
			    HOOKS_COMMON,
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
				HOOKS_COMMON,
				"setFocusStackColor",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ITEM_STACK),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> HAND_MIRROR_STACK_CONTAINER = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/container/ContainerHandMirror",
				false,
				"<init>",
				Type.VOID_TYPE,
				Types.INVENTORY_PLAYER, Types.WORLD, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.INVENTORY_PLAYER.getInternalName(),
			false,
			"func_70448_g",
			Types.ITEM_STACK
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getHandMirrorStack",
				Type.getMethodDescriptor(Types.ITEM_STACK, Types.ITEM_STACK, Types.INVENTORY_PLAYER),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> HAND_MIRROR_STACK_GUI = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/client/gui/GuiHandMirror",
				false,
				"<init>",
				Type.VOID_TYPE,
				Types.INVENTORY_PLAYER, Types.WORLD, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE
				)
			)
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.INVENTORY_PLAYER.getInternalName(),
			"field_70461_c",
			Type.INT_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_CLIENT,
				"fixupXSlot",
				Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.INVENTORY_PLAYER),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> INFUSION_ENCHANTMENT_DROPS_PRIORITY = () -> {
	return new ThrowingTransformerWrapper(new ChangeEventPriorityTransformer(Types.TOOL_EVENTS, ImmutableMap.of(
		new MethodDefinition(Types.TOOL_EVENTS.getInternalName(), false, "harvestBlockEvent",
			Type.VOID_TYPE, Type.getType("Lnet/minecraftforge/event/world/BlockEvent$HarvestDropsEvent;")), "LOWEST",
		new MethodDefinition(Types.TOOL_EVENTS.getInternalName(), false, "livingDrops",
			Type.VOID_TYPE, Type.getType("Lnet/minecraftforge/event/entity/living/LivingDropsEvent;")), "LOWEST"
		)));
    };

    public static final Supplier<ITransformer> PHIAL_CONSUMPTION_CREATIVE = () -> {
	LabelNode afterSpawn = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/items/consumables/ItemPhial",
			false,
			"onItemUseFirst",
			Types.ENUM_ACTION_RESULT,
			Types.ENTITY_PLAYER, Types.WORLD, Types.BLOCK_POS, Types.ENUM_FACING,
			Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Types.ENUM_HAND
			)))
		// set the stack shrink amount to 0
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ITEM_STACK.getInternalName(),
			false,
			"func_190918_g",
			Type.VOID_TYPE,
			Type.INT_TYPE
			)))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getItemConsumeAmount",
				Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.ENTITY_LIVING_BASE),
				false
				)
			)
		// jump over giving the phial to the player
		// this works by finding the label after the spawnEntity call, which
		// should also be a jump target from the addItemStackToInventory call
		// this means we get the frame "for free", which is good because
		// the frames for all 3 patch sites are different
		// we have to find the label for it and not just put it after spawnEntity
		// because some sites have extra pops etc after that call
		.findConsecutive()
		.findNextLocalAccess(1)
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.ENTITY_PLAYER.getInternalName(),
			"field_71071_by",
			Types.INVENTORY_PLAYER
			)))
		.endConsecutive()
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.WORLD.getInternalName(),
			false,
			"func_72838_d",
			Type.BOOLEAN_TYPE,
			Types.ENTITY
			)))
		.findNextInstructionType(LabelNode.class)
		.combineLastTwoMatches()
		.combineLastTwoMatches()
		.insertInstructionsSurrounding()
		.before(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"skipGivingPhial",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ENTITY_PLAYER),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, afterSpawn)
			)
		.after(afterSpawn)
		.endAction()
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
			HOOKS_COMMON,
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

    public static final Supplier<ITransformer> SANITY_SOAP_CREATIVE = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/items/consumables/ItemSanitySoap",
			false,
			"func_77615_a",
			Type.VOID_TYPE,
			Types.ITEM_STACK, Types.WORLD, Types.ENTITY_LIVING_BASE, Type.INT_TYPE
			)))
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ITEM_STACK.getInternalName(),
			false,
			"func_190918_g",
			Type.VOID_TYPE,
			Type.INT_TYPE
			)))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 3),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getItemConsumeAmount",
				Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.ENTITY_LIVING_BASE),
				false
				)
			)
		.build()
		);
    };

    // this is here due to reported classloading issues
    // this is not public API
    public static boolean elementalShovelWrapSetBlockStateForBlockPlacement(World world, BlockPos pos, IBlockState state, Operation<Boolean> op,
	    EntityPlayer player, World worldAgain, BlockPos origPos, EnumHand hand, EnumFacing side,
	    float hitX, float hitY, float hitZ, LocalRef<ItemStack> goodItem, LocalBooleanRef didSomething) {

	boolean result = false;
	ItemStack item = goodItem.get();
	float modHitX = hitX + (pos.getX() - origPos.getX());
	float modHitY = hitY + (pos.getY() - origPos.getY());
	float modHitZ = hitZ + (pos.getZ() - origPos.getZ());
	IBlockState toPlace = state.getBlock().getStateForPlacement(world, pos, side, modHitX, modHitY,
		modHitZ, item.getMetadata(), player, hand);
	IBlockState existing = world.getBlockState(pos);
	if (existing.getBlock().isReplaceable(world, pos) && player.canPlayerEdit(pos, side, item) &&
		world.mayPlace(state.getBlock(), pos, false, side, player)) {

	    if (item.getItem() instanceof ItemBlock) {
		ItemBlock itemBlock = (ItemBlock) item.getItem();
		result = itemBlock.placeBlockAt(item, player, world, pos, side,
			modHitX, modHitY, modHitZ, toPlace);
	    }
	    else {
		result = op.call(toPlace, pos);
	    }
	}

	if (result) {
	    didSomething.set(true);
	}

	return result;
    }

}
