package pack;

import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class TryCatchBlockNodeStruct {
    public TryCatchBlockNode tcb; 
    public Inst index; 
    public Inst value; 
    public Inst ref; 
    public Inst oldref; 

    public TryCatchBlockNodeStruct(TryCatchBlockNode tcb, Inst ref, Inst oldref,  Inst index, Inst value){
        this.tcb = tcb; 
        this.ref = ref;
        this.oldref = oldref;
        this.index = index;
        this.value = value;  
    }
}
