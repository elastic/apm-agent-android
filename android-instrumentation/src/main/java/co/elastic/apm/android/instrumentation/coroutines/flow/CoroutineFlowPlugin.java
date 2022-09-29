package co.elastic.apm.android.instrumentation.coroutines.flow;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.DescriptorMatcher;
import net.bytebuddy.matcher.StringMatcher;

import java.io.IOException;

public class CoroutineFlowPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        DescriptorMatcher<MethodDescription> matcher = new DescriptorMatcher<>(new StringMatcher("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", StringMatcher.Mode.ENDS_WITH));
        return builder.visit(Advice.to(CoroutineFlowBlockersAdvice.class).on(matcher));
    }

    @Override
    public boolean matches(TypeDescription typeDescription) {
        return typeDescription.getTypeName().equals("kotlinx.coroutines.flow.FlowKt");
    }

    @Override
    public void close() throws IOException {

    }
}
