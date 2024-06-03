package thecodex6824.thaumcraftfix.api.internal;

import java.util.Set;

import thaumcraft.api.research.ResearchCategory;

public class ThaumcraftFixApiBridge {

    public static interface InternalImplementation {
	Set<ResearchCategory> getAllowedTheorycraftCategories();
    }

    private static InternalImplementation impl;

    public static InternalImplementation implementation() {
	return impl;
    }

    public static void setImplementation(InternalImplementation newImpl) {
	impl = newImpl;
    }

}
