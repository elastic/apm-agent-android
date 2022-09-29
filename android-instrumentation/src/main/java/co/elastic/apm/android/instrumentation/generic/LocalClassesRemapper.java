package co.elastic.apm.android.instrumentation.generic;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.pool.TypePool;

public class LocalClassesRemapper extends AsmVisitorWrapper.AbstractBase {

    @Override
    public ClassVisitor wrap(TypeDescription typeDescription,
                             ClassVisitor classVisitor,
                             Implementation.Context context,
                             TypePool typePool,
                             FieldList<FieldDescription.InDefinedShape> fields,
                             MethodList<?> methods,
                             int writerFlags,
                             int readerFlags) {
        return new CoroutineBuilderRemapper(classVisitor);
    }
}
