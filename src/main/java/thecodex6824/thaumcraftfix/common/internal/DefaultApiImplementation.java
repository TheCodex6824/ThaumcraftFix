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

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge.InternalImplementation;

public class DefaultApiImplementation implements InternalImplementation {

    private ImmutableSet<ResearchCategory> allowedForTheorycraft;

    public DefaultApiImplementation() {
	allowedForTheorycraft = ImmutableSet.copyOf(ResearchCategories.researchCategories.values());
    }

    @Override
    public Set<ResearchCategory> getAllowedTheorycraftCategories() {
	return allowedForTheorycraft;
    }

    public void setAllowedTheorycraftCategories(ImmutableSet<ResearchCategory> allowed) {
	allowedForTheorycraft = allowed;
    }

}
