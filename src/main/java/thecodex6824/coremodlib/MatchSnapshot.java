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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class MatchSnapshot implements MatchDetails {

    private final InsnList matchList;
    private final AbstractInsnNode matchEnd;

    protected MatchSnapshot(AbstractInsnNode matchStart, AbstractInsnNode matchEnd) {
	if (matchStart != null && matchEnd != null) {
	    int distance = 0;
	    AbstractInsnNode cursor = matchStart;
	    while (cursor != matchEnd) {
		cursor = cursor.getNext();
		++distance;
	    }

	    matchList = ASMUtil.cloneNodeRangeAndDependencies(matchStart, matchEnd);
	    this.matchEnd = matchList.get(distance);
	}
	else {
	    matchList = null;
	    this.matchEnd = null;
	}
    }

    @Override
    public boolean matched() {
	return matchList != null && matchEnd != null;
    }

    @Override
    public AbstractInsnNode matchStart() {
	return matchList != null ? matchList.getFirst() : null;
    }

    @Override
    public AbstractInsnNode matchEnd() {
	return matchEnd;
    }

}
