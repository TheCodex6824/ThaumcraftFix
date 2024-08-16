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

package thecodex6824.thaumcraftfix.core.transformer.custom;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.google.common.collect.ImmutableSet;

import thecodex6824.thaumcraftfix.core.transformer.ITransformer;

public class AuraChunkThreadSafetyTransformer implements ITransformer {

    private static final ImmutableSet<String> FIELDS_NEEDING_VOLATILE = ImmutableSet.of(
	    "loc",
	    "base",
	    "vis",
	    "flux"
	    );

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.world.aura.AuraChunk".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	for (FieldNode field : classNode.fields) {
	    if (FIELDS_NEEDING_VOLATILE.contains(field.name)) {
		/*
		 * Applying volatile to these fields will make sure reads/writes are atomic and changes are visible
		 * to other threads. Volatile is enough since every operation on these variables we care about
		 * is a single action (load/store). Users of AuraChunk might use increments or other operations
		 * that are not safe with volatile, but we can't do anything about that without modifying a
		 * lot more code. Even if we locked the entire AuraChunk those accesses would still have data
		 * races since the lock would be released during the operation, instead of being held for
		 * the entire operation.
		 *
		 * The AuraChunk instances are stored in a ConcurrentHashMap, which is good, but that only
		 * protects access to the map itself, and not the individual AuraChunk instances. Since
		 * ConcurrentHashMap doesn't lock, there is nothing ensuring the changes are visible to other
		 * threads or some thread isn't trying to modify something at the same time as another.
		 */
		field.access |= Opcodes.ACC_VOLATILE;
	    }
	}

	return true;
    }

}
