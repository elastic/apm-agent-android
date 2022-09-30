package co.elastic.apm.android.plugin.instrumentation.remapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CoroutineLaunchCallMethodRemapper extends MethodVisitor {
    private final String elasticLaunchOwner;

    protected CoroutineLaunchCallMethodRemapper(MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
        elasticLaunchOwner = "co/elastic/apm/android/sdk/instrumentation/CoroutineExtensionsKt";
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (isCoroutineLaunchCall(owner, name)) {
            super.visitMethodInsn(opcode, elasticLaunchOwner, name, descriptor, isInterface);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    private boolean isCoroutineLaunchCall(String owner, String name) {
        return owner.equals("kotlinx/coroutines/BuildersKt") && (name.equals("launch") || name.equals("launch$default"));
    }
}
