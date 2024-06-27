package thecodex6824.thaumcraftfix.core.transformer.custom;

import java.util.ArrayList;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableMap;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.TransformUtil;
import thecodex6824.thaumcraftfix.core.transformer.Types;

public class ChangeEventPriorityTransformer implements ITransformer {

    private String internalNameWithDots;
    private Map<MethodDefinition, String> priorities;

    public ChangeEventPriorityTransformer(Type owner, Map<MethodDefinition, String> newPriorities) {
	internalNameWithDots = owner.getInternalName().replace('/', '.');
	priorities = ImmutableMap.copyOf(newPriorities);
    }

    @Override
    public boolean isTransformationNeeded(String transformedName) {
	return internalNameWithDots.equals(transformedName);
    }

    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
	boolean allFound = true;
	for (Map.Entry<MethodDefinition, String> entry : priorities.entrySet()) {
	    MethodNode method = TransformUtil.findMethod(classNode, entry.getKey());
	    if (method != null) {
		boolean found = false;
		for (AnnotationNode annotation : method.visibleAnnotations) {
		    if (annotation.desc.equals(Types.SUBSCRIBE_EVENT.getDescriptor())) {
			int priorityIndex = -1;
			if (annotation.values == null) {
			    annotation.values = new ArrayList<Object>();
			}
			else {
			    priorityIndex = annotation.values.indexOf("priority");
			}

			String[] priorityValue = new String[] {
				Types.EVENT_PRIORITY.getDescriptor(),
				entry.getValue()
			};
			if (priorityIndex != -1) {
			    annotation.values.set(priorityIndex + 1, priorityValue);
			}
			else {
			    annotation.values.add("priority");
			    annotation.values.add(priorityValue);
			}
			found = true;
			break;
		    }
		}

		allFound &= found;
	    }
	    else {
		allFound = false;
	    }
	}

	return allFound;
    }

}
