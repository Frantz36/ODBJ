package pack;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

public class InstructionStack extends Stack<Inst> {

    static final Map<Integer, String> opcodeMap = new HashMap<>();

    static {
        // Populate the map with common opcodes from the Opcodes class
        opcodeMap.put(Opcodes.IINC, "IINC");
        opcodeMap.put(Opcodes.ISTORE, "ISTORE");
        opcodeMap.put(Opcodes.BIPUSH, "BIPUSH");
        opcodeMap.put(Opcodes.ILOAD, "ILOAD");
        opcodeMap.put(Opcodes.ALOAD, "ALOAD");
        opcodeMap.put(Opcodes.I2B, "I2B");
        opcodeMap.put(Opcodes.ASTORE, "ASTORE");
        opcodeMap.put(Opcodes.BALOAD, "BALOAD");
        opcodeMap.put(Opcodes.BASTORE, "BASTORE");
        opcodeMap.put(Opcodes.CALOAD, "CALOAD");
        opcodeMap.put(Opcodes.CASTORE, "CASTORE");
        opcodeMap.put(Opcodes.DALOAD, "DALOAD");
        opcodeMap.put(Opcodes.DASTORE, "DASTORE");
        opcodeMap.put(Opcodes.FALOAD, "FALOAD");
        opcodeMap.put(Opcodes.FASTORE, "FASTORE");
        opcodeMap.put(Opcodes.IALOAD, "IALOAD");
        opcodeMap.put(Opcodes.IASTORE, "IASTORE");
        opcodeMap.put(Opcodes.LALOAD, "LALOAD");
        opcodeMap.put(Opcodes.LASTORE, "LASTORE");
        opcodeMap.put(Opcodes.ARETURN, "ARETURN");
        opcodeMap.put(Opcodes.DRETURN, "DRETURN");
        opcodeMap.put(Opcodes.FRETURN, "FRETURN");
        opcodeMap.put(Opcodes.IRETURN, "IRETURN");
        opcodeMap.put(Opcodes.LRETURN, "LRETURN");
        opcodeMap.put(Opcodes.RETURN, "RETURN");
        opcodeMap.put(Opcodes.SWAP, "SWAP");
        opcodeMap.put(Opcodes.DUP, "DUP");
        opcodeMap.put(Opcodes.DUP_X1, "DUP_X1");
        opcodeMap.put(Opcodes.DUP_X2, "DUP_X2");
        opcodeMap.put(Opcodes.DUP2, "DUP2");
        opcodeMap.put(Opcodes.DUP2_X1, "DUP2_X1");
        opcodeMap.put(Opcodes.DUP2_X2, "DUP2_X2");
        opcodeMap.put(Opcodes.POP, "POP");
        opcodeMap.put(Opcodes.POP2, "POP2");
        opcodeMap.put(Opcodes.ICONST_M1, "ICONST_M1");
        opcodeMap.put(Opcodes.ICONST_0, "ICONST_0");
        opcodeMap.put(Opcodes.ICONST_1, "ICONST_1");
        opcodeMap.put(Opcodes.ICONST_2, "ICONST_2");
        opcodeMap.put(Opcodes.ICONST_3, "ICONST_3");
        opcodeMap.put(Opcodes.ICONST_4, "ICONST_4");
        opcodeMap.put(Opcodes.ICONST_5, "ICONST_5");
        opcodeMap.put(Opcodes.LCONST_0, "LCONST_0");
        opcodeMap.put(Opcodes.LCONST_1, "LCONST_1");
        opcodeMap.put(Opcodes.FCONST_0, "FCONST_0");
        opcodeMap.put(Opcodes.FCONST_1, "FCONST_1");
        opcodeMap.put(Opcodes.FCONST_2, "FCONST_2");
        opcodeMap.put(Opcodes.DCONST_0, "DCONST_0");
        opcodeMap.put(Opcodes.DCONST_1, "DCONST_1");
        opcodeMap.put(Opcodes.LDC, "LDC");
        opcodeMap.put(Opcodes.GETSTATIC, "GETSTATIC");
        opcodeMap.put(Opcodes.PUTSTATIC, "PUTSTATIC");
        opcodeMap.put(Opcodes.GETFIELD, "GETFIELD");
        opcodeMap.put(Opcodes.PUTFIELD, "PUTFIELD");
        opcodeMap.put(Opcodes.INVOKEVIRTUAL, "INVOKEVIRTUAL");
        opcodeMap.put(Opcodes.INVOKESPECIAL, "INVOKESPECIAL");
        opcodeMap.put(Opcodes.INVOKESTATIC, "INVOKESTATIC");
        opcodeMap.put(Opcodes.INVOKEINTERFACE, "INVOKEINTERFACE");
        opcodeMap.put(Opcodes.INVOKEDYNAMIC, "INVOKEDYNAMIC");
        opcodeMap.put(Opcodes.NEW, "NEW");
        opcodeMap.put(Opcodes.NEWARRAY, "NEWARRAY");
        opcodeMap.put(Opcodes.ANEWARRAY, "ANEWARRAY");
        opcodeMap.put(Opcodes.ARRAYLENGTH, "ARRAYLENGTH");
        opcodeMap.put(Opcodes.ATHROW, "ATHROW");
        opcodeMap.put(Opcodes.CHECKCAST, "CHECKCAST");
        opcodeMap.put(Opcodes.INSTANCEOF, "INSTANCEOF");
        opcodeMap.put(Opcodes.MONITORENTER, "MONITORENTER");
        opcodeMap.put(Opcodes.MONITOREXIT, "MONITOREXIT");
        opcodeMap.put(Opcodes.IF_ACMPEQ, "IF_ACMPEQ");
        opcodeMap.put(Opcodes.IF_ACMPNE, "IF_ACMPNE");
        opcodeMap.put(Opcodes.IF_ICMPEQ, "IF_ICMPEQ");
        opcodeMap.put(Opcodes.IF_ICMPNE, "IF_ICMPNE");
        opcodeMap.put(Opcodes.IF_ICMPLT, "IF_ICMPLT");
        opcodeMap.put(Opcodes.IF_ICMPGE, "IF_ICMPGE");
        opcodeMap.put(Opcodes.IF_ICMPGT, "IF_ICMPGT");
        opcodeMap.put(Opcodes.IF_ICMPLE, "IF_ICMPLE");
        opcodeMap.put(Opcodes.IFEQ, "IFEQ");
        opcodeMap.put(Opcodes.IFNE, "IFNE");
        opcodeMap.put(Opcodes.IFLT, "IFLT");
        opcodeMap.put(Opcodes.IFGE, "IFGE");
        opcodeMap.put(Opcodes.IFGT, "IFGT");
        opcodeMap.put(Opcodes.IFLE, "IFLE");
        opcodeMap.put(Opcodes.GOTO, "GOTO");
        opcodeMap.put(Opcodes.JSR, "JSR");
        opcodeMap.put(Opcodes.RET, "RET");
        opcodeMap.put(Opcodes.TABLESWITCH, "TABLESWITCH");
        opcodeMap.put(Opcodes.LOOKUPSWITCH, "LOOKUPSWITCH");
        opcodeMap.put(Opcodes.IADD, "IADD");
        opcodeMap.put(Opcodes.ISUB, "ISUB");
        opcodeMap.put(Opcodes.IMUL, "IMUL");
        opcodeMap.put(Opcodes.IDIV, "IDIV");
        opcodeMap.put(Opcodes.IREM, "IREM");
        opcodeMap.put(Opcodes.INEG, "INEG");
        opcodeMap.put(Opcodes.ISHL, "ISHL");
        opcodeMap.put(Opcodes.ISHR, "ISHR");
        opcodeMap.put(Opcodes.IUSHR, "IUSHR");
        opcodeMap.put(Opcodes.IAND, "IAND");
        opcodeMap.put(Opcodes.IOR, "IOR");
        opcodeMap.put(Opcodes.IXOR, "IXOR");
        opcodeMap.put(Opcodes.LADD, "LADD");
        opcodeMap.put(Opcodes.LSUB, "LSUB");
        opcodeMap.put(Opcodes.LMUL, "LMUL");
        opcodeMap.put(Opcodes.LDIV, "LDIV");
        opcodeMap.put(Opcodes.LREM, "LREM");
        opcodeMap.put(Opcodes.LNEG, "LNEG");
        opcodeMap.put(Opcodes.LSHL, "LSHL");
        opcodeMap.put(Opcodes.LSHR, "LSHR");
        opcodeMap.put(Opcodes.LUSHR, "LUSHR");
        opcodeMap.put(Opcodes.LAND, "LAND");
        opcodeMap.put(Opcodes.LOR, "LOR");
        opcodeMap.put(Opcodes.LXOR, "LXOR");
        opcodeMap.put(Opcodes.FADD, "FADD");
        opcodeMap.put(Opcodes.FSUB, "FSUB");
        opcodeMap.put(Opcodes.FMUL, "FMUL");
        opcodeMap.put(Opcodes.FDIV, "FDIV");
        opcodeMap.put(Opcodes.FREM, "FREM");
        opcodeMap.put(Opcodes.FNEG, "FNEG");
        opcodeMap.put(Opcodes.DADD, "DADD");
        opcodeMap.put(Opcodes.DSUB, "DSUB");
        opcodeMap.put(Opcodes.DMUL, "DMUL");
        opcodeMap.put(Opcodes.DDIV, "DDIV");
        opcodeMap.put(Opcodes.DREM, "DREM");
        opcodeMap.put(Opcodes.DNEG, "DNEG");
        opcodeMap.put(Opcodes.I2L, "I2L");
        opcodeMap.put(Opcodes.I2F, "I2F");
        opcodeMap.put(Opcodes.I2D, "I2D");
        opcodeMap.put(Opcodes.L2I, "L2I");
        opcodeMap.put(Opcodes.L2F, "L2F");
        opcodeMap.put(Opcodes.L2D, "L2D");
        opcodeMap.put(Opcodes.F2I, "F2I");
        opcodeMap.put(Opcodes.F2L, "F2L");
        opcodeMap.put(Opcodes.F2D, "F2D");
        opcodeMap.put(Opcodes.D2I, "D2I");
        opcodeMap.put(Opcodes.D2L, "D2L");
        opcodeMap.put(Opcodes.D2F, "D2F");
    
    }
    
    

