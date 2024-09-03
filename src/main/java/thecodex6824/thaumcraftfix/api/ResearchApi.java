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

package thecodex6824.thaumcraftfix.api;

import java.nio.file.Path;
import java.util.Collection;

import com.google.gson.JsonElement;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.research.IScanThing;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;

public class ResearchApi {

    public static void registerScanParser(IScanParser parser, int weight) {
	ThaumcraftFixApiBridge.implementation().registerScanParser(parser, weight);
    }

    public static Collection<IScanThing> parseScans(String key, ResourceLocation type, JsonElement data) {
	return ThaumcraftFixApiBridge.implementation().parseScans(key, type, data);
    }

    public static void registerResearchEntrySource(ResourceLocation loc) {
	ThaumcraftFixApiBridge.implementation().registerResearchEntrySource(loc);
    }

    public static void registerResearchEntrySource(Path path) {
	ThaumcraftFixApiBridge.implementation().registerResearchEntrySource(path);
    }

    public static void registerResearchPatchSource(ResourceLocation loc) {
	ThaumcraftFixApiBridge.implementation().registerResearchPatchSource(loc);
    }

    public static void registerResearchPatchSource(Path path) {
	ThaumcraftFixApiBridge.implementation().registerResearchPathSource(path);
    }

}
