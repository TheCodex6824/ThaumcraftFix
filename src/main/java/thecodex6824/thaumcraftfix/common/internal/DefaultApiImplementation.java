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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonElement;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge.InternalImplementation;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;
import thecodex6824.thaumcraftfix.common.config.ThaumcraftFixConfig;

public class DefaultApiImplementation implements InternalImplementation {

    private ImmutableSet<ResearchCategory> allowedForTheorycraft;
    private ImmutableSet<String> allowedForTheorycraftKeys;
    private boolean controlAura;
    private boolean controlCrystals;
    private boolean controlTrees;
    private ImmutableSet<Biome> auraBiomes;
    private ImmutableSet<DimensionType> auraDims;
    private ImmutableSet<Biome> crystalBiomes;
    private ImmutableSet<DimensionType> crystalDims;
    private ImmutableSet<Biome> treeBiomes;
    private ImmutableSet<DimensionType> treeDims;
    private Multimap<Integer, IScanParser> scanParsers;
    private HashSet<Path> entrySources;
    private Multimap<Integer, ResearchPatchSource> patchSources;

    public DefaultApiImplementation() {
	allowedForTheorycraft = ImmutableSet.of();
	allowedForTheorycraftKeys = ImmutableSet.of();
	controlAura = false;
	controlCrystals = false;
	controlTrees = false;
	auraBiomes = ImmutableSet.of();
	auraDims = ImmutableSet.of();
	crystalBiomes = ImmutableSet.of();
	crystalDims = ImmutableSet.of();
	treeBiomes = ImmutableSet.of();
	treeDims = ImmutableSet.of();
	scanParsers = MultimapBuilder.treeKeys().hashSetValues().build();
	entrySources = new HashSet<>();
	patchSources = MultimapBuilder.treeKeys().hashSetValues().build();
    }

    private ImmutableSet<Biome> makeFilteredBiomeSet(Set<Biome> allBiomes, String[] filter, boolean allow) {
	ArrayList<Pattern> filterPatterns = new ArrayList<>();
	for (String p : filter) {
	    try {
		filterPatterns.add(Pattern.compile(p));
	    }
	    catch (PatternSyntaxException ex) {
		ThaumcraftFix.instance.getLogger().error("A biome filter regex in the config is invalid: " + p, ex);
	    }
	}
	return ImmutableSet.copyOf(allow ?
		allBiomes.stream().filter(b -> filterPatterns.stream().anyMatch(p -> p.matcher(b.getRegistryName().toString()).matches())).iterator() :
		    allBiomes.stream().filter(b -> !filterPatterns.stream().anyMatch(p -> p.matcher(b.getRegistryName().toString()).matches())).iterator());
    }

    private ImmutableSet<DimensionType> makeFilteredDimensionSet(Set<DimensionType> allDims, String[] filter, boolean allow) {
	ArrayList<Pattern> filterPatterns = new ArrayList<>();
	for (String p : filter) {
	    try {
		filterPatterns.add(Pattern.compile(p));
	    }
	    catch (PatternSyntaxException ex) {
		ThaumcraftFix.instance.getLogger().error("A biome filter regex in the config is invalid: " + p, ex);
	    }
	}
	return ImmutableSet.copyOf(allow ?
		allDims.stream().filter(d -> filterPatterns.stream().anyMatch(p -> p.matcher(d.getName()).matches())).iterator() :
		    allDims.stream().filter(d -> !filterPatterns.stream().anyMatch(p -> p.matcher(d.getName()).matches())).iterator());
    }

    @Override
    public void reloadConfig() {
	ThaumcraftFixConfig config = ThaumcraftFix.instance.getConfig();
	controlAura = config.world.aura.controlAura.value();
	controlCrystals = config.world.crystals.controlCrystals.value();
	controlTrees = config.world.vegetation.controlVegetation.value();

	Set<Biome> allBiomes = ImmutableSet.copyOf(Biome.REGISTRY.iterator());
	Set<DimensionType> allDims = ImmutableSet.copyOf(DimensionType.values());
	auraBiomes = makeFilteredBiomeSet(allBiomes, config.world.aura.biomeList.value(), config.world.aura.biomeAllowList.value());
	auraDims = makeFilteredDimensionSet(allDims, config.world.aura.dimList.value(), config.world.aura.dimAllowList.value());
	crystalBiomes = makeFilteredBiomeSet(allBiomes, config.world.crystals.biomeList.value(), config.world.crystals.biomeAllowList.value());
	crystalDims = makeFilteredDimensionSet(allDims, config.world.crystals.dimList.value(), config.world.crystals.dimAllowList.value());
	treeBiomes = makeFilteredBiomeSet(allBiomes, config.world.vegetation.biomeList.value(), config.world.vegetation.biomeAllowList.value());
	treeDims = makeFilteredDimensionSet(allDims, config.world.vegetation.dimList.value(), config.world.vegetation.dimAllowList.value());
    }

    @Override
    public Set<ResearchCategory> getAllowedTheorycraftCategories() {
	return allowedForTheorycraft;
    }

    @Override
    public Set<String> getAllowedTheorycraftCategoryKeys() {
	return allowedForTheorycraftKeys;
    }

