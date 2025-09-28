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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.event.research.ResearchEntryLoadEvent;
import thecodex6824.thaumcraftfix.api.event.research.ResearchLoadEvent;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.common.network.PacketGainKnowledge;
import thecodex6824.thaumcraftfix.common.network.PacketGainResearch;

public class ResearchTransformersHooks {

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

    public static String[] fixupFirstPassSplit(String rawText, String separator, String[] split) {
	if (rawText.endsWith(separator)) {
	    split = Arrays.copyOf(split, split.length + 1);
	    split[split.length - 1] = "";
	}

	return split;
    }

    public static void researchLoadStart() {
	MinecraftForge.EVENT_BUS.post(new ResearchLoadEvent.Pre());
    }

    public static void researchLoadEnd() {
	MinecraftForge.EVENT_BUS.post(new ResearchLoadEvent.Post());
    }

    public static boolean entryLoadStart(JsonObject json) {
	ResearchEntryLoadEvent.Pre event = new ResearchEntryLoadEvent.Pre(json);
	MinecraftForge.EVENT_BUS.post(event);
	return !event.isCanceled();
    }

    public static void entryLoadEnd(JsonObject json, ResearchEntry entry) {
	MinecraftForge.EVENT_BUS.post(new ResearchEntryLoadEvent.Post(json, entry));
    }

    public static InputStream getFilesystemStream(InputStream stream, ResourceLocation loc) {
	String prefix = ThaumcraftFixApiBridge.InternalImplementation.PATH_RESOURCE_PREFIX;
	if (stream == null && loc.getNamespace().equals(ThaumcraftFixApi.MODID) && loc.getPath().startsWith(prefix)) {
	    Path gameDir = ThaumcraftFix.proxy.getGameDirectory().toPath().normalize();
	    Path requested = Paths.get(loc.getPath().substring(prefix.length()));
	    if (!requested.isAbsolute() && requested.getRoot() == null) {
		try {
		    stream = new FileInputStream(gameDir.resolve(requested.normalize()).toFile());
		}
		catch (IOException ex) {
		    ThaumcraftFix.instance.getLogger().error("Failed opening research file: ", ex);
		}
	    }
	    else {
		ThaumcraftFix.instance.getLogger().error("Illegal research entry path, paths must be relative to the game directory: " + requested);
	    }
	}

	return stream;
    }

    public static boolean isPlayerCarryingScribingTools(ItemStack isCarrying, boolean ore, boolean original, EntityPlayer player) {
	if (original || isCarrying.getItem() != ItemsTC.scribingTools) {
	    return original;
	}

	for (ItemStack stack : Iterables.concat(player.inventory.offHandInventory, player.inventory.mainInventory)) {
	    if (!stack.isEmpty() && stack.getItem() instanceof IScribeTools) {
		return true;
	    }
	}

	return false;
    }

}
