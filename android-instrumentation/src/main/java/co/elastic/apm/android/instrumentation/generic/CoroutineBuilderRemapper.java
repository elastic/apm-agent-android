package co.elastic.apm.android.instrumentation.generic;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class CoroutineBuilderRemapper extends ClassVisitor {

    protected CoroutineBuilderRemapper(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new CoroutineLaunchCallMethodRemapper(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
