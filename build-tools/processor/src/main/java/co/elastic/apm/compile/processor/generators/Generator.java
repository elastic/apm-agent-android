package co.elastic.apm.compile.processor.generators;

import com.squareup.javapoet.JavaFile;

import java.util.List;

import javax.lang.model.element.TypeElement;

public interface Generator {

    Result generate(List<TypeElement> from);

    class Result {
        public final String typeName;
        public final JavaFile javaFile;
        public final TypeElement[] from;

        public Result(String typeName, JavaFile javaFile, TypeElement[] from) {
            this.typeName = typeName;
            this.javaFile = javaFile;
            this.from = from;
        }
    }
}
