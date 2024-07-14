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

package thecodex6824.coremodlib;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class MatchSnapshot implements MatchDetails {

    private final InsnList matchList;
    private final InsnList originalList;
    private final AbstractInsnNode matchEnd;
    private final AbstractInsnNode originalEnd;

    private Pair<InsnList, Integer> setupMatchList(AbstractInsnNode start, AbstractInsnNode end) {
	if (start == null || end == null) {
	    return null;
	}

	int distance = 0;
	AbstractInsnNode cursor = start;
	while (cursor != end) {
	    cursor = cursor.getNext();
	    ++distance;
	}

	return Pair.of(ASMUtil.cloneNodeRangeAndDependencies(start, end), distance);
    }

    protected MatchSnapshot(AbstractInsnNode originalStart, AbstractInsnNode matchStart,
	    AbstractInsnNode originalEnd, AbstractInsnNode matchEnd) {

	Pair<InsnList, Integer> match = setupMatchList(originalStart, originalEnd);
	originalList = match != null ? match.getLeft() : null;
	this.originalEnd = originalList != null ? originalList.get(match.getRight()) : null;

	match = setupMatchList(matchStart, matchEnd);
	matchList = match != null ? match.getLeft() : null;
	this.matchEnd = matchList != null ? matchList.get(match.getRight()) : null;
    }

    @Override
    public boolean matched() {
	return matchList != null && matchEnd != null;
    }

    @Override
    public AbstractInsnNode matchStart() {
	return matchList != null ? matchList.getFirst() : null;
    }

    public AbstractInsnNode originalStart() {
	return originalList != null ? originalList.getFirst() : null;
    }

    @Override
    public AbstractInsnNode matchEnd() {
	return matchEnd;
    }

    public AbstractInsnNode originalEnd() {
	return originalEnd;
    }

}
