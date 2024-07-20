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

package thecodex6824.thaumcraftfix.api.casting;

import java.util.Collection;

import thaumcraft.api.casters.FocusPackage;

/**
 * Interface for focus nodes that contain other independent collection(s) of focus nodes.
 * These nodes contain their own entire node tree called a {@link thaumcraft.api.casters.FocusPackage FocusPackage}.
 * In Thaumcraft itself, only {@link thaumcraft.api.casters.FocusModSplit FocusModSplit} fits this criteria.
 * In order to have any fixes for the behavior of these kind of nodes apply to nodes from other mods,
 * they must implement this interface to tell TC Fix that it contains <code>FocusPackage</code>s.
 *
 * @see thaumcraft.api.casters.FocusPackage FocusPackage
 * @see thaumcraft.api.casters.FocusModSplit FocusModSplit
 */
public interface IContainsFocusPackageNode {

    /**
     * Returns a collection of {@link thaumcraft.api.casters.FocusPackage FocusPackage}s that are contained
     * in this node.
     * The contained packages may be modified by the caller.
     * @return A collection of the {@link thaumcraft.api.casters.FocusPackage FocusPackage}s
     * that are a part of this focus node
     */
    public Collection<FocusPackage> getEmbeddedPackages();

}