    public static String getOpcodeString(int opcode) {
        return opcodeMap.getOrDefault(opcode, "UNKNOWN OPCODE");
    }



    private Inst pop1() {
        if (peek() instanceof OneSlotInst) return pop();
        else {
            System.out.println("inconsistent stack state");
            System.exit(0);
            return null;
        }
    }
    private Inst pop2() {
        if (peek() instanceof TwoSlotInst) return pop();
        else {
            System.out.println("inconsistent stack state");
            System.exit(0);
            return null;
        }
    }

    public void handle(AbstractInsnNode inst) {

        Inst v1,v2,v3,v4;

        switch (inst.getOpcode()) {
            // --- Constants ---
            case Opcodes.NOP:
                break; 
                
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                push(new OneSlotInst(inst));
                break; 
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                push(new TwoSlotInst(inst));
                break; 
            case Opcodes.LDC:
                LdcInsnNode ldcinst = (LdcInsnNode)inst;
                Object val = ldcinst.cst;
                if ((val instanceof Long) || (val instanceof Double)) {
                    push(new TwoSlotInst(inst));
                } else
                    push(new OneSlotInst(inst));
                break;
            
            // --- local variables  ---
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.ALOAD:
                push(new OneSlotInst(inst));
                break; 
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                push(new TwoSlotInst(inst));
                break; 
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                pop1();
                break; 
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop2();
                break; 
            case Opcodes.IINC:
                break;

            // --- Arithmetic operations ---
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.IXOR:
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
                pop1();pop1();
                push(new OneSlotInst(inst));
                break; 
            case Opcodes.INEG:
            case Opcodes.FNEG:
                pop1();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LOR:
            case Opcodes.LXOR:
            case Opcodes.LREM:
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                pop2();pop2();
                push(new TwoSlotInst(inst));
                break; 
            case Opcodes.LNEG:
            case Opcodes.DNEG:
                pop2();
                push(new TwoSlotInst(inst));
                break;
            case Opcodes.DCMPG:
            case Opcodes.DCMPL:
                pop2();pop2();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.FCMPG:
            case Opcodes.FCMPL:
                pop1();pop1();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                pop1();pop2();
                push(new TwoSlotInst(inst));
                break;
                

            // --- Conditional branch ---
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                pop1();
                break;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                pop1();pop1();
                break;

            // --- Flow control ---
            case Opcodes.GOTO:
            case Opcodes.JSR:
                break; 
            case Opcodes.RET:
                break; 

            // --- Array instructions ---
            case Opcodes.ARRAYLENGTH:
            case Opcodes.ANEWARRAY:
            case Opcodes.NEWARRAY:
                pop1();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.MULTIANEWARRAY:
                MultiANewArrayInsnNode arrayinst = (MultiANewArrayInsnNode)inst;
                String desc = arrayinst.desc;
                int nbdim = 0;
                while (desc.charAt(nbdim) == '[') {
                    nbdim++;
                    pop1();
                }
                push(new OneSlotInst(inst));
                break;

            case Opcodes.IALOAD:
            case Opcodes.FALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                pop1();pop1();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.LALOAD:
            case Opcodes.DALOAD:
                pop1();pop1();
                push(new TwoSlotInst(inst));
                break;
            case Opcodes.IASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
                pop1();pop1();pop1();
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                pop1();pop1();pop2();
                break;

            // --- Conversion instructions  ---
            case Opcodes.I2L:
            case Opcodes.F2L:
            case Opcodes.I2D:
            case Opcodes.F2D:
                pop1();
                push(new TwoSlotInst(inst));
                break;
            case Opcodes.L2I:
            case Opcodes.L2F:
            case Opcodes.D2I:
            case Opcodes.D2F:
                pop2();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.I2F:
            case Opcodes.F2I:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
                pop1();
                push(new OneSlotInst(inst));
                break;
            case Opcodes.D2L:
            case Opcodes.L2D:
                pop2();
                push(new TwoSlotInst(inst));
                break;
            case Opcodes.LAND:
                pop2();pop2();
                push(new TwoSlotInst(inst));
                break;
            case Opcodes.LCMP:
                pop2();pop2();
                push(new OneSlotInst(inst));
                break;

            // --- Return instructions ---
            case Opcodes.RETURN:
                break;
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                pop1();
                break;
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop2();
                break;

            // --- Field instructions ---
            case Opcodes.GETSTATIC: {
                FieldInsnNode fieldinst = (FieldInsnNode) inst;
                if ((fieldinst.desc.equals("J")) || (fieldinst.desc.equals("D"))) {
                    push(new TwoSlotInst(inst));
                } else
                    push(new OneSlotInst(inst));
                }
                break;
            case Opcodes.PUTSTATIC: {
                FieldInsnNode fieldinst = (FieldInsnNode) inst;
                if ((fieldinst.desc.equals("J")) || (fieldinst.desc.equals("D"))) {
                    pop2();
                } else
                    pop1();
                }
                break;
            case Opcodes.GETFIELD: {
                FieldInsnNode fieldinst = (FieldInsnNode) inst;
                pop1();
                if ((fieldinst.desc.equals("J")) || (fieldinst.desc.equals("D"))) {
                    push(new TwoSlotInst(inst));
                } else
                    push(new OneSlotInst(inst));
                }
                break;
            case Opcodes.PUTFIELD: {
                FieldInsnNode fieldinst = (FieldInsnNode) inst;
                pop1();
                if ((fieldinst.desc.equals("J")) || (fieldinst.desc.equals("D"))) {
                    pop2();
                } else
                    pop1();
                }
                break;

            // --- Object instructions ---
            case Opcodes.NEW:
                push(new OneSlotInst(inst));
                break;

            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
                pop1();
                Method meth = null;
                if (inst instanceof MethodInsnNode) {
                    MethodInsnNode methodinst = (MethodInsnNode) inst;
                    meth = new Method(methodinst.name, methodinst.desc);
                }
               
                String retdesc = meth.getReturnType().getDescriptor();
                if (retdesc.equals("V")) {
                    break;
                } else
                if (retdesc.equals("J") || retdesc.equals("D")) {
                    push(new TwoSlotInst(inst));
                } else {
                    push(new OneSlotInst(inst));
                }
                break;
            case Opcodes.INVOKEDYNAMIC:
            case Opcodes.INVOKESTATIC:
                 meth = null;
                if (inst instanceof MethodInsnNode) {
                    MethodInsnNode methodinst = (MethodInsnNode) inst;
                    meth = new Method(methodinst.name, methodinst.desc);
                }
                if (inst instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode methodinst = (InvokeDynamicInsnNode) inst;
                    meth = new Method(methodinst.name, methodinst.desc);
                }
                Type[] args = meth.getArgumentTypes();
                for (int i = args.length - 1; i >= 0; i--) {
                    String d = args[i].getDescriptor();
                    if (d.equals("J") || d.equals("D")) {
                        pop2();
                    } else {
                        pop1();
                    }
                }
                retdesc = meth.getReturnType().getDescriptor();
                if (retdesc.equals("V")) {
                    break;
                } else
                if (retdesc.equals("J") || retdesc.equals("D")) {
                    push(new TwoSlotInst(inst));
                } else {
                    push(new OneSlotInst(inst));
                }
                break;
            case Opcodes.INSTANCEOF:
                pop1();
                push(new OneSlotInst(inst));
                break;

            // --- Special instructions ---
            case Opcodes.POP:
                pop1();
                break; 
            case Opcodes.POP2:
                if (peek() instanceof OneSlotInst) {
                    pop1();pop1();
                } else pop2();
                break;
            case Opcodes.DUP:
                if (!(peek() instanceof OneSlotInst)) {System.out.println("inconsistent DUP");System.exit(0);}
                push(peek());
                break; 
            case Opcodes.DUP2:
                if (peek() instanceof OneSlotInst) {
                    v1=pop1();v2=pop1();
                    push(v2);push(v1);push(v2);push(v1);
                } else {
                    push(peek());
                }
                break; 
            case Opcodes.DUP_X1:
                v1=pop1();v2=pop1();
                if ((v1 instanceof TwoSlotInst) || (v2 instanceof TwoSlotInst)) {System.out.println("inconsistent DUP");System.exit(0);}
                push(v1);push(v2);push(v1);
                break;
            case Opcodes.DUP_X2:
                v1=pop1();
                if (peek() instanceof OneSlotInst) {
                    v2=pop1();v3=pop1();
                    push(v1);push(v3);push(v2);push(v1);
                } else {
                    v2=pop2();
                    push(v1);push(v2);push(v1);
                }
                break; 
            case Opcodes.DUP2_X1:
                v1=pop1();v2=pop1();v3=pop1();
                push(v2);push(v1);push(v3);push(v2);push(v1);
                break; 
            case Opcodes.DUP2_X2:
                v1=pop1();v2=pop1();
                v3=pop();v4=pop();
                push(v2);push(v1);push(v4);push(v3);push(v2);push(v1);
                break; 
            case Opcodes.SWAP:
                v1=pop1();v2=pop1();
                push(v1);push(v2);
                break;

            // --- Synchronisation ---
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
                pop1();
                break;

            // --- Exceptions ---
            case Opcodes.ATHROW:
                pop1();
                break;

            // --- Switch instructions ---
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
                pop1();
                break;

            case Opcodes.F_NEW:
                break;
            
            case Opcodes.CHECKCAST: 
                break; 
            default:
                System.out.println("Opcode non supporté : " + getOpcodeString(inst.getOpcode()));
                System.exit(0);
        }
        //System.out.println("stack size: "+size());
    }

}
