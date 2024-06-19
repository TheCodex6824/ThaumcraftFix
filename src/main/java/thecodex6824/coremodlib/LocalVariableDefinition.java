package thecodex6824.coremodlib;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LocalVariableDefinition {

    private final String varName;
    private final Type type;
    private final String desc;

    public LocalVariableDefinition(String varName, Type type) {
	this.varName = varName;
	this.type = type;
	desc = type.getDescriptor();
    }

    public String name() {
	return varName;
    }

    public String desc() {
	return desc;
    }

    public Type type() {
	return type;
    }

    public VarInsnNode asVarInsnNode(int opcode, MethodNode method) {
	return new VarInsnNode(opcode, method.localVariables.stream()
		.filter(v -> v.name.equals(varName) && v.desc.equals(desc))
		.findAny()
		.get()
		.index
		);
    }

    @Override
    public String toString() {
	return String.format("%s %s", type.getClassName(), varName);
    }

}
