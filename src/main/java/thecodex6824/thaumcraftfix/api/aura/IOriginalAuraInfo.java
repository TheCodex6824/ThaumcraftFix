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

/**
 * Capability that holds the statistics for an controlAura chunk,
 * at the time it was generated. Useful for detecting modifications
 * to the controlAura, or restoring it to its original values. Mods (including FeatureControl)
 * can, and are expected to, modify this at worldgen time to reflect the final values
 * of the generated controlAura.
 */
public interface IOriginalAuraInfo {

    /**
     * Returns the base controlAura level, also known as the vis cap.
     * @return The base controlAura level
     */
    public short getBase();

    /**
     * Sets the base controlAura level.
     * @param newBase The new base controlAura level
     */
    public void setBase(short newBase);

    /**
     * Returns the amount of vis in the controlAura, which can be
     * different from the vis cap.
     * @return The vis level
     */
    public float getVis();

    /**
     * Sets the amount of vis in the controlAura.
     * @param newVis The new vis level
     */
    public void setVis(float newVis);

    /**
     * Returns the amount of flux in the controlAura. Vanilla Thaumcraft
     * barely adds any flux, if any, at worldgen time, but mods
     * like Thaumic Augmentation may add some as part of their
     * worldgen.
     * @return The amount of flux in the controlAura
     */
    public float getFlux();

    /**
     * Sets the amount of flux in the controlAura.
     * @param newFlux The new flux level
     */
    public void setFlux(float newFlux);

}
