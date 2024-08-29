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

package thecodex6824.thaumcraftfix.api.aura;

import java.util.Optional;

/**
 * Capability that holds the statistics for an aura chunk,
 * at the time it was generated. Useful for detecting modifications
 * to the aura, or restoring it to its original values. Mods can,
 * and are expected to, modify this at worldgen time to reflect the final values
 * of the generated aura.
 *
 * In the event this mod is installed after worldgen time, aura
 * information will not have been recorded. In this case,
 * the getter methods of this capability should return empty Optional instances.
 * Using Optional here allows callers to differentiate between the lack of aura
 * information and auras that actually have zero values.
 */
public interface IOriginalAuraInfo {

    /**
     * Returns the base aura level, also known as the vis cap.
     * An empty Optional means that this value was never recorded.
     * @return The base aura level
     */
    public Optional<Short> getBase();

    /**
     * Sets the base aura level.
     * @param newBase The new base aura level
     */
    public void setBase(short newBase);

    /**
     * Returns the amount of vis in the aura, which can be
     * different from the vis cap.
     * An empty Optional means that this value was never recorded.
     * @return The vis level
     */
    public Optional<Float> getVis();

    /**
     * Sets the amount of vis in the aura.
     * @param newVis The new vis level
     */
    public void setVis(float newVis);

    /**
     * Returns the amount of flux in the aura. Vanilla Thaumcraft
     * barely adds any flux, if any, at worldgen time, but mods
     * like Thaumic Augmentation may add some as part of their
     * worldgen.
     * An empty Optional means that this value was never recorded.
     * @return The amount of flux in the controlAura
     */
    public Optional<Float> getFlux();

    /**
     * Sets the amount of flux in the aura.
     * @param newFlux The new flux level
     */
    public void setFlux(float newFlux);

}
