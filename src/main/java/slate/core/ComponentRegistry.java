package slate.core;

import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {

    private final Map<String, ComponentDefinition> components;

    public ComponentRegistry() {
        this.components = new HashMap<>();
    }

    public void register(ComponentDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException("Component definition cannot be null");
        }

        String name = definition.getName();

        if (components.containsKey(name)) {
            throw new IllegalStateException("Component already registered: " + name);
        }

        components.put(name, definition);
    }

    public ComponentDefinition find(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }

        return components.get(name);
    }
}