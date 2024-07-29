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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Streams;
import com.google.common.math.DoubleMath;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusNode.EnumSupplyType;
import thaumcraft.common.container.ContainerArcaneWorkbench;
import thaumcraft.common.tiles.crafting.FocusElementNode;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;
import thaumcraft.common.tiles.devices.TileInfernalFurnace;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.LocalVariableDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.core.transformer.custom.BlockApplyOffsetTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.BlockPillarDropFixTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;

public class BlockTransformers {

    public static final class HooksCommon {

	public static int modifyManipulatorComponentCount(int originalCount, TileFocalManipulator tile, AspectList crystals, EntityPlayer crafter) {
	    int result = crafter.isCreative() ? -1 : originalCount;
	    if (result < 0) {
		// we need to set this ourselves since the tile will now be skipping it
		tile.crystalsSync = crystals.copy();
	    }

	    return result;
	}

	public static int recalcManipulatorXpCost(int totalComplexity) {
	    return (int) Math.max(1, Math.round(Math.sqrt(totalComplexity)));
	}

	public static BlockFaceShape getTableBlockFaceShape(BlockFaceShape original, EnumFacing side) {
	    return side == EnumFacing.UP && original == BlockFaceShape.UNDEFINED ? BlockFaceShape.SOLID : original;
	}

	public static boolean isArcaneWorkbenchNotAllowed(boolean original, World world, BlockPos pos, EntityPlayer player) {
	    // original will be true if this call happened on the client
	    boolean notAllowed = original;
	    if (!notAllowed) {
		TileEntity tile = world.getTileEntity(pos);
		notAllowed = !(tile instanceof TileArcaneWorkbench) ||
			((TileArcaneWorkbench) tile).inventoryCraft.eventHandler instanceof ContainerArcaneWorkbench;
		if (notAllowed && player != null) {
		    player.sendStatusMessage(new TextComponentTranslation("thaumcraftfix.alreadyinuse")
			    .setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)), true);
		}
	    }

