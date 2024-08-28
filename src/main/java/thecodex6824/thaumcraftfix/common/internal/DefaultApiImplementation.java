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

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge.InternalImplementation;
import thecodex6824.thaumcraftfix.common.ThaumcraftFixConfig;

public class DefaultApiImplementation implements InternalImplementation {

    // not sure if client + server thread are going to be hitting this at the same time
    // or if that will happen after reloadConfig is called at runtime
    private volatile ImmutableSet<ResearchCategory> allowedForTheorycraft;
    private volatile boolean controlAura;
    private volatile boolean controlCrystals;
    private volatile boolean controlTrees;
    private volatile ImmutableSet<Biome> auraBiomes;
    private volatile ImmutableSet<DimensionType> auraDims;
    private volatile ImmutableSet<Biome> crystalBiomes;
    private volatile ImmutableSet<DimensionType> crystalDims;
    private volatile ImmutableSet<Biome> treeBiomes;
    private volatile ImmutableSet<DimensionType> treeDims;

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
	allowedForTheorycraft = ImmutableSet.copyOf(ResearchCategories.researchCategories.values());

	controlAura = ThaumcraftFixConfig.world.aura.controlAura;
	controlCrystals = ThaumcraftFixConfig.world.crystals.controlCrystals;
	controlTrees = ThaumcraftFixConfig.world.vegetation.controlVegetation;

	Set<Biome> allBiomes = ImmutableSet.copyOf(Biome.REGISTRY);
	Set<DimensionType> allDims = ImmutableSet.copyOf(DimensionType.values());
	auraBiomes = makeFilteredBiomeSet(allBiomes, ThaumcraftFixConfig.world.aura.biomeList, ThaumcraftFixConfig.world.aura.biomeAllowList);
	auraDims = makeFilteredDimensionSet(allDims, ThaumcraftFixConfig.world.aura.dimList, ThaumcraftFixConfig.world.aura.dimAllowList);
	crystalBiomes = makeFilteredBiomeSet(allBiomes, ThaumcraftFixConfig.world.crystals.biomeList, ThaumcraftFixConfig.world.crystals.biomeAllowList);
	crystalDims = makeFilteredDimensionSet(allDims, ThaumcraftFixConfig.world.crystals.dimList, ThaumcraftFixConfig.world.crystals.dimAllowList);
	treeBiomes = makeFilteredBiomeSet(allBiomes, ThaumcraftFixConfig.world.vegetation.biomeList, ThaumcraftFixConfig.world.vegetation.biomeAllowList);
	treeDims = makeFilteredDimensionSet(allDims, ThaumcraftFixConfig.world.vegetation.dimList, ThaumcraftFixConfig.world.vegetation.dimAllowList);
    }

    @Override
    public Set<ResearchCategory> getAllowedTheorycraftCategories() {
	return allowedForTheorycraft;
    }

    public void setAllowedTheorycraftCategories(ImmutableSet<ResearchCategory> allowed) {
	allowedForTheorycraft = allowed;
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

}
