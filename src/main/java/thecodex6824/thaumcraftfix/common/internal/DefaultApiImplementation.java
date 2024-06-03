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
