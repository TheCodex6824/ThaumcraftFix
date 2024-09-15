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

package thecodex6824.thaumcraftfix.api.research;

import java.util.Set;

import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;

/**
 * Public API for the research category filter in the research table.
 * This filter, by default, will make it impossible for research cards from
 * Thaumcraft and other compliant mods to give knowledge points in research categories.
 */
public class ResearchCategoryTheorycraftFilter {

    /**
     * Returns the {@link java.util.Set Set} of categories that <strong>will</strong> be permitted to appear in
     * theorycrafting cards. The returned <code>Set</code> is not required to be modifiable.
     * @return The <code>Set</code> of allowed research categories
     */
    public static Set<ResearchCategory> getAllowedTheorycraftCategories() {
	return ThaumcraftFixApiBridge.implementation().getAllowedTheorycraftCategories();
    }

    /**
     * Returns the {@link java.util.Set Set} of category keys that <strong>will</strong> be permitted to appear in
     * theorycrafting cards. The returned <code>Set</code> is not required to be modifiable.
     * If you need the {@link thaumcraft.api.research.ResearchCategory ResearchCategory} instances directly,
     * use {@link getAllowedTheorycraftCategories} instead. This method is intended for returning a
     * (potentially cached) set for category key comparison.
     * @return The <code>Set</code> of allowed research categories
     * @see getAllowedTheorycraftCategories
     */
    public static Set<String> getAllowedTheorycraftCategoryKeys() {
	return ThaumcraftFixApiBridge.implementation().getAllowedTheorycraftCategoryKeys();
    }

}
