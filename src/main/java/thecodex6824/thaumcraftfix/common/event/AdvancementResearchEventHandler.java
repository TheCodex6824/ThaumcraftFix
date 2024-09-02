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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.research.ResearchManager;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.common.research.AdvancementResearchInfo;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID)
public class AdvancementResearchEventHandler {

    protected static Map<ResourceLocation, AdvancementResearchInfo> advancements = new HashMap<>();

    public static void addAdvancementInfo(ResourceLocation key, AdvancementResearchInfo info) {
	advancements.put(key, info);
    }

    public static void clearAdvancementInfo() {
	advancements.clear();
    }

    protected static void processResearchDeep(ResearchEntry initial, Predicate<ResearchEntry> accept, Function<ResearchEntry, String[]> nextLevel, Consumer<String> func) {
	ArrayDeque<String> researchStack = new ArrayDeque<>();
	ArrayDeque<String> toVisit = new ArrayDeque<>();
	HashSet<String> visited = new HashSet<>();
	for (String s : nextLevel.apply(initial)) {
	    toVisit.add(s);
	    researchStack.push(s);
	}

	while (!toVisit.isEmpty()) {
	    String s = toVisit.poll();
	    if (visited.add(s)) {
		ResearchEntry entry = ResearchCategories.getResearch(s);
		if (entry != null && accept.test(entry)) {
		    for (String s2 : nextLevel.apply(entry)) {
			toVisit.add(s2);
			researchStack.push(s2);
		    }
		}
	    }
	}

	researchStack.forEach(func);
    }

    protected static void giveResearchFully(EntityPlayer player, IPlayerKnowledge knowledge, String key) {
	knowledge.addResearch(key);
	ResearchEntry entry = ResearchCategories.getResearch(key);
	if (entry != null) {
	    if (entry.getStages() != null) {
		for (ResearchStage stage : entry.getStages()) {
		    if (stage.getResearch() != null) {
			for (String s : stage.getResearch()) {
			    ResearchManager.completeResearch(player, s, true);
			}
		    }
		}
	    }

	    ResearchManager.completeResearch(player, key, true);
	    // show research complete toast
	    knowledge.setResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
	    // this flag puts the green plus on the research
	    knowledge.setResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.PAGE);
	}
    }

    protected static boolean giveResearch(EntityPlayer player, IPlayerKnowledge knowledge, String key) {
	String researchWithoutStage = key;
	int stage = -1;
	if (researchWithoutStage.contains("@")) {
	    String[] components = key.split("@", 2);
	    researchWithoutStage = components[0];
	    if (components.length == 2)
		stage = MathHelper.getInt(components[1], 0);
	}

	// unfortunately, complete does not seem to imply known
	if (!knowledge.isResearchKnown(researchWithoutStage) || !knowledge.isResearchComplete(researchWithoutStage)) {
	    ResearchEntry entry = ResearchCategories.getResearch(researchWithoutStage);
	    // if intermediate stage, just give the stage and not the full research with siblings
	    if (stage >= 1 && (entry == null || stage < entry.getStages().length)) {
		if (!knowledge.isResearchKnown(researchWithoutStage) || stage > knowledge.getResearchStage(researchWithoutStage)) {
		    if (entry != null && entry.getParents() != null && !knowledge.isResearchKnown(researchWithoutStage))
			processResearchDeep(entry, e -> !knowledge.isResearchKnown(e.getKey()), e -> e.getParentsStripped(), s -> giveResearchFully(player, knowledge, s));

		    knowledge.addResearch(researchWithoutStage);
		    knowledge.setResearchStage(researchWithoutStage, stage);
		    return true;
		}
	    }
	    else {
		if (entry != null && entry.getParents() != null && !knowledge.isResearchComplete(researchWithoutStage))
		    processResearchDeep(entry, e -> !knowledge.isResearchKnown(e.getKey()), e -> e.getParentsStripped(), s -> giveResearchFully(player, knowledge, s));

		giveResearchFully(player, knowledge, researchWithoutStage);
		if (entry != null && entry.getSiblings() != null)
		    processResearchDeep(entry, e -> !knowledge.isResearchKnown(e.getKey()), e -> e.getSiblings(), s -> giveResearchFully(player, knowledge, s));

		return true;
	    }
	}

	return false;
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
	EntityPlayer player = event.getEntityPlayer();
	IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
	if (knowledge != null) {
	    AdvancementResearchInfo info = advancements.get(event.getAdvancement().getId());
	    if (info != null) {
		if (!info.getResearchKeys().isEmpty()) {
		    boolean didSomething = false;
		    for (String s : info.getResearchKeys())
			didSomething |= giveResearch(player, knowledge, s);

		    if (didSomething && player instanceof EntityPlayerMP)
			knowledge.sync((EntityPlayerMP) player);
		}

		if (info.getResearchMessage().isPresent()) {
		    player.sendStatusMessage(new TextComponentTranslation(
			    info.getResearchMessage().get()).setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)), true);
		}
	    }
	}
    }

}
