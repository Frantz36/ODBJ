package pack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;



public class CodeParserExceptions {

    static String owner;
    static boolean parse = true;
    static boolean second = false;
    static Boolean bclinit = false;

    static List<TryCatchBlockNode> newtcb = new ArrayList<TryCatchBlockNode>();
    static List<TryCatchBlockNodeStruct> tcbstruct = new ArrayList<TryCatchBlockNodeStruct>();


    private static final Map<String,String> typeTranslation = new HashMap<String,String>() {{
//        put("Ljava/net/Socket;","Lodbj/MySocket;");
//        put("Ljava/net/ServerSocket;","Lodbj/MyServerSocket;");
//        put("Ljava/io/OutputStream;","Lodbj/MyOutputStream;");
//        put("Ljava/io/InputStream;","Lodbj/MyInputStream;");
//        put("Ljava/io/FileInputStream;","Lodbj/MyFileInputStream;");
    }};

    // only for NEW and method owners
    private static final Map<String,String> classTranslation = new HashMap<String,String>() {{
//        put("java/net/Socket","odbj/MySocket");
//        put("java/net/ServerSocket","odbj/MyServerSocket");
//        put("java/io/OutputStream","odbj/MyOutputStream");
//        put("java/io/InputStream","odbj/MyInputStream");
//        put("java/io/FileInputStream","odbj/MyFileInputStream");
    }};

