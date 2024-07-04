package thecodex6824.coremodlib;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface MutableMatchDetails extends MatchDetails {

    public void setMatchStart(AbstractInsnNode newStart);

    public void setMatchEnd(AbstractInsnNode newEnd);

}
