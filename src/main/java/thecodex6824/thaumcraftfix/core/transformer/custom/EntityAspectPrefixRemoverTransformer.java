package thecodex6824.thaumcraftfix.core.transformer.custom;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;

public class EntityAspectPrefixRemoverTransformer implements ITransformer {

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return "thaumcraft.common.config.ConfigAspects".equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	MethodNode register = TransformUtil.findMethod(classNode, new MethodDefinition(
		"thaumcraft/common/config/ConfigAspects",
		false,
		"registerEntityAspects",
		Type.VOID_TYPE
		));
	boolean didSomething = false;
	for (int i = 0; i < register.instructions.size(); ++i) {
	    AbstractInsnNode node = register.instructions.get(i);
	    if (node instanceof LdcInsnNode) {
		LdcInsnNode ldc = (LdcInsnNode) node;
		// the entity name registrations do not have a "Thaumcraft." prefix, so remove it to match
		if (ldc.cst instanceof String && ((String) ldc.cst).startsWith("Thaumcraft.")) {
		    ldc.cst = ((String) ldc.cst).replaceFirst("Thaumcraft.", "");
		    didSomething = true;
		}
	    }
	}

	return didSomething;
    }

}
