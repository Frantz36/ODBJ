package pack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class Util {

    // map used to print the opcode names
    static Map<Integer, String> map;

    static {

        map = new HashMap<Integer, String>();

        try {
            String v[] = {"NOP","ACONST_NULL","ICONST_M1","ICONST_0","ICONST_1","ICONST_2","ICONST_3",
            "ICONST_4","ICONST_5","LCONST_0","LCONST_1","FCONST_0","FCONST_1","FCONST_2","DCONST_0","DCONST_1","BIPUSH","SIPUSH",
            "LDC","ILOAD","LLOAD","FLOAD","DLOAD","ALOAD","IALOAD","LALOAD","FALOAD","DALOAD","AALOAD","BALOAD","CALOAD","SALOAD",
            "ISTORE","LSTORE","FSTORE","DSTORE","ASTORE","IASTORE","LASTORE","FASTORE","DASTORE","AASTORE","BASTORE","CASTORE",
            "SASTORE","POP","POP2","DUP","DUP_X1","DUP_X2","DUP2","DUP2_X1","DUP2_X2","SWAP","IADD","LADD","FADD","DADD","ISUB",
            "LSUB","FSUB","DSUB","IMUL","LMUL","FMUL","DMUL","IDIV","LDIV","FDIV","DDIV","IREM","LREM","FREM","DREM","INEG","LNEG",
            "FNEG","DNEG","ISHL","LSHL","ISHR","LSHR","IUSHR","LUSHR","IAND","LAND","IOR","LOR","IXOR","LXOR","IINC","I2L","I2F","I2D",
            "L2I","L2F","L2D","F2I","F2L","F2D","D2I","D2L","D2F","I2B","I2C","I2S","LCMP","FCMPL","FCMPG","DCMPL","DCMPG","IFEQ","IFNE",
            "IFLT","IFGE","IFGT","IFLE","IF_ICMPEQ","IF_ICMPNE","IF_ICMPLT","IF_ICMPGE","IF_ICMPGT","IF_ICMPLE","IF_ACMPEQ","IF_ACMPNE",
            "GOTO","JSR","RET","TABLESWITCH","LOOKUPSWITCH","IRETURN","LRETURN","FRETURN","DRETURN","ARETURN","RETURN","GETSTATIC",
            "PUTSTATIC","GETFIELD","PUTFIELD","INVOKEVIRTUAL","INVOKESPECIAL","INVOKESTATIC","INVOKEINTERFACE","INVOKEDYNAMIC","NEW",
            "NEWARRAY","ANEWARRAY","ARRAYLENGTH","ATHROW","CHECKCAST","INSTANCEOF","MONITORENTER","MONITOREXIT","MULTIANEWARRAY","IFNULL",
            "IFNONNULL"};

            for (String s : v) {
                for (java.lang.reflect.Field field : Opcodes.class.getFields()) {
                    if (field.getName().equals(s)) {
                        //System.out.println("found "+s+" "+field.getInt(null));
                        map.put(field.getInt(null),s);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //////////////////////////////////////////
    /// print the opcode name

    public static String getOpcodeName(int opcode) {

        String ret =  map.get(opcode);
        if (ret == null) ret = "unknown";
        return ret;
    }

    // map of the primitive types
    private static Map<Integer, String> arrayType = new HashMap<Integer, String>() {{
        put(4, "[Z"); // boolean
        put(5, "[C"); // char
        put(6, "[F"); // float
        put(7, "[D"); // double
        put(8, "[B"); // byte
        put(9, "[S"); // short
        put(10, "[I"); // int
        put(11, "[J"); // long
    }};

    //////////////////////////////////////////
    // return the desc of the instruction at the top of the instruction stack
    // for the moment, only used for detection whether this desc is a Pair

    public static String getADesc(MethodNode m, InstructionStack stack) {
   
        AbstractInsnNode inst = stack.peek().inst;
        switch (inst.getOpcode()) {
            case Opcodes.AALOAD:  // degoutant !!!!!!!!!!!!!!!!!!!!!!
                InstructionStack st = (InstructionStack)stack.clone();
                st.pop1(); st.pop1(); // AALOAD + index
                String arraydesc = getADesc(m, st);
                if (!arraydesc.startsWith("[")) return null;
                return arraydesc.substring(1);
            case Opcodes.ALOAD:
            VarInsnNode localinst = (VarInsnNode)inst;
                LocalVariableNode lv = getLocalVar(m, localinst);
                return lv.desc;
            //case Opcodes.CHECKCAST:
            case Opcodes.GETFIELD:
            case Opcodes.GETSTATIC:
                FieldInsnNode fieldinst = (FieldInsnNode)inst;
                return fieldinst.desc;
            case Opcodes.INVOKEDYNAMIC:
                InvokeDynamicInsnNode dyninst = (InvokeDynamicInsnNode)inst;
                Method dynmeth = new Method(dyninst.name, dyninst.desc);
                return dynmeth.getReturnType().getDescriptor();
            case Opcodes.INVOKEINTERFACE:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
                MethodInsnNode methodinst = (MethodInsnNode)inst;
                Method meth = new Method(methodinst.name, methodinst.desc);
                return meth.getReturnType().getDescriptor();
            case Opcodes.MULTIANEWARRAY:
                MultiANewArrayInsnNode multiinst = (MultiANewArrayInsnNode)inst;
                return multiinst.desc;
            case Opcodes.ANEWARRAY:
                TypeInsnNode typeinst = (TypeInsnNode)inst;
                return typeinst.desc;
            case Opcodes.NEWARRAY:
                IntInsnNode newarrayinst = (IntInsnNode)inst;
                return arrayType.get(newarrayinst.operand);
            case Opcodes.NEW:
                // handled by INVOKESPECIAL
        }
        return null;
        // return null when the type handled by the instruction is not interesting (not a Pair)
    }


    //////////////////////////////////////////
    /// return the local variable associated with a VarInsnNode instruction (which uses a local variable)

    static LocalVariableNode getLocalVar(MethodNode m, VarInsnNode inst) {
        LocalVariableNode local = null;
        for (LocalVariableNode lv : m.localVariables)
            if (lv.index == inst.var) {
                local = lv;
                break;
            }
        if (local == null) {
            System.out.println("inconsistent local variable index");
            System.exit(0);
        }
        return local;
    }

}
