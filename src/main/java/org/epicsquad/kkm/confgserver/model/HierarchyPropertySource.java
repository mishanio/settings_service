package org.epicsquad.kkm.confgserver.model;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.*;
import java.util.stream.Collectors;

public class HierarchyPropertySource extends PropertiesPropertySource {
    private final List<HierarchyPropertySource> imports;

    public HierarchyPropertySource(String name, Properties source, List<HierarchyPropertySource> imports) {
        super(name, source);
        this.imports = Optional.ofNullable(imports).orElse(Collections.emptyList());
    }

    public CompositePropertySource toCompositePropertySource() {
        CompositePropertySource compositePropertySource = new CompositePropertySource(name);
        compositePropertySource.addPropertySource(this);
        addImportHierarchy(this, compositePropertySource);
        return compositePropertySource;
    }

    private void addImportHierarchy(HierarchyPropertySource source,
                                    CompositePropertySource compositePropertySource) {

        for (HierarchyPropertySource sourceImport : source.getImports()) {
            compositePropertySource.addPropertySource(sourceImport);
        }
    }

    public static class Builder {
        private String name;
        private Properties source;
        private List<Builder> imports;

        public HierarchyPropertySource build() {
            List<HierarchyPropertySource> importSources =
                    imports.stream().map(Builder::build).collect(Collectors.toList());
            return new HierarchyPropertySource(name, source, importSources);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSource(Properties source) {
            this.source = source;
            return this;
        }

        public Builder setImports(List<Builder> imports) {
            this.imports = imports;
            return this;
        }

    }

    public List<HierarchyPropertySource> getImports() {
        return imports;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HierarchyPropertySource.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("source=" + source)
                .add("imports=" + imports)
                .toString();
    }

}
