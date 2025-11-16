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

package thecodex6824.thaumcraftfix.common.event;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApi.BluePrint;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.api.crafting.Part;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.lib.crafting.DustTriggerMultiblock;
import thaumcraft.common.lib.events.PlayerEvents;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.aura.CapabilityOriginalAuraInfo;
import thecodex6824.thaumcraftfix.api.aura.OriginalAuraInfo;
import thecodex6824.thaumcraftfix.common.network.PacketConfigSync;
import thecodex6824.thaumcraftfix.common.util.SimpleCapabilityProvider;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID)
public class CommonEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
	if (event.player instanceof EntityPlayerMP) {
	    PacketConfigSync packet = new PacketConfigSync(ThaumcraftFix.instance.getConfig().serializeNetwork());
	    ThaumcraftFix.instance.getNetworkHandler().sendTo(packet, (EntityPlayerMP) event.player);
	}
    }

    @SubscribeEvent
    public static void onFallFirst(LivingAttackEvent event) {
	if (event.getSource() == DamageSource.FALL) {
	    LivingHurtEvent fakeEvent = new LivingHurtEvent(event.getEntityLiving(), event.getSource(),
		    event.getAmount());
	    PlayerEvents.onFallDamage(fakeEvent);
	    if (fakeEvent.isCanceled()) {
		event.setCanceled(true);
	    }
	}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
	if (event.getUseBlock() != Result.DENY && event.getUseItem() != Result.DENY &&
		event.getItemStack().getItem() == ItemsTC.primalCrusher &&
		event.getFace() != EnumFacing.DOWN) {

	    World world = event.getWorld();
	    BlockPos originalPos = event.getPos();
	    if (world.getBlockState(originalPos).getBlock() == Blocks.GRASS &&
		    world.getBlockState(originalPos.up()).getMaterial() == Material.AIR) {

		EntityPlayer player = event.getEntityPlayer();
		if (!world.isRemote) {
		    MutableBlockPos pos = new MutableBlockPos(originalPos);
		    ItemStack stack = event.getItemStack();
		    ArrayList<BlockPos> pathPos = new ArrayList<>();
		    if (!player.isSneaking()) {
			for (int x = -1; x < 2; ++x) {
			    for (int z = -1; z < 2; ++z) {
				pos.setPos(originalPos.getX() + x, originalPos.getY(), originalPos.getZ() + z);
				if (world.getBlockState(pos).getBlock() == Blocks.GRASS &&
					world.getBlockState(pos.up()).getMaterial() == Material.AIR &&
					player.canPlayerEdit(pos.offset(event.getFace()), event.getFace(), stack)) {
				    pathPos.add(pos.toImmutable());
				}
			    }
			}
		    }
		    // only flatten 1 block if sneaking
		    else if (world.getBlockState(pos).getBlock() == Blocks.GRASS &&
			    world.getBlockState(pos.up()).getMaterial() == Material.AIR &&
			    player.canPlayerEdit(pos.offset(event.getFace()), event.getFace(), stack)) {
			pathPos.add(pos.toImmutable());
		    }

		    for (BlockPos newPath : pathPos) {
			world.setBlockState(newPath, Blocks.GRASS_PATH.getDefaultState(), BlockFlags.DEFAULT_AND_RERENDER);
			stack.damageItem(1, player);
			if (stack.isEmpty()) {
			    break;
			}
		    }

		    if (!pathPos.isEmpty()) {
			world.playSound(null, event.getPos(), SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
			event.setCanceled(true);
			event.setCancellationResult(EnumActionResult.SUCCESS);
			player.swingArm(event.getHand());
		    }
		}
		else {
		    player.swingArm(event.getHand());
		}
	    }
	}
    }

    @SubscribeEvent
    public static void onAttachCapabilityChunk(AttachCapabilitiesEvent<Chunk> event) {
	event.addCapability(new ResourceLocation(ThaumcraftFixApi.MODID, "original_aura_info"),
		new SimpleCapabilityProvider<>(new OriginalAuraInfo(), CapabilityOriginalAuraInfo.AURA_INFO));
    }

    private static Field TRIGGER_RESEARCH = null;

    private static String getDustTriggerResearch(DustTriggerMultiblock trigger) {
	try {
	    if (TRIGGER_RESEARCH == null) {
		TRIGGER_RESEARCH = DustTriggerMultiblock.class.getDeclaredField("research");
		TRIGGER_RESEARCH.setAccessible(true);
	    }
	    return (String) TRIGGER_RESEARCH.get(trigger);
	}
	catch (Exception ex) {
	    FMLCommonHandler.instance().raiseException(ex, "Failed to access or invoke Thaumcraft's DustTriggerMultiblock#research", true);
	    return null;
	}
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
	/**
	 * The multiblock recipes registered by TC require a pedestal with a meta value
	 * of 1 for ancient and 2 for eldritch. However, the meta value is for the redstone inlay power
	 * going through the pedestal (the arcane/ancient/eldritch pillars are their own blocks).
	 * Removing the metadata requirement fixes the multiblocks. This was probably an oversight in
	 * the last few betas (since the stablizier mechanics were reworked).
	 */

	Part matrix = new Part(BlocksTC.infusionMatrix, null);

	Part ancientStone = new Part(BlocksTC.stoneAncient, "AIR");
	Part ancientPillarEast = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.EAST)));
	Part ancientPillarNorth = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.NORTH)));
	Part ancientPillarSouth = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.SOUTH)));
	Part ancientPillarWest = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.WEST)));
	Part ancientPedestal = new Part(BlocksTC.pedestalAncient, null);
	Part[][][] ancientBlueprint = new Part[][][] {
	    {
		{null, null, null},
		{null, matrix, null},
		{null, null, null}
	    },
	    {
		{ancientStone, null, ancientStone},
		{null, null, null},
		{ancientStone, null, ancientStone}
	    },
	    {
		{ancientPillarEast, null, ancientPillarNorth},
		{null, ancientPedestal, null},
		{ancientPillarSouth, null, ancientPillarWest}
	    }
	};

	// IDustTrigger stores an ArrayList of triggers, so we need to find / remove ourselves
	for (int i = 0; i < IDustTrigger.triggers.size(); ++i) {
	    IDustTrigger trigger = IDustTrigger.triggers.get(i);
	    if (trigger instanceof DustTriggerMultiblock && getDustTriggerResearch((DustTriggerMultiblock) trigger).equals("INFUSIONANCIENT")) {
		IDustTrigger.triggers.remove(i);
		break;
	    }
	}

	IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONANCIENT", ancientBlueprint));
	// the catalog is just a map so this is ok
	ThaumcraftApi.addMultiblockRecipeToCatalog(new ResourceLocation("thaumcraft", "infusionaltarancient"), new BluePrint("INFUSIONANCIENT", ancientBlueprint, new ItemStack[] {
		new ItemStack(BlocksTC.stoneAncient, 8),
		new ItemStack(BlocksTC.pedestalAncient),
		new ItemStack(BlocksTC.infusionMatrix)
	}));

	Part eldritchStone = new Part(BlocksTC.stoneEldritchTile, "AIR");
	Part eldritchPillarEast = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.EAST)));
	Part eldritchPillarNorth = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.NORTH)));
	Part eldritchPillarSouth = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.SOUTH)));
	Part eldritchPillarWest = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.WEST)));
	Part eldritchPedestal = new Part(BlocksTC.pedestalEldritch, null);
	Part[][][] eldritchBlueprint = new Part[][][] {
	    {
		{null, null, null},
		{null, matrix, null},
		{null, null, null}
	    },
	    {
		{eldritchStone, null, eldritchStone},
		{null, null, null},
		{eldritchStone, null, eldritchStone}
	    },
	    {
		{eldritchPillarEast, null, eldritchPillarNorth},
		{null, eldritchPedestal, null},
		{eldritchPillarSouth, null, eldritchPillarWest}
	    }
	};

	for (int i = 0; i < IDustTrigger.triggers.size(); ++i) {
	    IDustTrigger trigger = IDustTrigger.triggers.get(i);
	    if (trigger instanceof DustTriggerMultiblock && getDustTriggerResearch((DustTriggerMultiblock) trigger).equals("INFUSIONELDRITCH")) {
		IDustTrigger.triggers.remove(i);
		break;
	    }
	}

	IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONELDRITCH", eldritchBlueprint));
	ThaumcraftApi.addMultiblockRecipeToCatalog(new ResourceLocation("thaumcraft", "infusionaltareldritch"), new BluePrint("INFUSIONELDRITCH", eldritchBlueprint, new ItemStack[] {
		new ItemStack(BlocksTC.stoneEldritchTile, 8),
		new ItemStack(BlocksTC.pedestalEldritch),
		new ItemStack(BlocksTC.infusionMatrix)
	}));

    }

	/**
	 * Fixes an internal logic bug with Thaumcraft preventing players from receiving exploration research if they already
	 * had other research completed.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLivingTickLate(LivingEvent.LivingUpdateEvent event) {
		if(!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().ticksExisted % 200 == 0) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
			Biome biome = player.world.getBiome(player.getPosition());

			//Fixes bug caused when rewarding research with command locking players out of this research.
			if (knowledge.isResearchKnown("UNLOCKAUROMANCY@1")) {
				if (player.posY < (double)10.0F && !knowledge.isResearchKnown("m_deepdown")) {
					knowledge.addResearch("m_deepdown");
					knowledge.sync((EntityPlayerMP)player);
					player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("got.deepdown")), true);
				}

				if (player.posY > (double)player.getEntityWorld().getActualHeight() * 0.4 && !knowledge.isResearchKnown("m_uphigh")) {
					knowledge.addResearch("m_uphigh");
					knowledge.sync((EntityPlayerMP)player);
					player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("got.uphigh")), true);
				}
			}

			//Fixes Thaumcraft not granting players exploration research correctly
			if (!knowledge.isResearchKnown("m_finddesert") && BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)) {
				knowledge.addResearch("m_finddesert");
				knowledge.sync((EntityPlayerMP) player);
				player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + net.minecraft.util.text.translation.I18n.translateToLocal("got.finddesert")), true);
			}
			if (!knowledge.isResearchKnown("m_findocean") && BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
				knowledge.addResearch("m_findocean");
				knowledge.sync((EntityPlayerMP) player);
				player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + net.minecraft.util.text.translation.I18n.translateToLocal("got.findocean")), true);
			}
		}
	}

}
