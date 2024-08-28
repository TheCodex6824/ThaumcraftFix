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

import java.util.Set;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;

public final class FeatureControl {

    private FeatureControl() {}

    /**
     * Returns if aura gen is being controlled by this API.
     * @return If aura gen is being controlled
     */
    public static boolean isControllingAuraGen() {
	return ThaumcraftFixApiBridge.implementation().isControllingAuraGen();
    }

    /**
     * Sets if aura gen should be controlled by this API.
     * @param handle If aura gen should be handled
     */
    public static void setHandleAuraGen(boolean handle) {
	ThaumcraftFixApiBridge.implementation().setControlAuraGen(handle);
    }

    /**
     * Returns the set of all biomes that should have an aura.
     * It will always be the allowed biomes regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate an aura.
     * @return The set of all biomes that should have an aura generated
     *
     * @see #auraAllowedDimensions()
     */
    public static Set<Biome> auraAllowedBiomes() {
	return ThaumcraftFixApiBridge.implementation().auraAllowedBiomes();
    }

    /**
     * Returns the set of all dimensions that should have an aura.
     * It will always be the allowed dimensions regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate an aura.
     * @return The set of all dimensions that should have an aura generated
     *
     * @see #auraAllowedBiomes()
     */
    public static Set<DimensionType> auraAllowedDimensions() {
	return ThaumcraftFixApiBridge.implementation().auraAllowedDimensions();
    }

    /**
     * Returns if crystal gen is being controlled by this API.
     * @return If crystal gen is being controlled
     */
    public static boolean isControllingCrystalGen() {
	return ThaumcraftFixApiBridge.implementation().isControllingCrystalGen();
    }

    /**
     * Sets if crystal gen should be controlled by this API.
     * @param handle If crystal gen should be handled
     */
    public static void setControlCrystalGen(boolean handle) {
	ThaumcraftFixApiBridge.implementation().setControlCrystalGen(handle);
    }

    /**
     * Returns the set of all biomes that should have crystals generated.
     * It will always be the allowed biomes regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate an aura.
     * @return The set of all biomes that should have crystals generated
     *
     * @see #crystalAllowedDimensions()
     */
    public static Set<Biome> crystalAllowedBiomes() {
	return ThaumcraftFixApiBridge.implementation().crystalAllowedBiomes();
    }

    /**
     * Returns the set of all dimensions that should have crystals generated.
     * It will always be the allowed dimensions regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate crystals.
     * @return The set of all dimensions that should have crystals generated
     *
     * @see #crystalAllowedBiomes()
     */
    public static Set<DimensionType> crystalAllowedDimensions() {
	return ThaumcraftFixApiBridge.implementation().crystalAllowedDimensions();
    }

    /**
     * Returns if Thaumcraft vegetation (trees, plants) is being controlled.
     * @return If tree gen is being controlled
     */
    public static boolean isControllingVegetationGen() {
	return ThaumcraftFixApiBridge.implementation().isControllingVegetationGen();
    }

    /**
     * Sets if Thaumcraft vegetation (trees, plants) should be controlled.
     * @param handle If tree gen should be handled
     */
    public static void setControlVegetationGen(boolean handle) {
	ThaumcraftFixApiBridge.implementation().setControlVegetationGen(handle);
    }

    /**
     * Returns the set of all biomes that should have Thaumcraft vegetation (trees, plants) generated.
     * It will always be the allowed biomes regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate vegetation.
     * @return The set of all biomes that should have vegetation generated
     *
     * @see #vegetationAllowedDimensions()
     */
    public static Set<Biome> vegetationAllowedBiomes() {
	return ThaumcraftFixApiBridge.implementation().vegetationAllowedBiomes();
    }

    /**
     * Returns the set of all dimensions that should have Thaumcraft vegetation (trees, plants) generated.
     * It will always be the allowed dimensions regardless of how the user configured
     * the list (i.e. regardless of whether a blocklist or allowlist was used).
     * Both the biome and dimension checks must pass to generate vegetation.
     * @return The set of all dimensions that should have vegetation generated
     *
     * @see #vegetationAllowedBiomes()
     */
    public static Set<DimensionType> vegetationAllowedDimensions() {
	return ThaumcraftFixApiBridge.implementation().vegetationAllowedDimensions();
    }

}
