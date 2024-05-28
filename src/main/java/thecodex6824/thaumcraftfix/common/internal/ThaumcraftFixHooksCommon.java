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

package thecodex6824.thaumcraftfix.common.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import baubles.api.cap.BaublesCapabilities;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.container.ContainerThaumatorium;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.network.misc.PacketLogisticsRequestToServer;
import thaumcraft.common.lib.network.misc.PacketNote;
import thaumcraft.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.casting.IContainsFocusPackageNode;
import thecodex6824.thaumcraftfix.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumcraftfix.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumcraftfix.common.network.PacketGainKnowledge;
import thecodex6824.thaumcraftfix.common.network.PacketGainResearch;
import thecodex6824.thaumcraftfix.core.transformer.custom.PacketNoteHandlerRewriteTransformer;

@SuppressWarnings("unused")
public final class ThaumcraftFixHooksCommon {

    private ThaumcraftFixHooksCommon() {}

    public static int isInOuterLands(int entityDim, Entity entity) {
	EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(entity);
	MinecraftForge.EVENT_BUS.post(event);
	boolean pass = event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT &&
		entity.getEntityWorld().provider.getDimension() == ModConfig.CONFIG_WORLD.dimensionOuterId);
	// if we want the check to pass, we return the entity dimension so the condition on TC's side passes
	// otherwise, we pass a different dimension so the check will fail
	return pass ? entityDim : entityDim + 1;
    }

    public static boolean shouldAllowRunicShield(ItemStack stack) {
	return stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
    }

    public static boolean fireFluxRiftDestroyBlockEvent(EntityFluxRift rift, BlockPos pos, IBlockState state) {
	return MinecraftForge.EVENT_BUS.post(new FluxRiftDestroyBlockEvent(rift, pos, state));
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

    public static boolean shouldFocalManipulatorClearState(boolean originalDecision, TileFocalManipulator tile, ItemStack previousFocusStack) {
	return originalDecision && (tile.vis <= 0.0F || !previousFocusStack.isEmpty());
    }

    public static boolean shouldFocalManipulatorClearStateInverted(boolean originalDecisionInverted, TileFocalManipulator tile, ItemStack previousFocusStack) {
	return !shouldFocalManipulatorClearState(!originalDecisionInverted, tile, previousFocusStack);
    }

    public static int modifyManipulatorComponentCount(int originalCount, TileFocalManipulator tile, AspectList crystals, EntityPlayer crafter) {
	int result = crafter.isCreative() ? -1 : originalCount;
	if (result < 0) {
	    // we need to set this ourselves since the tile will now be skipping it
	    tile.crystalsSync = crystals.copy();
	}

	return result;
    }

    public static boolean checkFocusComplexity(TileFocalManipulator tile, EntityPlayer player, int maxComplexity, int computedComplexity) {
	boolean result = true;
	if (computedComplexity > maxComplexity) {
	    result = false;
	    tile.vis = 0.0F;
	    ThaumcraftFix.instance.getLogger().warn("Player {} ({}) tried to make a focus of complexity {} when the focus has a maximum complexity of {}",
		    player.getName(), player.getUniqueID().toString(), computedComplexity, maxComplexity);
	}

	return result;
    }

    private static Collection<FocusPackage> getEmbeddedPackages(IFocusElement node) {
	Collection<FocusPackage> embeddedPackages = null;
	if (node instanceof IContainsFocusPackageNode) {
	    embeddedPackages = ((IContainsFocusPackageNode) node).getEmbeddedPackages();
	}
	else if (node instanceof FocusModSplit) {
	    embeddedPackages = ((FocusModSplit) node).getSplitPackages();
	}

	return embeddedPackages;
    }

    public static void initializeFocusPackage(FocusPackage focusPackage, EntityLivingBase caster) {
	focusPackage.setCasterUUID(caster.getUniqueID());
	for (IFocusElement node : focusPackage.nodes) {
	    Collection<FocusPackage> embeddedPackages = getEmbeddedPackages(node);
	    if (embeddedPackages != null) {
		embeddedPackages.forEach(p -> p.initialize(caster));
	    }
	}
    }

    public static void setFocusPackageCasterUUID(FocusPackage focusPackage, UUID caster) {
	for (IFocusElement node : focusPackage.nodes) {
	    Collection<FocusPackage> embeddedPackages = getEmbeddedPackages(node);
	    if (embeddedPackages != null) {
		embeddedPackages.forEach(p -> p.setCasterUUID(caster));
	    }
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

    public static void sendKnowledgeGainPacket(EntityPlayer player, EnumKnowledgeType type, ResearchCategory category, int amount) {
	if (player instanceof EntityPlayerMP) {
	    ThaumcraftFix.instance.getNetworkHandler().sendTo(
		    new PacketGainKnowledge(type, category, amount), (EntityPlayerMP) player);
	}
    }

    public static void sendResearchGainPacket(EntityPlayer player, String researchKey) {
	if (player instanceof EntityPlayerMP) {
	    ThaumcraftFix.instance.getNetworkHandler().sendTo(
		    new PacketGainResearch(researchKey), (EntityPlayerMP) player);
	}
    }

    public static BlockFaceShape getTableBlockFaceShape(BlockFaceShape original, EnumFacing side) {
	return side == EnumFacing.UP && original == BlockFaceShape.UNDEFINED ? BlockFaceShape.SOLID : original;
    }

    public static void fixupPlayerSound(EntityLivingBase player, SoundEvent sound, float volume, float pitch) {
	if (player instanceof EntityPlayerMP) {
	    // send the sound to the originating player, since the server won't do it
	    SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
	    ((EntityPlayerMP) player).connection.sendPacket(packet);
	}
    }

    public static Set<String> filterResearchAids(Set<String> toFilter, EntityPlayer player, TileEntity table) {
	// just in case we somehow get passed an ImmutableSet / to not clobber the input set
	Set<String> ret = new HashSet<>(toFilter);
	// Thaumcraft already checked this was a research table
	ret.retainAll(((TileResearchTable) table).checkSurroundingAids());
	return ret;
    }

    private static boolean validatePosition(EntityPlayerMP player, BlockPos target, String component) {
	if (!player.getEntityWorld().isBlockLoaded(target)) {
	    ThaumcraftFix.instance.getLogger().warn("Player {} ({}) sent {} for unloaded position {}",
		    player.getName(), player.getUniqueID().toString(), component, target.toString());
	    return false;
	}

	Vec3d start = player.getPositionEyes(1.0f);
	Vec3d end = new Vec3d(target).add(0.5, 0.5, 0.5);
	// we add 1 block extra to compensate for lag and so on
	double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;
	double distance = start.squareDistanceTo(end);
	if (distance > reach * reach) {
	    ThaumcraftFix.instance.getLogger().warn("Player {} ({}) sent {} for a position too far away. Pos = {}, distance = {}",
		    player.getName(), player.getUniqueID().toString(), component, target.toString(), Math.sqrt(distance));
	    return false;
	}

	return true;
    }

    private static Field logisticsRequestPosition;

    public static boolean validateLogisticsRequest(PacketLogisticsRequestToServer message, MessageContext ctx) throws Exception {
	if (logisticsRequestPosition == null) {
	    logisticsRequestPosition = PacketLogisticsRequestToServer.class.getDeclaredField("pos");
	    logisticsRequestPosition.setAccessible(true);
	}

	EntityPlayerMP player = ctx.getServerHandler().player;
	boolean ok = player.isEntityAlive();
	BlockPos pos = (BlockPos) logisticsRequestPosition.get(message);
	if (ok && pos != null) {
	    ok = validatePosition(player, pos, "logistics request");
	}

	return ok;
    }

    // this can be used by both client and server threads (and is created on network thread)
    // it will only be read after creation so volatile is ok
    private static volatile Method originalPacketNoteHandle;

    // these will only be used by server thread
    private static Field packetNoteDim;
    private static Field packetNoteX;
    private static Field packetNoteY;
    private static Field packetNoteZ;

    public static void handlePacketNote(PacketNote message, MessageContext ctx) throws Exception {
	if (originalPacketNoteHandle == null) {
	    originalPacketNoteHandle = PacketNote.class.getMethod(PacketNoteHandlerRewriteTransformer.ORIGINAL_METHOD_REDIRECT_NAME, PacketNote.class, MessageContext.class);
	}

	ThaumcraftFix.proxy.scheduleTask(ctx.side, () -> {
	    try {
		boolean ok = true;
		if (ctx.side == Side.SERVER) {
		    EntityPlayerMP player = ctx.getServerHandler().player;
		    if (packetNoteDim == null) {
			packetNoteDim = PacketNote.class.getDeclaredField("dim");
			packetNoteDim.setAccessible(true);
		    }

		    int dim = packetNoteDim.getInt(message);
		    if (dim != player.dimension) {
			ok = false;
			ThaumcraftFix.instance.getLogger().warn("Player {} ({}) tried to get note status of a dimension they weren't in. Player dim = {}, requested dim = {}",
				player.getName(), player.getUniqueID().toString(), player.dimension, dim);
		    }

		    World world = player.getEntityWorld();
		    if (ok) {
			if (packetNoteX == null) {
			    packetNoteX = PacketNote.class.getDeclaredField("x");
			    packetNoteX.setAccessible(true);
			    packetNoteY = PacketNote.class.getDeclaredField("y");
			    packetNoteY.setAccessible(true);
			    packetNoteZ = PacketNote.class.getDeclaredField("z");
			    packetNoteZ.setAccessible(true);
			}

			BlockPos pos = new BlockPos(packetNoteX.getInt(message), packetNoteY.getInt(message), packetNoteZ.getInt(message));
			ok = validatePosition(player, pos, "note status");
		    }
		}

		if (ok) {
		    originalPacketNoteHandle.invoke(message, message, ctx);
		}
	    }
	    catch (Exception ex) {
		// all of the checked exceptions are reflection related and there is no recovery
		throw new RuntimeException(ex);
	    }
	});
    }

    private static Field thaumatoriumRecipePosition;
    private static Field containerThaumatoriumTile;

    public static boolean validateThaumatoriumSelection(PacketSelectThaumotoriumRecipeToServer message, MessageContext ctx) throws Exception {
	if (thaumatoriumRecipePosition == null) {
	    thaumatoriumRecipePosition = PacketSelectThaumotoriumRecipeToServer.class.getDeclaredField("pos");
	    thaumatoriumRecipePosition.setAccessible(true);
	}

	EntityPlayerMP player = ctx.getServerHandler().player;
	boolean ok = player.isEntityAlive();
	BlockPos pos = BlockPos.fromLong(thaumatoriumRecipePosition.getLong(message));
	if (ok && pos != null) {
	    ok = validatePosition(player, pos, "thaumatorium recipe selection");
	    if (ok) {
		if (containerThaumatoriumTile == null) {
		    containerThaumatoriumTile = ContainerThaumatorium.class.getDeclaredField("thaumatorium");
		    containerThaumatoriumTile.setAccessible(true);
		}

		ok = player.openContainer instanceof ContainerThaumatorium &&
			((TileEntity) containerThaumatoriumTile.get(player.openContainer)).getPos().equals(pos);
	    }
	}

	return ok;
    }

    public static int checkProgressSyncStage(int originalStage, EntityPlayer player, ResearchEntry entry) {
	int logicStage = originalStage;
	if (logicStage < 0 && ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
	    logicStage = Integer.MAX_VALUE;
	}

	return logicStage;
    }

}
