package thecodex6824.thaumcraftfix.api.research;

import java.util.Set;

import thaumcraft.api.research.ResearchCategory;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;

public class ResearchCategoryTheorycraftFilter {

    public static Set<ResearchCategory> getAllowedTheorycraftCategories() {
	return ThaumcraftFixApiBridge.implementation().getAllowedTheorycraftCategories();
    }

}
