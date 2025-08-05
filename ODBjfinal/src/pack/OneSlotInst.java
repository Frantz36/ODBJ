package pack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class OneSlotInst extends Inst {
    public OneSlotInst(AbstractInsnNode inst) {
        this.inst = inst;
    }
}
