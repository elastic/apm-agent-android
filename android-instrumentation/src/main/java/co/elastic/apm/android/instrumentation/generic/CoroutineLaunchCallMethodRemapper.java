package co.elastic.apm.android.instrumentation.generic;

import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

import co.elastic.apm.android.sdk.instrumentation.CoroutineExtensionsKt;

public class CoroutineLaunchCallMethodRemapper extends MethodVisitor {
    private final String elasticLaunchOwner;

    protected CoroutineLaunchCallMethodRemapper(MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
        elasticLaunchOwner = Type.getInternalName(CoroutineExtensionsKt.class);
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