    //////////////////////////////////////////
    static void addCreateTable(MethodNode m) {
        System.out.println("Creating hashtable");
        InsnList l = new InsnList();
        l.add(new TypeInsnNode(Opcodes.NEW, "java/util/Hashtable"));
        l.add(new InsnNode(Opcodes.DUP));
        l.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V", false));
        l.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "_tables", "Ljava/util/Hashtable;"));
        m.instructions.insert(l);
    }

    static List<FieldNode> parseFields(List<FieldNode> fields) {

        fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "_tables", "Ljava/util/Hashtable;", null, null));
        if (second != false) {
            List<FieldNode> lf = new ArrayList<FieldNode>();
            for (FieldNode f : fields) {
                String desttype = typeTranslation.get(f.desc);
                if (desttype != null) {
                    System.out.println("FIELD: name: " + f.name + "  type: " + f.desc);
                    lf.add(new FieldNode(f.access, f.name, desttype, null, null));
                } else
                    lf.add(f);
            }
            return lf;
        }
        return fields;
    }

    static LocalVariableNode getLocalVarByIndex (MethodNode m,  AbstractInsnNode inst){
        LocalVariableNode lv_ref = null;
        for (LocalVariableNode loc : m.localVariables){
            if ( loc.index == (((VarInsnNode)inst).var)){
                lv_ref = loc;
                break;
            }
        }
        return lv_ref;
    }
    public static MethodNode getMethodNodeFromInsn(MethodInsnNode methodInsnNode) {

        String className = methodInsnNode.owner;
        try{
            ClassReader classReader = new ClassReader(className);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            // Parcourir les méthodes de la classe pour trouver celle qui correspond
            List<MethodNode> methods = classNode.methods;
            for (MethodNode method : methods) {
                if (method.name.equals(methodInsnNode.name) && method.desc.equals(methodInsnNode.desc)) {
                    return method; // Méthode trouvée
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        // Méthode introuvable
        return null;
    }

    public static FieldNode getFieldNodeFromInsn(FieldInsnNode fieldInsnNode) {
        // Charger la classe propriétaire du champ
        String className = fieldInsnNode.owner;

        // Charger la classe avec ASM
        try{
            ClassReader classReader = new ClassReader(className);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            // Parcourir les champs pour trouver celui qui correspond
            List<FieldNode> fields = classNode.fields;
            for (FieldNode field : fields) {
                if (field.name.equals(fieldInsnNode.name) && field.desc.equals(fieldInsnNode.desc)) {
                    System.out.println("Found field "+field.name);
                    return field; // Champ trouvé
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        // Champ introuvable
        return null;
    }

    //////////////////////////////////////////
    /// replace each (type to adapt) parameter in a method signature
    /// trace : whether we should print traces of the adapter parameters
    static String parseMethodDesc(String name, String desc, boolean trace) {

        if (parse == true) {

            String desttype = "";
            Method meth = new Method(name, desc);
            Type ret = meth.getReturnType();
            if (second == false) {
                if (trace) System.out.println("METHOD translated (" + name + "): result " + ret);
                desttype = typeTranslation.get(ret.getDescriptor());
                if (desttype != null) {
                    ret = Type.getType(desttype);
                    if (trace) System.out.println("METHOD translated (" + name + "): result " + ret);
                }
            }

            if (second == false) {
                int i = 0;
                Type[] args = meth.getArgumentTypes();
                ArrayList<Type> newargs = new ArrayList<Type>();
                for (Type t : args) {
                    desttype = typeTranslation.get(t.getDescriptor());
                    if (desttype != null) {
                        newargs.add(Type.getType(desttype));
                        if (trace)
                            System.out.println("METHOD (" + name + "): arg[" + i + "][" + Type.getType(desttype) + "]");
                    } else
                        newargs.add(t);
                    i++;
                }
                meth = new Method(name, ret, newargs.toArray(args));
                return meth.getDescriptor();
            }

        }
        return desc;
    }

    /// replace each (type to adapt) local variable
    static void parseLocalVariables(MethodNode m) {
        if (second != false) {
            System.out.println("Translation of variables");
            List<LocalVariableNode> list = m.localVariables;
            List<LocalVariableNode> newlist = new ArrayList<LocalVariableNode>();
            if (list.isEmpty()) System.out.println("pas de var locales");
            for (LocalVariableNode lv : list) {
                String desttype = typeTranslation.get(lv.desc);
                if (desttype != null) {
                    System.out.println("METHOD (" + m.name + "): local var name: " + lv.name + " index: " + lv.index + " size: " + Type.getType(lv.desc).getSize());
                    newlist.add(new LocalVariableNode(lv.name, desttype, null, lv.start, lv.end, lv.index));
                } else {
                    newlist.add(lv);
                    System.out.println("METHOD (" + m.name + "): local var name: " + lv.name + " index: " + lv.index + " not traslated");

                }
            }
            m.localVariables = newlist;
        }
    }


    //////////////////////////////////////////
    static void parseInstructions(MethodNode m) {
        InstructionStack stack = new InstructionStack();

        InsnList instructions = m.instructions;
        if (instructions.size() == 0) return;
        LabelNode start = null;
        LabelNode end = null;
        if (start == null){
            start = new LabelNode();
            instructions.insert(start);
        }
        InsnList toInject = new InsnList();

        toInject.add(new TypeInsnNode(Opcodes.NEW, "java/util/Hashtable"));
        toInject.add(new InsnNode(Opcodes.DUP));
        toInject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V", false));
        toInject.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "_tables", "Ljava/util/Hashtable;"));
//        toInject.add(new InsnNode(Opcodes.RETURN));
        bclinit = true;
        System.out.println("clinit parcouru");

// req = new MyHttpServletRequest(req);
        toInject.add(new TypeInsnNode(Opcodes.NEW, "odbj/MyHttpServletRequest"));
        toInject.add(new InsnNode(Opcodes.DUP));
        toInject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slot 1 = req
//        toInject.add(new InsnNode(Opcodes.ICONST_0));//Mettre false au niveau du booléen qui sera pris en paramètre
        toInject.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "odbj/MyHttpServletRequest",
                "<init>",
                "(Ljakarta/servlet/http/HttpServletRequest;)V",
                false
        ));
        toInject.add(new VarInsnNode(Opcodes.ASTORE, 1));

// resp = new MyHttpServletResponse(resp);
        toInject.add(new TypeInsnNode(Opcodes.NEW, "odbj/MyHttpServletResponse"));
        toInject.add(new InsnNode(Opcodes.DUP));
        toInject.add(new VarInsnNode(Opcodes.ALOAD, 2)); // slot 2 = resp
//        toInject.add(new InsnNode(Opcodes.ICONST_0));//Mettre false au niveau du booléen qui sera pris en paramètre
        toInject.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "odbj/MyHttpServletResponse",
                "<init>",
                "(Ljakarta/servlet/http/HttpServletResponse;)V",
                false
        ));
        toInject.add(new VarInsnNode(Opcodes.ASTORE, 2));

//        if(!bclinit) {
//        InsnList li = new InsnList();
        /*toInject.add(new TypeInsnNode(Opcodes.NEW, "java/util/Hashtable"));
        toInject.add(new InsnNode(Opcodes.DUP));
        toInject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V", false));
        toInject.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "_tables", "Ljava/util/Hashtable;"));
        toInject.add(new InsnNode(Opcodes.RETURN));
        bclinit = true;
        System.out.println("clinit parcouru");*/
//        }

// Insérer juste après le constructeur super()
        if (m.name.equals("doGet")){
            AbstractInsnNode insertAfter = null;
            for (AbstractInsnNode insn : m.instructions) {
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL &&
                        insn instanceof MethodInsnNode method &&
                        method.name.equals("<init>")) {
                    insertAfter = insn.getNext();
                    break;
                }
            }
            if (insertAfter != null) {
                m.instructions.insert/*Before*/(/*insertAfter, */toInject);
            } else {
                System.err.println("⚠️ Pas trouvé de <init> dans doGet — injection ignorée");
            }
        }
        if (end == null){
            end = new LabelNode();
            instructions.add(end);
        }

        AbstractInsnNode inst = instructions.getFirst();
        int handler_index = 0;
        int nbAddedVars = 0;
        List<LocalVariableNode> list = m.localVariables;
        List<Integer> methodCalls = Arrays.asList(Opcodes.INVOKESTATIC, Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEDYNAMIC, Opcodes.INVOKESPECIAL);
        List<Integer> returnopcodes = Arrays.asList(Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN);
        List<Integer> modificationOpcodes = Arrays.asList(Opcodes.DUP, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, Opcodes.INEG, Opcodes.I2B, Opcodes.I2C, Opcodes.I2S);

        while (inst != null &&  (!returnopcodes.contains(inst.getOpcode()) || inst.getNext().getOpcode() !=Opcodes.POP )) {
            System.out.println("handleInstruction : "+ m.instructions.indexOf(inst)+" "+getOpcodeName(inst.getOpcode()));

            int op = inst.getOpcode();

            if (inst instanceof TypeInsnNode) {
                TypeInsnNode inst1 = (TypeInsnNode)inst;
                if (op == Opcodes.NEW) {
                    String destclass = classTranslation.get(inst1.desc);
                    if (destclass != null)
                        inst1.desc = destclass;
                }
            }

            if ((inst instanceof MethodInsnNode) || (inst instanceof InvokeDynamicInsnNode)) {
                String methname, methdesc, methowner = null;
                if (inst instanceof MethodInsnNode) {

                    MethodInsnNode methodinst = (MethodInsnNode)inst;
                    methname = methodinst.name;
                    methdesc = methodinst.desc;
                    methowner = methodinst.owner;
                    System.out.println("methoddesc "+methdesc);
                    String newdesc = parseMethodDesc(methname, methdesc, true);

                    System.out.println("methoddesc "+newdesc);
                    // translate the owner class if this class is overhidden (e.g. Socket or OutputStream)
                    // for instance OutputStream becomes MyOutputStream as owner
                    // and [B becomes Pair as a parameter
                    if (inst.getOpcode()== Opcodes.INVOKESTATIC  && methodinst.desc.contains("OutputStream")){
                        System.out.println("vu");
                        System.out.println("name "+ methodinst.name +" desc "+ methodinst.desc + "new desc " + newdesc+"Owner : "+methowner);
                    }
                    if ((methowner.startsWith("app/")) || (methowner.startsWith("odbj/"))) {
                        methodinst.desc = newdesc;
                    }
                    String newowner = classTranslation.get(methowner);
                    System.out.println("name "+ methodinst.name +" desc "+ methodinst.desc + "new desc " + newdesc+"Owner : "+methowner+ " new owner :"+newowner);
                    methodinst.desc = newdesc;
                    if (newowner != null) {
                        methodinst.desc = newdesc;
                        methodinst.owner = newowner;
                    }
                }else {
                    InvokeDynamicInsnNode methodinst = (InvokeDynamicInsnNode)inst;
                    methname = methodinst.name;
                    methdesc = methodinst.desc;
                    System.out.println("instruction INVOKE [dynamic,"+methname+"]");
                    // here I suppose that dynamic invocations are not with the application
                }
            }
            if (inst instanceof FieldInsnNode) {
                FieldInsnNode fieldinst = (FieldInsnNode)inst;
                if (second != false) {
                    String desttype = typeTranslation.get(fieldinst.desc);
                    if (desttype != null) {
                        if ((op == Opcodes.GETSTATIC) || (op == Opcodes.GETFIELD)) {
                            // replace the instruction with one with the (type to adapt) type
                            //m.instructions.set(inst, new FieldInsnNode(op,fieldinst.owner,fieldinst.name,desttype));
                            fieldinst.desc = desttype;
                        }
                        if ((op == Opcodes.PUTSTATIC) || (op == Opcodes.PUTFIELD)) {
                            // replace the instruction with one with the (type to adapt) type
                            //m.instructions.set(inst, new FieldInsnNode(op,fieldinst.owner,fieldinst.name,desttype));
                            fieldinst.desc = desttype;
                        }
                    }
                }

            }

            // BALOAD / BASTORE 
            if (inst instanceof InsnNode) {

                if (op == Opcodes.BALOAD || op==Opcodes.BASTORE) {
                    // we should have on the stack : [B then index then value if op == BASTORE

                    //Potential new local variables for containing reference, index, value  
                    LocalVariableNode new_lv_ref = null;
                    LocalVariableNode new_lv_index = null;
                    LocalVariableNode new_lv_val = null;

                    // variables to set if reference, index, or value have been modifie

                    Boolean refModified = false;
                    Boolean indexModified = false;
                    Boolean valModified = false;
                    Boolean refIsField = false;
                    Boolean indexIsField = false;
                    Boolean valIsField = false;
                    // popping instruction that has pushed potential value 
                    Inst val = null;
                    if (op == Opcodes.BASTORE){
                        val = stack.pop();
                    }
                    if (val != null){
                        if (val.inst instanceof FieldInsnNode)
                            valIsField = true;
                    }
                    //popping instruction that pushed index 
                    Inst index = stack.pop();
                    if (index.inst instanceof FieldInsnNode)
                        indexIsField = true;
                    //popping instruction that pushed reference 
                    Inst ref = stack.pop();
                    if (ref.inst instanceof FieldInsnNode)
                        refIsField = true;


                    // Inspecting instruction if they are modification instructions. If yes, new local variables must be 
                    // created to store new values in order not to recall those instructions on the handler. 
                    Inst indexInst = index;
                    Inst refInst = ref;
                    Inst valInst = val;
                    Inst oldRefInst = ref;
                    // Checking if it is a modification instruction that has pushed the index or the value on which the baload/bastore will be executed 
                    // if so, an intermediate variable is created for each of value and index 
                    if (modificationOpcodes.contains(index.inst.getOpcode())){
                        new_lv_index = new LocalVariableNode("_index_"+handler_index, "I", null, start, end, list.size()+nbAddedVars);
                        list.add(new_lv_index);
                        nbAddedVars++;
                        VarInsnNode istore = new VarInsnNode(Opcodes.ISTORE, new_lv_index.index);
                        instructions.insert(index.inst, istore);
                        VarInsnNode ilaod = new VarInsnNode(Opcodes.ILOAD, new_lv_index.index);
                        instructions.insert(istore, ilaod);
                        indexInst = new Inst();
                        indexInst.inst = ilaod;

                    }
                    if (val != null){ /// ////////////////////////
                        if (modificationOpcodes.contains(val.inst.getOpcode())){
                            new_lv_val = new LocalVariableNode("_value_"+handler_index, "I", null, start, end, list.size()+nbAddedVars);
                            list.add(new_lv_val);
                            nbAddedVars++;
                            VarInsnNode istore = new VarInsnNode(Opcodes.ISTORE, new_lv_val.index);
                            instructions.insert(val.inst, istore);
                            VarInsnNode ilaod = new VarInsnNode(Opcodes.ILOAD, new_lv_val.index);
                            instructions.insert(istore, ilaod);
                            valInst = new Inst();
                            valInst.inst = ilaod;

                        }
                    } ///////////////////


                    // if value or index has been pushed by a methodcall, we assume the 
                    // reference could have been modified
                    //TODO : add check for reference modification in called method

                    if (methodCalls.contains(indexInst.inst.getOpcode())){
                        MethodInsnNode meth =  (MethodInsnNode) indexInst.inst;
                        if (meth.owner.startsWith("app/")){
                            //Inspect wether the called method modifies the reference 
                            if (refIsField)
                                refModified =checkRefModification(getFieldNodeFromInsn((FieldInsnNode) ref.inst), getMethodNodeFromInsn(meth));
                            else
                                refModified = checkRefModification((VarInsnNode) ref.inst, getMethodNodeFromInsn(meth));
                        }
                        indexModified = true;
                    }

                    if (valInst != null) { ///////////////////////////////////
                        if (methodCalls.contains(valInst.inst.getOpcode()) && !refModified) {
                            MethodInsnNode meth = (MethodInsnNode) valInst.inst;
                            if (meth.owner.startsWith("app/")) {
                                //Inspect wether the called method modifies the reference 
                                if (refIsField)
                                    refModified = checkRefModification(getFieldNodeFromInsn((FieldInsnNode) ref.inst), getMethodNodeFromInsn(meth));
                                else
                                    refModified = checkRefModification((VarInsnNode) ref.inst, getMethodNodeFromInsn(meth));
                            }
                            valModified = true;
                        }
                    }   //////////////////////////////////////

                    // instructions that are beween current instruction (bastore/baload) and instrctions that have pushed index and value are inspected
                    // to create potential new localvar that will contain old index/value to restore them after exception  

                    if (!indexIsField || !valIsField){
                        for (AbstractInsnNode i = index.inst.getNext(); i != inst; i = i.getNext()){
                            if (i.getOpcode() == Opcodes.ISTORE  &&  index.inst.getOpcode() == Opcodes.ILOAD){
                                VarInsnNode v = (VarInsnNode) i;
                                if (v.var == ((VarInsnNode) index.inst).var)
                                    indexModified = true;
                                else if (v.var == ((VarInsnNode) val.inst).var)
                                    valModified = true;
                            }else if (i.getOpcode() == Opcodes.IINC  &&  index.inst.getOpcode() == Opcodes.ILOAD){
                                IincInsnNode v = (IincInsnNode) i;
                                if (v.var == ((VarInsnNode) index.inst).var)
                                    indexModified = true;
                                else if (v.var == ((VarInsnNode) val.inst).var)
                                    valModified = true;
                            }else
                            if (i.getOpcode() == Opcodes.ASTORE  &&  ref.inst.getOpcode() == Opcodes.ALOAD){
                                VarInsnNode v = (VarInsnNode) i;
                                if (v.var == ((VarInsnNode) ref.inst).var)
                                    refModified = true;
                            }
                        }
                    }
                    if (indexIsField){
                        for (AbstractInsnNode i = index.inst.getNext(); i != inst; i = i.getNext()){
                            if ((i.getOpcode() == Opcodes.PUTSTATIC && index.inst.getOpcode() == Opcodes.GETSTATIC)||(i.getOpcode() == Opcodes.PUTFIELD && index.inst.getOpcode() == Opcodes.GETFIELD)){
                                FieldInsnNode v = (FieldInsnNode) i;
                                if (getFieldNodeFromInsn(v).name.equals(getFieldNodeFromInsn((FieldInsnNode)index.inst).name)){
                                    indexModified = true;
                                    System.out.println("index is modified");
                                }
                            }
                        }
                    }

                    if (indexModified){
                        InsnNode dup = new InsnNode(Opcodes.DUP);
                        new_lv_index = new LocalVariableNode("_index_"+handler_index, "I", null, start, end, list.size()+nbAddedVars);
                        list.add(new_lv_index);
                        nbAddedVars++;
                        VarInsnNode istore = new VarInsnNode(Opcodes.ISTORE, new_lv_index.index);
                        instructions.insert(index.inst, dup);
                        instructions.insert(dup, istore);
                        VarInsnNode ilaod = new VarInsnNode(Opcodes.ILOAD, new_lv_index.index);
                        indexInst = new Inst();
                        indexInst.inst = ilaod;
                    }
                    if (valModified){
                        InsnNode dup = new InsnNode(Opcodes.DUP);
                        new_lv_val = new LocalVariableNode("_value_"+handler_index, "I", null, start, end, list.size()+nbAddedVars);
                        list.add(new_lv_val);
                        nbAddedVars++;
                        VarInsnNode istore = new VarInsnNode(Opcodes.ISTORE, new_lv_val.index);
                        instructions.insert(index.inst, dup);
                        instructions.insert(dup, istore);
                        VarInsnNode ilaod = new VarInsnNode(Opcodes.ILOAD, new_lv_val.index);
                        //instructions.insert(istore, ilaod);
                        valInst = new Inst();
                        valInst.inst =ilaod;
                    }
                    if (refModified){
                        InsnNode dup = new InsnNode(Opcodes.DUP);
                        new_lv_ref = new LocalVariableNode("_ref_"+handler_index, "[B", null, start, end, list.size()+nbAddedVars);
                        list.add(new_lv_ref);
                        nbAddedVars++;
                        VarInsnNode astore = new VarInsnNode(Opcodes.ASTORE, new_lv_ref.index);
                        instructions.insert(ref.inst, dup);
                        instructions.insert(dup, astore);
                        VarInsnNode aload = new VarInsnNode(Opcodes.ALOAD, new_lv_ref.index);

                        refInst = new Inst();
                        refInst.inst =aload;
                    }

                    LabelNode startlabel = new LabelNode();
                    LabelNode endlabel = new LabelNode();
                    LabelNode handlerlabel = new LabelNode();

                    instructions.insertBefore(inst, startlabel);
                    // surround bastore/baload by start and endlabel to add the exception

                    instructions.insert(inst, endlabel);

                    stack.push(ref);
                    stack.push(index);
                    if (val != null)
                        stack.push(val);

                    stack.handle(inst);
                    inst = endlabel;
                    addExceptionHandler(m, new TryCatchBlockNodeStruct(new TryCatchBlockNode(startlabel, endlabel, handlerlabel, "java/lang/Exception"), refInst , oldRefInst, indexInst , valInst));
                    handler_index++;
                }
            }


            // NEWARRAY
            if (inst instanceof IntInsnNode) {
                IntInsnNode inst1 = (IntInsnNode)inst;
                if (op == Opcodes.NEWARRAY) {
                    if (inst1.operand == 8) {
                        // here, we should have on the stack : size                         
                        InsnList l = new InsnList();
                        AbstractInsnNode i;
                        i = new InsnNode(Opcodes.ICONST_0); l.add(i); stack.handle(i);

                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        i = new IntInsnNode(Opcodes.NEWARRAY,8); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        i = new InsnNode(Opcodes.DUP); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        i = new FieldInsnNode(Opcodes.GETSTATIC, owner,"_tables", "Ljava/util/Hashtable;"); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        i = new InsnNode(Opcodes.SWAP); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        instructions.insertBefore(inst.getPrevious(), l);
                        System.out.println("instruction new array [B]");

                        // store in table
                        i = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        i = new InsnNode(Opcodes.POP); l.add(i); stack.handle(i);
                        System.out.println("instruction NEWARRAY [B] : add inst ("+getOpcodeName(i.getOpcode())+")");

                        instructions.insert(inst, l);
                        inst = i;
                    }
                }
            }
            stack.handle(inst);
            inst = inst.getNext();
        }

    }


    static void addExceptionHandler(MethodNode m, TryCatchBlockNodeStruct tcbs) {


        InsnList l = new InsnList();
        AbstractInsnNode updateIndex,  updateRef, updateVal, repushIndex,  repushRef, repushVal;
        updateIndex =  updateRef = updateVal = repushIndex = repushRef = repushVal = null;

        LocalVariableNode lv_ref = null;
        if (tcbs.oldref.inst.getOpcode() == Opcodes.ALOAD){
            lv_ref = getLocalVarByIndex(m, tcbs.oldref.inst);
            updateRef =  new VarInsnNode(Opcodes.ASTORE, lv_ref.index);
            repushRef = new VarInsnNode(Opcodes.ALOAD, lv_ref.index);
        }else if (tcbs.oldref.inst.getOpcode() == Opcodes.GETFIELD){
            FieldInsnNode f = (FieldInsnNode) tcbs.oldref.inst;
            updateRef = new FieldInsnNode(Opcodes.PUTFIELD, f.owner, f.name, f.desc);
            repushRef = new FieldInsnNode(Opcodes.GETFIELD, f.owner, f.name, f.desc);
        }else if (tcbs.oldref.inst.getOpcode() == Opcodes.GETSTATIC){
            FieldInsnNode f = (FieldInsnNode) tcbs.oldref.inst;
            updateRef = new FieldInsnNode(Opcodes.PUTSTATIC,f.owner, f.name, f.desc);
            repushRef = new FieldInsnNode(Opcodes.GETSTATIC,f.owner, f.name, f.desc);
        }else{
            System.out.println("Inconsistant or new case found " + getOpcodeName(tcbs.oldref.inst.getOpcode()));
        }

        LocalVariableNode lv_index = null;
        if (tcbs.index.inst.getOpcode() == Opcodes.ILOAD){
            lv_index = getLocalVarByIndex(m, tcbs.index.inst);
            updateIndex =  new VarInsnNode(Opcodes.ISTORE, lv_index.index);
            repushIndex = new VarInsnNode(Opcodes.ILOAD, lv_index.index);
        }else if (tcbs.index.inst.getOpcode() == Opcodes.GETFIELD){
            FieldInsnNode f = (FieldInsnNode) tcbs.index.inst;
            updateIndex = new FieldInsnNode(Opcodes.PUTFIELD, f.owner, f.name, f.desc);
            repushIndex = new FieldInsnNode(Opcodes.GETFIELD, f.owner, f.name, f.desc);
        }else if (tcbs.index.inst.getOpcode() == Opcodes.GETSTATIC){
            FieldInsnNode f = (FieldInsnNode) tcbs.index.inst;
            updateIndex = new FieldInsnNode(Opcodes.PUTSTATIC, f.owner, f.name, f.desc);
            repushIndex = new FieldInsnNode(Opcodes.GETSTATIC, f.owner, f.name, f.desc);
        }else{
            repushIndex = new InsnNode(tcbs.index.inst.getOpcode());
            System.out.println("Inconsistant or new case found  " + getOpcodeName(tcbs.index.inst.getOpcode()));
        }

        LocalVariableNode lv_val = null;
        if (tcbs.value != null){
            if (tcbs.value.inst.getOpcode() == Opcodes.ILOAD){
                lv_val = getLocalVarByIndex(m, tcbs.value.inst);
                updateVal =  new VarInsnNode(Opcodes.ISTORE, lv_val.index);
                repushVal = new VarInsnNode(Opcodes.ILOAD, lv_val.index);
            }else if (tcbs.value.inst.getOpcode() == Opcodes.GETFIELD){
                FieldInsnNode f = (FieldInsnNode) tcbs.value.inst;
                updateVal = new FieldInsnNode(Opcodes.PUTFIELD, f.owner, f.name, f.desc);
                repushVal = new FieldInsnNode(Opcodes.GETFIELD, f.owner, f.name, f.desc);
            }else if (tcbs.value.inst.getOpcode() == Opcodes.GETSTATIC){
                FieldInsnNode f = (FieldInsnNode) tcbs.value.inst;
                updateVal = new FieldInsnNode(Opcodes.PUTSTATIC, f.owner, f.name, f.desc);
                repushVal = new FieldInsnNode(Opcodes.GETSTATIC, f.owner, f.name, f.desc);
            }else{
                repushVal = new InsnNode(tcbs.value.inst.getOpcode());
                System.out.println("Inconsistant or new case found  " + getOpcodeName(tcbs.value.inst.getOpcode()));
            }
        }



        List<TryCatchBlockNode> mtcb = m.tryCatchBlocks;

        AbstractInsnNode last = m.instructions.getLast();

        AbstractInsnNode i;


        System.out.println("exception handler ");

        i = tcbs.tcb.handler; l.add(i);


        i = new InsnNode(Opcodes.POP); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");

        i = new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");

        i = new LdcInsnNode("buffer fault"); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");



        i = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");


        i = new FieldInsnNode(Opcodes.GETSTATIC, owner,"_tables", "Ljava/util/Hashtable;"); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");

        l.add(repushRef.clone(null));

        i = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Hashtable", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");

        i = new TypeInsnNode(Opcodes.CHECKCAST, "[B"); l.add(i);
        System.out.println("exception handler : add inst ("+getOpcodeName(i.getOpcode())+")");
        /*added*/
        i = new InsnNode(Opcodes.DUP);
        l.add(i);;
        i = new MethodInsnNode(Opcodes.INVOKESTATIC,"odbj/Handler","bufferFault","([B)V",false);
        l.add(i);

        if (tcbs.ref.inst != tcbs.oldref.inst){
            i = new InsnNode(Opcodes.DUP);
            l.add(i);

            i = new VarInsnNode(Opcodes.ALOAD, ((VarInsnNode) tcbs.ref.inst).var);
            l.add(i);


            LabelNode equal = new LabelNode();
            i = new JumpInsnNode(Opcodes.IF_ACMPNE, equal);
            l.add(i);

            l.add(updateRef);
            l.add(repushRef);
            l.add(equal);
        }else{
            l.add(updateRef);
            l.add(repushRef);
        }

        l.add(repushIndex);
        if (lv_val != null)
            l.add(repushVal);

        i = new JumpInsnNode(Opcodes.GOTO,tcbs.tcb.start); l.add(i);

        m.instructions.insert(last, l);
        i = new LabelNode(); m.instructions.insert(m.instructions.getLast(), i);
        if (lv_ref != null) lv_ref.end =  (LabelNode)i;
        mtcb.add(tcbs.tcb);

    }

    public static Boolean checkRefModification(FieldNode f, MethodNode m){
        InsnList mInsnList = m.instructions;

        for (AbstractInsnNode inst : mInsnList){
            if (inst.getOpcode()==Opcodes.PUTSTATIC){
                FieldInsnNode fInsnNode = (FieldInsnNode) inst;
                if (fInsnNode.name.equals(f.name))
                    return true;
            }
        }
        return false;

    }
    public static Boolean checkRefModification(VarInsnNode f, MethodNode m){
        InsnList mInsnList = m.instructions;

        for (AbstractInsnNode inst : mInsnList){
            if (inst.getOpcode()==Opcodes.ASTORE){
                VarInsnNode var = (VarInsnNode) inst;
                if (var.var == f.var)
                    return true;
            }
        }
        return false;

    }

    public static void main(String args[]) {

        try {
            ClassNode cn = new ClassNode(Opcodes.ASM4);
            ClassReader cr = new ClassReader(args[0]);
            cr.accept(cn, 0);

            System.out.println("parsing class "+cn.name);

            owner = cn.name;

            parseFields(cn.fields);

            for (MethodNode m : cn.methods) {
                System.out.println("METHOD ("+m.name+"): desc : "+ m.desc);

                if (m.name.equals("<clinit>")) { /// Code mort, car on n'a jamais accès au constructeur super() de BackendServer,InterServer... ///
                    addCreateTable(m);
                    bclinit = true;
                    System.out.println("bclinit turn : " + bclinit);
                }
                m.desc = parseMethodDesc(m.name,m.desc, true);
                System.out.println("Variables locales :" +m.localVariables.size());
                parseLocalVariables(m);

                parseInstructions(m);

                System.out.println();


            }

            /*System.out.println("blclint: Still "+bclinit);
            if (!bclinit) {
                System.out.println("blclint: Entrée dans initialisation bclinit");
                MethodNode clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);

                InsnList li = new InsnList();
                li.add(new TypeInsnNode(Opcodes.NEW, "java/util/Hashtable"));
                li.add(new InsnNode(Opcodes.DUP));
                li.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V", false));
                li.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "_tables", "Ljava/util/Hashtable;"));
                li.add(new InsnNode(Opcodes.RETURN));

                clinit.instructions.add(li);
                cn.methods.add(clinit);
                bclinit = true;
            }*/

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            //cn.accept(cw);
            cn.accept(new CheckClassAdapter(cw));

            byte[] b = cw.toByteArray();

            File outputFile = new File("out/"+cn.name+".class");
            if (outputFile.exists()) outputFile.delete();
            outputFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(b);
            fos.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
                for (Field field : Opcodes.class.getFields()) {
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

    public static String getOpcodeName(int opcode) {

        String ret =  map.get(opcode);
        if (ret == null) ret = "unknown";
        return ret;
    }
}
