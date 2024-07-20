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

import org.objectweb.asm.tree.ClassNode;

import thecodex6824.coremodlib.ASMUtil;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;

public class ThrowingTransformerWrapper implements ITransformer {

    private ITransformer wrapped;

    public ThrowingTransformerWrapper(ITransformer toWrap) {
	wrapped = toWrap;
    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return wrapped.isTransformationNeeded(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	String origClassDump = ASMUtil.dumpClass(classNode);
	Throwable toRethrow = null;
	try {
	    if (!wrapped.transform(classNode, name, transformedName)) {
		toRethrow = new RuntimeException("Patch returned false");
	    }
	}
	catch (Throwable anything) {
	    toRethrow = anything;
	}

	if (toRethrow != null) {
	    ThaumcraftFixCore.getLogger().error("Class dump before changes:");
	    ThaumcraftFixCore.getLogger().error(origClassDump);
	    ThaumcraftFixCore.getLogger().error("Class dump after changes:");
	    ThaumcraftFixCore.getLogger().error(ASMUtil.dumpClass(classNode));
	    throw new RuntimeException(toRethrow);
	}

	return true;
    }

}
