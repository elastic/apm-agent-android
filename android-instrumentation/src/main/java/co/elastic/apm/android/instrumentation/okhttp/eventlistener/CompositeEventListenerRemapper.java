package co.elastic.apm.android.instrumentation.okhttp.eventlistener;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.commons.ClassRemapper;
import net.bytebuddy.jar.asm.commons.Remapper;
import net.bytebuddy.pool.TypePool;

import co.elastic.apm.android.common.okhttp.eventlistener.CompositeEventListener;

public class CompositeEventListenerRemapper extends AsmVisitorWrapper.AbstractBase {

    @Override
    public ClassVisitor wrap(TypeDescription typeDescription,
                             ClassVisitor classVisitor,
                             Implementation.Context context,
                             TypePool typePool,
                             FieldList<FieldDescription.InDefinedShape> fields,
                             MethodList<?> methods,
                             int writerFlags,
                             int readerFlags) {
        return new ClassRemapper(classVisitor, new GeneratedRemapper());
    }

    static class GeneratedRemapper extends Remapper {
        private final String from;
        private final String to;

        GeneratedRemapper() {
            from = CompositeEventListener.class.getName().replaceAll("\\.", "/");
            to = CompositeEventListener.getGeneratedName().replaceAll("\\.", "/");
        }

        @Override
        public String map(String internalName) {
            if (internalName.equals(from)) {
                return to;
            }
            return super.map(internalName);
        }
    }
}
