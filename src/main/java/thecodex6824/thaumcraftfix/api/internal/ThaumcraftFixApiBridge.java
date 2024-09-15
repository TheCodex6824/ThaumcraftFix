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

package thecodex6824.thaumcraftfix.api.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;

public class ThaumcraftFixApiBridge {

    public static interface InternalImplementation {

	public static final String PATH_RESOURCE_PREFIX = "$filesystem:";

	public void reloadConfig();

	Set<ResearchCategory> getAllowedTheorycraftCategories();
	Set<String> getAllowedTheorycraftCategoryKeys();

	public Set<Biome> auraAllowedBiomes();
	public Set<DimensionType> auraAllowedDimensions();
	public boolean isControllingAuraGen();
	public void setControlAuraGen(boolean handle);

	public Set<Biome> crystalAllowedBiomes();
	public Set<DimensionType> crystalAllowedDimensions();
	public boolean isControllingCrystalGen();
	public void setControlCrystalGen(boolean handle);

	public Set<Biome> vegetationAllowedBiomes();
	public Set<DimensionType> vegetationAllowedDimensions();
	public boolean isControllingVegetationGen();
	public void setControlVegetationGen(boolean handle);

	public void registerScanParser(IScanParser parser, int weight);
	public Collection<IScanThing> parseScans(String key, ResourceLocation type, JsonElement data);
	public void registerResearchEntrySource(ResourceLocation loc);
	public void registerResearchEntrySource(Path path);
	public void registerResearchPatchSource(ResourceLocation loc, int weight);
	public void registerResearchPathSource(Path path, int weight);

	public Collection<ResourceLocation> getFilesystemResearchEntrySources();

	public static interface ResearchPatchSource {
	    public Map<String, ? extends InputStream> open() throws IOException;
	    public String getDescriptor();
	}

	public Collection<ResearchPatchSource> getResearchPatchSources();
    }

    private static InternalImplementation impl;

    public static InternalImplementation implementation() {
	return impl;
    }

    public static void setImplementation(InternalImplementation newImpl) {
	impl = newImpl;
    }

}
