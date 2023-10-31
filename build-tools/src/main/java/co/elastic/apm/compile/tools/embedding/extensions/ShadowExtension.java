package co.elastic.apm.compile.tools.embedding.extensions;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ShadowExtension {
    private final ObjectFactory objectFactory;

    private final List<Relocation> relocations = new ArrayList<>();

    @Inject
    public ShadowExtension(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public List<Relocation> getRelocations() {
        return relocations;
    }

    public void relocate(String pattern, String destination) {
        Relocation relocation = objectFactory.newInstance(Relocation.class);
        relocation.getPattern().set(pattern);
        relocation.getDestination().set(destination);
        relocations.add(relocation);
    }

    public interface Relocation {
        Property<String> getPattern();

        Property<String> getDestination();
    }
}
