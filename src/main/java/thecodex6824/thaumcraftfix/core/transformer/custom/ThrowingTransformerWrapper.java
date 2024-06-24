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
