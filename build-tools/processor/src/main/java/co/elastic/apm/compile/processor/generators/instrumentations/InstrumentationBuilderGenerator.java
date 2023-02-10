package co.elastic.apm.compile.processor.generators.instrumentations;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class InstrumentationBuilderGenerator extends BaseInstrumentationsGenerator {

    @Override
    public Result generate(List<TypeElement> from) {
        ClassName instrumentationConfigName = ClassName.get("co.elastic.apm.android.sdk.instrumentation", "InstrumentationConfiguration");
        ClassName builderName = ClassName.get(instrumentationConfigName.packageName(), instrumentationConfigName.simpleName() + "Builder");
        List<FieldInfo> fieldInfos = new ArrayList<>();
        from.forEach(element -> {
            String fieldName = "enable" + getInstrumentationName(element);
            FieldSpec field = FieldSpec.builder(TypeName.BOOLEAN, fieldName, Modifier.PRIVATE).build();
            MethodSpec setterMethod = createSetterMethod(field, builderName);
            fieldInfos.add(new FieldInfo(field, setterMethod, element));
        });

        JavaFile javaFile = createType(instrumentationConfigName, builderName, fieldInfos);

        return new Result(builderName.canonicalName(), javaFile, from.toArray(new TypeElement[0]));
    }

    private MethodSpec createSetterMethod(FieldSpec field, ClassName builderName) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(field.type, "enable")
                .returns(builderName)
                .addStatement("this.$N = enable", field)
                .addStatement("return this")
                .build();
    }

    private JavaFile createType(ClassName instrumentationConfigName, ClassName builderName, List<FieldInfo> fields) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(builderName);
        TypeSpec.classBuilder(builderName);

        fields.forEach(fieldInfo -> {
            classBuilder.addField(fieldInfo.field);
            classBuilder.addMethod(fieldInfo.method);
        });

        classBuilder.addMethod(createBuildMethod(instrumentationConfigName, fields));

        TypeSpec typeSpec = classBuilder.build();

        return JavaFile.builder(builderName.packageName(), typeSpec).build();
    }

    private MethodSpec createBuildMethod(ClassName instrumentationConfigName, List<FieldInfo> fields) {
        ClassName instrumentationTypeName = ClassName.get(instrumentationConfigName.packageName(), "Instrumentation");
        String instrumentationsListName = "instrumentations";
        MethodSpec.Builder buildMethodBuilder = MethodSpec.methodBuilder("build").returns(instrumentationConfigName)
                .addStatement("$T<$T> $L = new $T<>()", List.class, instrumentationTypeName, instrumentationsListName, ArrayList.class);

        fields.forEach(fieldInfo -> buildMethodBuilder.addStatement("$N.add(new $T($N))", instrumentationsListName, fieldInfo.element, fieldInfo.field));

        return buildMethodBuilder.addStatement("return new $T(true, $N)", instrumentationConfigName, instrumentationsListName).build();
    }

    private static class FieldInfo {
        public final FieldSpec field;
        public final MethodSpec method;
        public final TypeElement element;

        private FieldInfo(FieldSpec field, MethodSpec method, TypeElement element) {
            this.field = field;
            this.method = method;
            this.element = element;
        }
    }
}
