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

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.research.theorycraft.ResearchTableData;
import thecodex6824.thaumcraftfix.api.research.ResearchCategoryTheorycraftFilter;

public class TheorycraftTransformersHooks {

    public static boolean isTheorycraftCategoryAllowed(boolean originalDecision, String category,
	    EntityPlayer player, ResearchTableData data) {

	return originalDecision && data.getAvailableCategories(player).contains(category) &&
		ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategories().stream()
		.map(c -> c.key)
		.anyMatch(s -> s.equals(category));
    }

    public static ArrayList<String> filterTheorycraftCategories(ArrayList<String> input) {
	input.retainAll(ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategories().stream()
		.map(c -> c.key)
		.collect(Collectors.toList()));
	return input;
    }

    public static ArrayList<String> filterTheorycraftCategories(ArrayList<String> input,
	    EntityPlayer player, ResearchTableData data) {

	input.retainAll(data.getAvailableCategories(player));
	input.retainAll(ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategories().stream()
		.map(c -> c.key)
		.collect(Collectors.toList()));
	return input;
    }

    public static String[] filterTheorycraftCategoriesArray(String[] input, EntityPlayer player, ResearchTableData data) {
	ArrayList<String> list = Lists.newArrayList(input);
	list = filterTheorycraftCategories(list, player, data);
	return list.toArray(new String[0]);
    }

}
