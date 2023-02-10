package co.elastic.apm.compile.processor;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import co.elastic.apm.compile.processor.generators.Generator;
import co.elastic.apm.compile.processor.generators.InstrumentationsGenerator;

@AutoService(Processor.class)
@SupportedAnnotationTypes("co.elastic.apm.compile.processor.annotations.AutoInstrumentation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoInstrumentationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        annotations.forEach((Consumer<TypeElement>) annotation -> {
            Set<? extends Element> annotated = roundEnvironment.getElementsAnnotatedWith(annotation);
            List<TypeElement> types = new ArrayList<>();
            annotated.forEach((Consumer<Element>) element -> types.add((TypeElement) element));

            Generator.Result instrumentations = new InstrumentationsGenerator().generate(types);

            createJavaSource(instrumentations);
        });

        return true;
    }

    private void createJavaSource(Generator.Result generated) {
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(generated.typeName, generated.from);
            Writer writer = sourceFile.openWriter();

            generated.javaFile.writeTo(writer);

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
