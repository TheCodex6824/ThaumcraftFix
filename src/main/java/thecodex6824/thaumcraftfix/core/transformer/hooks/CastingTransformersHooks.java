/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import java.util.Collection;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thecodex6824.thaumcraftfix.api.casting.IContainsFocusPackageNode;

public class CastingTransformersHooks {

    private static Collection<FocusPackage> getEmbeddedPackages(IFocusElement node) {
	Collection<FocusPackage> embeddedPackages = null;
	if (node instanceof IContainsFocusPackageNode) {
	    embeddedPackages = ((IContainsFocusPackageNode) node).getEmbeddedPackages();
	}
	else if (node instanceof FocusModSplit) {
	    embeddedPackages = ((FocusModSplit) node).getSplitPackages();
	}

	return embeddedPackages;
    }

    public static void initializeFocusPackage(FocusPackage focusPackage, EntityLivingBase caster) {
	focusPackage.setCasterUUID(caster.getUniqueID());
	for (IFocusElement node : focusPackage.nodes) {
	    Collection<FocusPackage> embeddedPackages = getEmbeddedPackages(node);
	    if (embeddedPackages != null) {
		embeddedPackages.forEach(p -> p.initialize(caster));
	    }
	}
    }

    public static void setFocusPackageCasterUUID(FocusPackage focusPackage, UUID caster) {
	for (IFocusElement node : focusPackage.nodes) {
	    Collection<FocusPackage> embeddedPackages = getEmbeddedPackages(node);
	    if (embeddedPackages != null) {
		embeddedPackages.forEach(p -> p.setCasterUUID(caster));
	    }
	}
    }

}
