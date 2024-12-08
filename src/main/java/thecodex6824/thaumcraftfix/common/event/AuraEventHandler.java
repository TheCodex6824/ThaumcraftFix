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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraThread;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.aura.CapabilityAuraProcessor;
import thecodex6824.thaumcraftfix.api.aura.DefaultAuraProcessor;
import thecodex6824.thaumcraftfix.common.aura.IListeningAuraThread;
import thecodex6824.thaumcraftfix.common.aura.RiftTriggerEvent;
import thecodex6824.thaumcraftfix.common.util.SimpleCapabilityProvider;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID)
public class AuraEventHandler {

    @SubscribeEvent
    public static void onWorldTick(WorldTickEvent event) {
	if (event.phase == Phase.END && !event.world.isRemote) {
	    World world = event.world;
	    AuraThread thread = ServerEvents.auraThreads.get(world.provider.getDimension());
	    if (thread instanceof IListeningAuraThread) {
		((IListeningAuraThread) thread).notifyUpdate(world);
	    }
	}
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
	if (!event.getWorld().isRemote) {
	    World world = event.getWorld();
	    AuraThread thread = ServerEvents.auraThreads.get(world.provider.getDimension());
	    if (thread instanceof IListeningAuraThread) {
		((IListeningAuraThread) thread).unloadChunk(world, event.getChunk());
	    }
	}
    }

    @SubscribeEvent
    public static void onRiftTrigger(RiftTriggerEvent event) {
	BlockPos pos = event.getPosition();
	if (event.useInexactSpawning()) {
	    // addons like TA monitor riftTrigger to disable rifts spawning
	    // eventually, we might want to require using the TC Fix API, but for now keep it as is
	    AuraHandler.riftTrigger.put(event.getWorld().provider.getDimension(), pos);
	}
	else if (!ModConfig.CONFIG_MISC.wussMode) {
	    World world = event.getWorld();
	    if (EntityUtils.getEntitiesInRange(world, event.getPosition(), null, EntityFluxRift.class, 32.0).isEmpty()) {
		EntityFluxRift rift = new EntityFluxRift(world);
		rift.setRiftSeed(world.rand.nextInt());
		rift.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
			world.rand.nextFloat() * 360.0f, 0.0f);
		float riftSizeAndFluxCost = MathHelper.sqrt(AuraHandler.getFlux(world, pos) * 3.0f);
		riftSizeAndFluxCost = AuraHandler.drainFlux(world, pos, riftSizeAndFluxCost, false);
		if (riftSizeAndFluxCost > 5.0f && world.spawnEntity(rift)) {
		    rift.setRiftSize((int) riftSizeAndFluxCost);
		    AxisAlignedBB researchRange = new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(32.0, 32.0, 32.0);
		    for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, researchRange)) {
			IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
			if (!knowledge.isResearchKnown("f_toomuchflux")) {
			    ITextComponent fluxMessage = new TextComponentTranslation("tc.fluxevent.3").setStyle(
				    new Style().setColor(TextFormatting.DARK_PURPLE).setItalic(true));
			    player.sendStatusMessage(fluxMessage, true);
			    ThaumcraftApi.internalMethods.completeResearch(player, "f_toomuchflux");
			}
		    }
		}
	    }
	}
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachCapabilitiesWorld(AttachCapabilitiesEvent<World> event) {
	for (ICapabilityProvider provider : event.getCapabilities().values()) {
	    if (provider.hasCapability(CapabilityAuraProcessor.AURA_PROCESSOR, null)) {
		return;
	    }
	}

	event.addCapability(new ResourceLocation(ThaumcraftFixApi.MODID, "aura_processor"),
		new SimpleCapabilityProvider<>(new DefaultAuraProcessor(), CapabilityAuraProcessor.AURA_PROCESSOR));
    }

}
