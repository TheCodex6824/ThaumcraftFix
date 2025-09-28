/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Streams;
import com.google.common.math.DoubleMath;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
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
import thecodex6824.thaumcraftfix.ThaumcraftFix;

public class BlockTransformersHooksCommon {

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
			    !bannedNodes.isEmpty() && leaf.node.isExclusive() && mediumFound) {

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
	float pX = pos.getX() + rand.nextFloat();
	float pY = pos.getY() + 0.5F;
	float pZ = pos.getZ() + rand.nextFloat();
	SPacketParticles packet = new SPacketParticles(EnumParticleTypes.LAVA, false, pX, pY, pZ,
		0.0F, 0.0F, 0.0F, 0.0F, rand.nextInt(3) + 2);
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