	    return notAllowed;
	}

	public static boolean isArcaneWorkbenchNotAllowedForCharger(boolean original, World world, BlockPos pos, EntityPlayer player) {
	    boolean notAllowed = original;
	    if (!notAllowed) {
		BlockPos real = pos.down();
		IBlockState state = world.getBlockState(real);
		notAllowed = state.getBlock() == BlocksTC.arcaneWorkbench && isArcaneWorkbenchNotAllowed(original, world, real, player);
	    }

	    return notAllowed;
	}

	public static Iterator<FocusElementNode> getNodesInTree(Iterator<FocusElementNode> nodeIterator,
		HashMap<Integer, FocusElementNode> allNodes, int selectedNode) {
	    // this is probably never going to get used, but for completeness:
	    // this solution will hopefully allow using remove and so on with the iterator and have it do the right thing
	    Set<FocusElementNode> allowedNodes = Collections.newSetFromMap(new IdentityHashMap<FocusElementNode, Boolean>());
	    FocusElementNode original = allNodes.get(selectedNode);
	    FocusElementNode cursor = original;
	    allowedNodes.add(cursor);
	    while (cursor.parent != -1) {
		cursor = allNodes.get(cursor.parent);
		allowedNodes.add(cursor);
	    }

	    ArrayDeque<FocusElementNode> nodesStack = new ArrayDeque<>();
	    nodesStack.add(original);
	    while (!nodesStack.isEmpty()) {
		FocusElementNode node = nodesStack.pop();
		allowedNodes.add(node);
		for (int child : node.children) {
		    nodesStack.push(allNodes.get(child));
		}
	    }

	    return Streams.stream(nodeIterator).filter(allowedNodes::contains).iterator();
	}

	private static boolean canParentSupplyAll(FocusNode parent, FocusNode child) {
	    for (EnumSupplyType type : child.mustBeSupplied()) {
		if (!parent.canSupply(type)) {
		    return false;
		}
	    }

	    return true;
	}

	public static boolean checkFocus(TileFocalManipulator tile, EntityPlayer player, int maxComplexity, int computedComplexity) {
	    boolean result = true;
	    Logger logger = ThaumcraftFix.instance.getLogger();
	    if (computedComplexity > maxComplexity) {
		result = false;
		logger.warn("Player {} ({}) tried to make a focus of complexity {} when the focus has a maximum complexity of {}",
			player.getName(), player.getUniqueID().toString(), computedComplexity, maxComplexity);
	    }

	    if (result) {
		List<FocusElementNode> leafNodes = tile.data.values().stream()
			.filter(n -> n.children.length == 0)
			.collect(Collectors.toList());
		for (FocusElementNode leaf : leafNodes) {
		    boolean rootFound = false;
		    boolean mediumFound = false;
		    HashSet<String> bannedNodes = new HashSet<>();
		    while (leaf != null && leaf.node != null) {
			FocusElementNode directParent = leaf.parent != -1 ? tile.data.get(leaf.parent) : null;
			if (!ThaumcraftCapabilities.knowsResearchStrict(player, leaf.node.getResearch())) {
			    result = false;
			    logger.warn("Player {} ({}) tried to make a focus without required research {}",
				    player.getName(), player.getUniqueID().toString(), leaf.node.getResearch());
			    break;
			}
			else if ((directParent != null && directParent.node != null && !canParentSupplyAll(directParent.node, leaf.node)) ) {
			    result = false;
			    logger.warn("Player {} ({}) tried to make a focus with an invalid parent -> child node combo {} -> {}",
				    player.getName(), player.getUniqueID().toString(), directParent.node.getKey(), leaf.node.getKey());
			    break;
			}
			else if (leaf.node.getKey().equals("ROOT")) {
			    if (rootFound) {
				result = false;
				logger.warn("Player {} ({}) tried to make a focus with multiple root nodes",
					player.getName(), player.getUniqueID().toString());
				break;
			    }

			    rootFound = true;
			}
			else if (leaf.node instanceof FocusMedium &&
				!bannedNodes.isEmpty() && (leaf.node.isExclusive() || mediumFound)) {

			    result = false;
			    logger.warn("Player {} ({}) tried to make a focus with illegal mediums",
				    player.getName(), player.getUniqueID().toString());
			}
			else if (leaf.node.isExclusive() && !bannedNodes.add(leaf.node.getKey())) {
			    result = false;
			    logger.warn("Player {} ({}) tried to make a focus with illegal nodes",
				    player.getName(), player.getUniqueID().toString());
			    break;
			}
			else if (leaf.node instanceof FocusMedium) {
			    mediumFound = true;
			}

			leaf = directParent;
		    }
		}
	    }

	    if (!result) {
		tile.vis = 0.0F;
	    }

	    return result;
	}

	public static float handleFocalManipulatorVis(float vis) {
	    if (DoubleMath.fuzzyEquals(vis, 0.0, 1.0E-4)) {
		vis = 0.0F;
	    }

	    return vis;
	}

	public static ItemStack passStackToFurnace(ItemStack original, Entity item) {
	    return item.isDead ? ItemStack.EMPTY : original;
	}

	public static ItemStack getStackToSet(ItemStack original, Entity item) {
	    item.setDead();
	    return ((EntityItem) item).getItem();
	}

	public static void destroyItemEffectsThatActuallyWork(TileInfernalFurnace furnace) {
	    ThreadLocalRandom rand = ThreadLocalRandom.current();
	    BlockPos pos = furnace.getPos();
	    World world = furnace.getWorld();
	    world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH,
		    SoundCategory.BLOCKS, 0.3F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
	    for (int i = 0; i < 16; ++i) {
		float pX = pos.getX() + rand.nextFloat();
		float pY = pos.getY() + 0.5F;
		float pZ = pos.getZ() + rand.nextFloat();
		SPacketParticles packet = new SPacketParticles(EnumParticleTypes.LAVA, false, pX, pY, pZ,
			0.0F, 0.0F, 0.0F, 0.0F, rand.nextInt(1) + 1);
		for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
		    if (player.dimension == world.provider.getDimension()) {
			double dX = pX - player.posX;
			double dY = pY - (player.posY + player.eyeHeight);
			double dZ = pZ - player.posZ;
			if (dX * dX + dY * dY + dZ * dZ < 64 * 64) {
			    player.connection.sendPacket(packet);
			}
		    }
		}
	    }
	}

    }

    @SideOnly(Side.CLIENT)
    public static final class HooksClient {

	public static boolean modifyFocalManipulatorCraftValid(boolean origResult, int totalComplexity,
		int maxComplexity, TileFocalManipulator table, boolean emptyNodes, boolean validCrystals) {

	    EntityPlayer player = Minecraft.getMinecraft().player;
	    if (!player.isCreative()) {
		return origResult;
	    }

	    // waive crystal and xp requirement in creative
	    return totalComplexity <= maxComplexity && !emptyNodes;
	}

    }

    private static final String HOOKS_COMMON = Type.getInternalName(HooksCommon.class);

    @SideOnly(Side.CLIENT)
    private static final String HOOKS_CLIENT = Type.getInternalName(HooksClient.class);

    public static final Supplier<ITransformer> ARCANE_WORKBENCH_NO_CONCURRENT_USE = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/crafting/BlockArcaneWorkbench",
				false,
				"func_180639_a",
				Type.BOOLEAN_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY_PLAYER,
				Type.getType("Lnet/minecraft/util/EnumHand;"), Types.ENUM_FACING, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE
				)))
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.WORLD.getInternalName(),
			"field_72995_K",
			Type.BOOLEAN_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isArcaneWorkbenchNotAllowed",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.WORLD, Types.BLOCK_POS, Types.ENTITY_PLAYER),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> ARCANE_WORKBENCH_NO_CONCURRENT_USE_CHARGER = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/crafting/BlockArcaneWorkbenchCharger",
				false,
				"func_180639_a",
				Type.BOOLEAN_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY_PLAYER,
				Type.getType("Lnet/minecraft/util/EnumHand;"), Types.ENUM_FACING, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE
				)))
		.findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			Types.WORLD.getInternalName(),
			"field_72995_K",
			Type.BOOLEAN_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isArcaneWorkbenchNotAllowedForCharger",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.WORLD, Types.BLOCK_POS, Types.ENTITY_PLAYER),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> BRAIN_JAR_EAT_DELAY = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/devices/TileJarBrain",
				false,
				"func_73660_a",
				Type.VOID_TYPE
				)
				))
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition(
			"ents",
			Types.LIST
			))
		.findNextMethodCall(new MethodDefinition(
			"java/util/List",
			true,
			"iterator",
			Types.ITERATOR
			))
		.findNextOpcode(Opcodes.ASTORE)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new LdcInsnNode(10),
			new FieldDefinition(
				"thaumcraft/common/tiles/devices/TileJarBrain",
				"eatDelay",
				Type.INT_TYPE
				).asFieldInsnNode(Opcodes.PUTFIELD)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_FOCUS_SLOT = () -> {
	FieldDefinition vis = new FieldDefinition(
		Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
		"vis",
		Type.FLOAT_TYPE
		);
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				false,
				"func_70299_a",
				Type.VOID_TYPE,
				Type.INT_TYPE, Types.ITEM_STACK
				)
				))
		.findNextFieldAccess(vis)
		.insertInstructionsBefore(
			new InsnNode(Opcodes.POP),
			new VarInsnNode(Opcodes.ALOAD, 0),
			vis.asFieldInsnNode(Opcodes.GETFIELD)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_SERVER_CHECKS = () -> {
	LabelNode newLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				false,
				"startCraft",
				Type.BOOLEAN_TYPE,
				Type.INT_TYPE, Types.ENTITY_PLAYER
				)
			)
		.findConsecutive()
		.findNextOpcode(Opcodes.I2F)
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"vis",
			Type.FLOAT_TYPE)
			)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"checkFocus",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.TILE_FOCAL_MANIPULATOR, Types.ENTITY_PLAYER, Type.INT_TYPE, Type.INT_TYPE),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, newLabel),
			new InsnNode(Opcodes.ICONST_0),
			new InsnNode(Opcodes.IRETURN),
			newLabel,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)
		.build(), true, 1
		);
    };

    // makes focal manipulator not require any materials in creative mode
    public static final ITransformer FOCAL_MANIPULATOR_COMPONENTS = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			    false,
			    "startCraft",
			    Type.BOOLEAN_TYPE,
			    Type.INT_TYPE, Types.ENTITY_PLAYER
			    )
		    )
	    .findConsecutive()
	    .findNextLocalAccess(6)
	    .findNextOpcode(Opcodes.ARRAYLENGTH)
	    .endConsecutive()
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new InsnNode(Opcodes.DUP),
		    new FieldDefinition(
			    Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			    "crystals",
			    Type.getType("Lthaumcraft/api/aspects/AspectList;")
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 2),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_COMMON,
			    "modifyManipulatorComponentCount",
			    Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.TILE_FOCAL_MANIPULATOR,
				    Type.getType("Lthaumcraft/api/aspects/AspectList;"), Types.ENTITY_PLAYER),
			    false
			    )
		    )
	    .build(), true, 1
	    );

    private static final String GUI_MANIPULATOR_CLASS = "thaumcraft/client/gui/GuiFocalManipulator";

    public static final ITransformer FOCAL_MANIPULATOR_COMPONENTS_CLIENT = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    GUI_MANIPULATOR_CLASS,
			    false,
			    "gatherInfo",
			    Type.VOID_TYPE,
			    Type.BOOLEAN_TYPE
			    )
		    )
	    .findNextFieldAccess(new FieldDefinition(
		    GUI_MANIPULATOR_CLASS,
		    "valid",
		    Type.BOOLEAN_TYPE
		    ))
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new InsnNode(Opcodes.DUP),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "valid",
			    Type.BOOLEAN_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "totalComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "maxComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "table",
			    Types.TILE_FOCAL_MANIPULATOR
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new VarInsnNode(Opcodes.ILOAD, 4),
		    new VarInsnNode(Opcodes.ILOAD, 6),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_CLIENT,
			    "modifyFocalManipulatorCraftValid",
			    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE,
				    Types.TILE_FOCAL_MANIPULATOR, Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE),
			    false
			    ),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "valid",
			    Type.BOOLEAN_TYPE
			    ).asFieldInsnNode(Opcodes.PUTFIELD)
		    )
	    .build()
	    );

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_EXCLUSIVE_NODES_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				GUI_MANIPULATOR_CLASS,
				false,
				"gatherPartsList",
				Type.VOID_TYPE
				)
			)
		.findConsecutive()
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"data",
			Types.HASH_MAP
			))
		.findNextMethodCall(new MethodDefinition(
			Types.HASH_MAP.getInternalName(),
			false,
			"values",
			Types.COLLECTION
			))
		.findNextMethodCall(new MethodDefinition(
			Types.COLLECTION.getInternalName(),
			true,
			"iterator",
			Types.ITERATOR
			))
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				GUI_MANIPULATOR_CLASS,
				"table",
				Types.TILE_FOCAL_MANIPULATOR
				).asFieldInsnNode(Opcodes.GETFIELD),
			new FieldDefinition(
				Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
				"data",
				Types.HASH_MAP
				).asFieldInsnNode(Opcodes.GETFIELD),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldDefinition(
				GUI_MANIPULATOR_CLASS,
				"selectedNode",
				Type.INT_TYPE
				).asFieldInsnNode(Opcodes.GETFIELD),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getNodesInTree",
				Type.getMethodDescriptor(Types.ITERATOR, Types.ITERATOR, Types.HASH_MAP, Type.INT_TYPE),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> FOCAL_MANIPULATOR_VIS_FP_ISSUES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			false,
			"func_73660_a",
			Type.VOID_TYPE
			)))
		.findConsecutive()
		.findNextLocalAccess(2)
		.findNextOpcode(Opcodes.FSUB)
		.findNextFieldAccess(new FieldDefinition(
			Types.TILE_FOCAL_MANIPULATOR.getInternalName(),
			"vis",
			Type.FLOAT_TYPE
			))
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsBefore(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"handleFocalManipulatorVis",
				Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE),
				false
				)
			)
		.build()
		);
    };

    public static final ITransformer FOCAL_MANIPULATOR_XP_COST_GUI = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    GUI_MANIPULATOR_CLASS,
			    false,
			    "gatherInfo",
			    Type.VOID_TYPE,
			    Type.BOOLEAN_TYPE
			    )
		    )
	    .findNextFieldAccess(new FieldDefinition(
		    GUI_MANIPULATOR_CLASS,
		    "costXp",
		    Type.INT_TYPE
		    ))
	    .insertInstructionsBefore(
		    new InsnNode(Opcodes.POP),
		    new VarInsnNode(Opcodes.ALOAD, 0),
		    new FieldDefinition(
			    GUI_MANIPULATOR_CLASS,
			    "totalComplexity",
			    Type.INT_TYPE
			    ).asFieldInsnNode(Opcodes.GETFIELD),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_COMMON,
			    "recalcManipulatorXpCost",
			    Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE),
			    false
			    )
		    )
	    .build()
	    );

    public static final Supplier<ITransformer> INFERNAL_FURNACE_DESTROY_EFFECTS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/devices/TileInfernalFurnace",
				false,
				"destroyItem",
				Type.VOID_TYPE
				)))
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"destroyItemEffectsThatActuallyWork",
				Type.VOID_TYPE,
				Type.getType("Lthaumcraft/common/tiles/devices/TileInfernalFurnace;")
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> INFERNAL_FURNACE_ITEM_CHECKS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/devices/BlockInfernalFurnace",
				false,
				"func_180634_a",
				Type.VOID_TYPE,
				Types.WORLD, Types.BLOCK_POS, Types.I_BLOCK_STATE, Types.ENTITY
				)))
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/tiles/devices/TileInfernalFurnace",
			false,
			"addItemsToInventory",
			Types.ITEM_STACK,
			Types.ITEM_STACK
			))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"passStackToFurnace",
				Types.ITEM_STACK,
				Types.ITEM_STACK, Types.ENTITY
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ENTITY_ITEM.getInternalName(),
			false,
			"func_92058_a",
			Type.VOID_TYPE,
			Types.ITEM_STACK
			)))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodDefinition(
				HOOKS_COMMON,
				false,
				"getStackToSet",
				Types.ITEM_STACK,
				Types.ITEM_STACK, Types.ENTITY
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> PILLAR_DROP_FIX = () -> new ThrowingTransformerWrapper(
	    new BlockPillarDropFixTransformer());

    public static final Supplier<ITransformer> PLANT_CINDERPEARL_OFFSET = () -> new ThrowingTransformerWrapper(
	    new BlockApplyOffsetTransformer("thaumcraft/common/blocks/world/plants/BlockPlantCinderpearl"));

    public static final Supplier<ITransformer> PLANT_SHIMMERLEAF_OFFSET = () -> new ThrowingTransformerWrapper(
	    new BlockApplyOffsetTransformer("thaumcraft/common/blocks/world/plants/BlockPlantShimmerleaf"));

    public static final Supplier<ITransformer> TABLE_TOP_SOLID = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/blocks/basic/BlockTable",
				false,
				"func_193383_a",
				Types.BLOCK_FACE_SHAPE,
				Types.I_BLOCK_ACCESS, Types.I_BLOCK_STATE,
				Types.BLOCK_POS, Types.ENUM_FACING
				)
				))
		.findNextOpcode(Opcodes.ARETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 4),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getTableBlockFaceShape",
				Type.getMethodDescriptor(Types.BLOCK_FACE_SHAPE, Types.BLOCK_FACE_SHAPE, Types.ENUM_FACING),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> THAUMATORIUM_TOP_EMPTY = () -> {
	LabelNode jumpTarget = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/tiles/crafting/TileThaumatoriumTop",
				false,
				"func_191420_l",
				Type.BOOLEAN_TYPE
				)
				))
		.findConsecutive()
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/tiles/crafting/TileThaumatorium",
			false,
			"func_191420_l",
			Type.BOOLEAN_TYPE
			)))
		.findNextOpcode(Opcodes.IRETURN)
		.endConsecutive()
		.insertInstructionsSurrounding()
		.before(
			new InsnNode(Opcodes.DUP),
			new JumpInsnNode(Opcodes.IFNULL, jumpTarget)
			)
		.after(
			jumpTarget,
			new FrameNode(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "thaumcraft/common/tiles/crafting/TileThaumatorium" }),
			new InsnNode(Opcodes.POP),
			new InsnNode(Opcodes.ICONST_1),
			new InsnNode(Opcodes.IRETURN)
			)
		.endAction()
		.build(), true, 1
		);
    };

}
