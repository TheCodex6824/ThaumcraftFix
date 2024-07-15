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

import java.util.List;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

class PrefabMatchTransformers {

    public static class LastNodeOnly implements MatchTransformer {

	@Override
	public void transformMatch(MethodNode method, MutableMatchDetails match,
		List<? extends MutableMatchDetails> previousMatches) {
	    match.setMatchStart(match.matchEnd());
	}

	@Override
	public String toString() {
	    return "Trims match to only contain the last node";
	}

    }

    public static class MakeMatchRange implements MatchTransformer {

	@Override
	public void transformMatch(MethodNode method, MutableMatchDetails match,
		List<? extends MutableMatchDetails> previousMatches) {
	    if (previousMatches.isEmpty()) {
		throw new IllegalStateException("Cannot make a match range out of a single match");
	    }

	    MutableMatchDetails otherMatch = previousMatches.remove(previousMatches.size() - 1);
	    InsnList insns = method.instructions;
	    if (insns.indexOf(otherMatch.matchStart()) < insns.indexOf(match.matchStart())) {
		match.setMatchStart(otherMatch.matchStart());
	    }
	    if (insns.indexOf(otherMatch.matchEnd()) > insns.indexOf(match.matchEnd())) {
		match.setMatchEnd(otherMatch.matchEnd());
	    }
	}

	@Override
	public String toString() {
	    return "Combines the last 2 matches into a single match range";
	}

    }

}