    public void setAllowedTheorycraftCategories(ImmutableSet<ResearchCategory> allowed) {
	allowedForTheorycraft = allowed;
	allowedForTheorycraftKeys = allowedForTheorycraft.stream().map(c -> c.key).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public boolean isControllingAuraGen() {
	return controlAura;
    }

    @Override
    public void setControlAuraGen(boolean handle) {
	controlAura = handle;
    }

    @Override
    public Set<Biome> auraAllowedBiomes() {
	return auraBiomes;
    }

    @Override
    public Set<DimensionType> auraAllowedDimensions() {
	return auraDims;
    }

    @Override
    public boolean isControllingCrystalGen() {
	return controlCrystals;
    }

    @Override
    public void setControlCrystalGen(boolean handle) {
	controlCrystals = handle;
    }

    @Override
    public Set<Biome> crystalAllowedBiomes() {
	return crystalBiomes;
    }

    @Override
    public Set<DimensionType> crystalAllowedDimensions() {
	return crystalDims;
    }

    @Override
    public boolean isControllingVegetationGen() {
	return controlTrees;
    }

    @Override
    public void setControlVegetationGen(boolean handle) {
	controlTrees = handle;
    }

    @Override
    public Set<Biome> vegetationAllowedBiomes() {
	return treeBiomes;
    }

    @Override
    public Set<DimensionType> vegetationAllowedDimensions() {
	return treeDims;
    }

    @Override
    public void registerScanParser(IScanParser parser, int weight) {
	scanParsers.put(weight, parser);
    }

    @Override
    public Collection<IScanThing> parseScans(String key, ResourceLocation type, JsonElement data) {
	RuntimeException throwLater = new RuntimeException("No parsers were able to load scan of type " + type);
	for (IScanParser parser : scanParsers.values()) {
	    if (parser.matches(type)) {
		try {
		    return parser.parseScan(key, type, data);
		}
		catch (Exception ex) {
		    throwLater.addSuppressed(ex);
		}
	    }
	}

	throw throwLater;
    }

    @Override
    public void registerResearchEntrySource(Path path) {
	if (path.isAbsolute() || path.getRoot() != null) {
	    throw new IllegalArgumentException("Filesystem research entry sources must be relative to the game directory");
	}
	entrySources.add(ThaumcraftFix.proxy.getGameDirectory().toPath().resolve(path.normalize()));
    }

    @Override
    public void registerResearchEntrySource(ResourceLocation loc) {
	ThaumcraftApi.registerResearchLocation(loc);
    }

    private ResourceLocation createFilesystemLocation(String path) {
	return new ResourceLocation(ThaumcraftFixApi.MODID,
		InternalImplementation.PATH_RESOURCE_PREFIX + path);
    }

    @Override
    public Collection<ResourceLocation> getFilesystemResearchEntrySources() {
	ArrayList<ResourceLocation> locs = new ArrayList<>();
	for (Path p : entrySources) {
	    File file = p.toFile();
	    if (file.exists()) {
		if (file.isFile()) {
		    locs.add(createFilesystemLocation(p.toString()));
		}
		else {
		    for (File f : file.listFiles(f -> f.isFile() && f.getName().endsWith(".json"))) {
			locs.add(createFilesystemLocation(f.getPath()));
		    }
		}
	    }
	}

	return locs;
    }

    private static class ResourceLocationSource implements ResearchPatchSource {
	private final ResourceLocation loc;

	public ResourceLocationSource(ResourceLocation location) {
	    loc = location;
	}

	@Override
	public String getDescriptor() {
	    return loc.toString();
	}

	@Override
	public Map<String, ? extends InputStream> open() throws IOException {
	    return ImmutableMap.of(loc.toString(), ThaumcraftFix.proxy.resolveResource(loc));
	}

	@Override
	public boolean equals(Object obj) {
	    return getClass() == obj.getClass() && loc.equals(((ResourceLocationSource) obj).loc);
	}

	@Override
	public int hashCode() {
	    return loc.hashCode();
	}
    }

    private static class FilesystemSource implements ResearchPatchSource {
	private final Path path;

	public FilesystemSource(Path p) {
	    path = p;
	}

	@Override
	public String getDescriptor() {
	    return path.toString();
	}

	@Override
	public Map<String, ? extends InputStream> open() throws IOException {
	    File file = path.toFile();
	    if (file.exists()) {
		if (file.isFile()) {
		    return ImmutableMap.of(path.getFileName().toString(), new FileInputStream(file));
		}
		else {
		    ImmutableMap.Builder<String, FileInputStream> builder = ImmutableMap.builder();
		    for (File f : file.listFiles(f -> f.isFile() && f.getName().endsWith(".json"))) {
			builder.put(f.getName(), new FileInputStream(f));
		    }

		    return builder.build();
		}
	    }

	    return ImmutableMap.of();
	}

	@Override
	public boolean equals(Object obj) {
	    return getClass() == obj.getClass() && path.equals(((FilesystemSource) obj).path);
	}

	@Override
	public int hashCode() {
	    return path.hashCode();
	}
    }

    @Override
    public void registerResearchPatchSource(ResourceLocation loc, int weight) {
	patchSources.put(weight, new ResourceLocationSource(loc));
    }

    @Override
    public void registerResearchPathSource(Path path, int weight) {
	patchSources.put(weight, new FilesystemSource(path));
    }

    @Override
    public Collection<ResearchPatchSource> getResearchPatchSources() {
	return patchSources.values();
    }

}
