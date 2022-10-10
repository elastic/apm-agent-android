package co.elastic.apm.compile.tools.data;

import java.util.Objects;

public class License {
    public final String id;
    public final String name;

    public License(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(id, license.id) && Objects.equals(name, license.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
