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

public class MatchResult implements MatchDetails {

    private final AbstractInsnNode matchStart;
    private final AbstractInsnNode matchEnd;

    private MatchResult(AbstractInsnNode matchStart, AbstractInsnNode matchEnd) {
	this.matchStart = matchStart;
	this.matchEnd = matchEnd;
    }

    @Override
    public boolean matched() {
	return matchStart != null && matchEnd != null;
    }

    @Override
    public AbstractInsnNode matchStart() {
	return matchStart;
    }

    @Override
    public AbstractInsnNode matchEnd() {
	return matchEnd;
    }

    public MatchSnapshot makeSnapshot() {
	if (!matched()) {
	    throw new IllegalStateException("Cannot make MatchSnapshot from result without a match");
	}

	return new MatchSnapshot(matchStart, matchEnd);
    }

    public static MatchResult noMatch() {
	return new MatchResult(null, null);
    }

    public static MatchResult matchSingleNode(AbstractInsnNode node) {
	return new MatchResult(node, node);
    }

    public static MatchResult matchNodeRange(AbstractInsnNode start, AbstractInsnNode end) {
	return new MatchResult(start, end);
    }

}
