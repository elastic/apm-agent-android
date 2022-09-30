package co.elastic.apm.android.plugin.instrumentation.remapping;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CoroutineBuilderRemapper extends ClassVisitor {

    public CoroutineBuilderRemapper(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new CoroutineLaunchCallMethodRemapper(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
