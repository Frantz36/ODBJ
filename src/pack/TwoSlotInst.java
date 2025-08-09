package pack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class TwoSlotInst extends Inst {
    public TwoSlotInst(AbstractInsnNode inst) {
        this.inst = inst;
    }
}
