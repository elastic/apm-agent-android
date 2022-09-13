package co.elastic.apm.compile.tools.extensions;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class LicensesFinderExtension {
    public final RegularFileProperty manualMappingFile;

    @Inject
    public LicensesFinderExtension(ObjectFactory objects) {
        manualMappingFile = objects.fileProperty();
    }
}
