package org.perfagent;

import jdk.internal.org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.lang.instrument.*;
import java.security.ProtectionDomain;

public class PerfAgent {
    static Instrumentation inst;
    static boolean debug = false;

    public static void premain(String agentArgs, Instrumentation inst)
    {
        ClassFileTransformer transformer = new CollectionTransformer();
        inst.addTransformer(transformer, true);
        PerfAgent.inst = inst;
        System.out.println("Im 1st premain,My agentAges = [" + agentArgs + "].");
    }

    static class CollectionTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            ClassReader source = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(source, ClassWriter.COMPUTE_MAXS);

            ClassVisitor cv;

            if (className.startsWith("scala/collection/")) {
                cv = new CollectionClassAdapter(className, cw);
            } else {
                cv = new NonCollectionClassAdapter(className, cw);
            }

            source.accept(cv, 0);


            byte[] res = cw.toByteArray();
            if (res == classfileBuffer) return null;
            else {
                if (debug) {
                    try {
                        FileOutputStream fos = new FileOutputStream("E:\\ASM_output\\" + className + ".class");
                        fos.write(res);
                    } catch (Exception e) {
                        System.out.println("print modified class file " + className + " failed with " + e);
                    }
                }
                return res;
            }
        }
    }

    static class CollectionClassAdapter extends ClassVisitor implements Opcodes {
        private String cname;
        public CollectionClassAdapter(String cname, final ClassVisitor cv) {
            super(ASM4, cv);
            this.cname = cname;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if ("<init>".equals(name)) {
                return mv == null ? null : new CollectionCtorVisitor(cname, name, desc, mv);
            }
            return mv == null ? null : new CollectionMethodVisitor(cname, name, access, desc, mv);
        }
    }

    static class CollectionMethodVisitor extends MethodVisitor implements Opcodes {
        private int mAccess;
        private String mDesc;
        private String mName;
        private String cName;
        public CollectionMethodVisitor(String cname, String mname, int access, String desc, MethodVisitor mv)  {
            super(ASM4, mv);
            cName = cname;
            mName = mname;
            mDesc = desc;
            mAccess = access;
        }

        @Override
        public void visitCode() {
            if ((mAccess & ACC_STATIC) != 0) {
                CollectionInjectHelper.traceStaticInvokedWithParam(mv, cName, mName, mDesc);
            } else {
                CollectionInjectHelper.traceInvokedWithParam(mv, cName, mName, mDesc);
            }
            super.visitCode();
        }
    }

    static class CollectionCtorVisitor extends MethodVisitor implements Opcodes {
        private String mDesc;
        private String mName;
        private String cName;
        public CollectionCtorVisitor(String cname, String mname, String desc, MethodVisitor mv)  {
            super(ASM4, mv);
            cName = cname;
            mName = mname;
            mDesc = desc;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                CollectionInjectHelper.traceCtorInvokedWithParam(mv, cName, mName, mDesc);
            }
            super.visitInsn(opcode);
        }
    }

    static class NonCollectionClassAdapter extends ClassVisitor implements Opcodes {
        private String cname;

        public NonCollectionClassAdapter(String cname, final ClassVisitor cv) {
            super(ASM4, cv);
            this.cname = cname;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return mv == null ? null : new CollectionInvokerVisitor(cname, name, mv);
        }
    }

    static class CollectionInvokerVisitor extends MethodVisitor implements Opcodes {
        private String cname;
        private String mname;
        public CollectionInvokerVisitor(String cname, String mname, final MethodVisitor mv) {
            super(ASM4, mv);
            this.cname = cname;
            this.mname = mname;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isStatic) {
            if (mv != null && owner.startsWith("scala/collection/")) {
                CollectionInjectHelper.traceUserInvoked(mv, cname, mname, owner, name, desc);
            }

            super.visitMethodInsn(opcode, owner, name, desc, isStatic);
        }
    }

    static class CollectionInjectHelper implements Opcodes {

        public static void traceStaticInvokedWithParam(MethodVisitor mv, String cName, String mName, String mDesc) {
            Type[] paramTypes = Type.getArgumentTypes(mDesc);
            if (paramTypes.length > 0) {
                int index = genParam(mv, paramTypes, false);

                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceStaticInvokedWithParam",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", true);
            } else {
                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceStaticInvoked",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", true);

            }
        }

        public static void traceInvokedWithParam(MethodVisitor mv, String cName, String mName, String mDesc) {
            Type[] paramTypes = Type.getArgumentTypes(mDesc);
            if (paramTypes.length > 0) {
                int index = genParam(mv, paramTypes, false);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceInvokedWithParam",
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", true);
            } else {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceInvoked",
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", true);

            }
        }

        public static void traceCtorInvokedWithParam(MethodVisitor mv, String cName, String mName, String mDesc) {
            Type[] paramTypes = Type.getArgumentTypes(mDesc);
            if (paramTypes.length > 0) {
                int index = genParam(mv, paramTypes, false);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceCtorInvokedWithParam",
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", true);
            } else {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(cName);
                mv.visitLdcInsn(mName);
                mv.visitLdcInsn(mDesc);
                mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceCtorInvoked",
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", true);

            }
        }

        private static void visitConstNum(MethodVisitor mv, int num) {
            if (num < 5) mv.visitInsn(ICONST_0 + num);
            else mv.visitIntInsn(BIPUSH, num);
        }
        private static int genParam(MethodVisitor mv, Type[] paramTypes, boolean isStatic) {
            visitConstNum(mv, paramTypes.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            int readIndex = isStatic ? 0 : 1;
            int arrayIndex = 0;
            for (Type tp : paramTypes) {
                mv.visitInsn(DUP);
                visitConstNum(mv, arrayIndex++);

                if (tp.equals(Type.BOOLEAN_TYPE)) {
                    mv.visitVarInsn(ILOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", true);
                } else if (tp.equals(Type.BYTE_TYPE)) {
                    mv.visitVarInsn(ILOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", true);
                } else if (tp.equals(Type.CHAR_TYPE)) {
                    mv.visitVarInsn(ILOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", true);
                } else if (tp.equals(Type.SHORT_TYPE)) {
                    mv.visitVarInsn(ILOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", true);
                } else if (tp.equals(Type.INT_TYPE)) {
                    mv.visitVarInsn(ILOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", true);
                } else if (tp.equals(Type.LONG_TYPE)) {
                    mv.visitVarInsn(LLOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", true);
                    ++readIndex;
                } else if (tp.equals(Type.FLOAT_TYPE)) {
                    mv.visitVarInsn(FLOAD, readIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", true);
                } else if (tp.equals(Type.DOUBLE_TYPE)) {
                    mv.visitVarInsn(DLOAD, readIndex++);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", true);
                    ++readIndex;
                } else mv.visitVarInsn(ALOAD, readIndex);

                mv.visitInsn(AASTORE);
                ++readIndex;
            }

            mv.visitVarInsn(ASTORE, readIndex);

            return readIndex;
        }

        public static void traceUserInvoked(MethodVisitor mv, String invoker, String callerMethod, String colCls, String method, String desc) {
            mv.visitLdcInsn(invoker);
            mv.visitLdcInsn(callerMethod);
            mv.visitLdcInsn(colCls);
            mv.visitLdcInsn(method);
            mv.visitLdcInsn(desc);
            mv.visitMethodInsn(INVOKESTATIC, "org/perfagent/CollectionTraceSupport", "traceUserInvoked",
               "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", true);
        }

    }

}
