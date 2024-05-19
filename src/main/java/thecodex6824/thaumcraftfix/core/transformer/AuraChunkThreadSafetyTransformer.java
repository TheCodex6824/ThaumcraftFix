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

package thecodex6824.thaumcraftfix.core.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableSet;

public class AuraChunkThreadSafetyTransformer implements ITransformer {

    private static final ImmutableSet<String> METHODS_NEEDING_SYNC = ImmutableSet.of(
	    "getBase",
	    "setBase",
	    "getVis",
	    "setVis",
	    "getFlux",
	    "setFlux",
	    "getLoc",
	    "setLoc"
	    );

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.world.aura.AuraChunk".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	for (MethodNode method : classNode.methods) {
	    if (METHODS_NEEDING_SYNC.contains(method.name)) {
		/*
		 * Applying synchronized to these methods can be thought of as adding a mutex to the class,
		 * that is acquired when any of these methods is entered and released when returning.
		 * The AuraChunk instances are stored in a ConcurrentHashMap, which is good, but that only
		 * protects access to the map itself, and not the individual AuraChunk instances. Since
		 * ConcurrentHashMap doesn't lock, there is nothing stopping the aura thread and some other thread
		 * (probably the server thread) from trying to modify something at the same time.
		 *
		 * We don't technically need to lock the entire object when changing just one field, and having one
		 * lock each for vis, flux, etc would be better, but that would require more drastic changes that I
		 * don't think are worth it right now.
		 */
		method.access |= Opcodes.ACC_SYNCHRONIZED;
	    }
	}

	return true;
    }

}
