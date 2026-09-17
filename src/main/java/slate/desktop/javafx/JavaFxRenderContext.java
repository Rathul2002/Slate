package slate.desktop.javafx;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import slate.core.ComponentInstance;
import slate.core.ComponentTreeNode;

/**
 * Provides the services required by individual JavaFX element renderers.
 *
 * <p>The context dispatches runtime nodes and maintains the component
 * ownership boundary while recursively rendering a component subtree.</p>
 *
 * <p>A component instance owns the behavior associated with its entire
 * internal subtree. Therefore, an element such as Button must be able to
 * discover the ComponentInstance that owns it even when the Button is
 * several levels below the component root.</p>
 */
public final class JavaFxRenderContext {

    private final JavaFxElementRegistry elementRegistry;
    private final JavaFxComponentMountManager componentMountManager;

    /**
     * Tracks the component whose subtree is currently being rendered.
     *
     * <p>This is intentionally kept inside the backend render context. It is
     * not part of ComponentTreeNode or slate.core.</p>
     */
    private ComponentInstance currentComponentOwner;

    public JavaFxRenderContext(JavaFxElementRegistry elementRegistry) {
        this(elementRegistry, new JavaFxComponentMountManager());
    }

    public JavaFxRenderContext(JavaFxElementRegistry elementRegistry, JavaFxComponentMountManager componentMountManager) {
        if (elementRegistry == null) {
            throw new IllegalArgumentException("Element registry cannot be null");
        }

        if (componentMountManager == null) {
            throw new IllegalArgumentException("Component mount manager cannot be null");
        }

        this.elementRegistry = elementRegistry;
        this.componentMountManager = componentMountManager;
    }

    public JavaFxElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    public JavaFxComponentMountManager getComponentMountManager() {
        return componentMountManager;
    }

    /**
     * Renders one runtime node using the currently active component owner.
     *
     * <p>This is the method used by element renderers such as View when they
     * recursively render their children.</p>
     */
    public Node render(ComponentTreeNode node) {
        return render(node, currentComponentOwner);
    }

    /**
     * Renders a runtime node with an explicitly supplied component owner.
     *
     * <p>The explicit owner is primarily used when crossing a component
     * boundary.</p>
     */
    public Node render(ComponentTreeNode node, ComponentInstance ownerComponent) {
        if (node == null) {
            throw new IllegalArgumentException("Render node cannot be null");
        }

        /*
         * A component boundary creates a new runtime ownership scope.
         */
        if (node.isComponent()) {
            return renderComponent(node);
        }

        /*
         * Raw XML text nodes are represented separately from Slate's
         * built-in <Text> element.
         */
        if (node.isText()) {
            return new Label(node.getText());
        }

        /*
         * Actual Slate ELEMENT nodes are delegated to the modular registry.
         */
        Node nativeNode = elementRegistry.render(node, this);

        /*
         * Events are bound after the native element has been created.
         *
         * The owner is inherited from the component currently being rendered.
         */
        bindEvents(node, nativeNode, ownerComponent);

        return nativeNode;
    }

    /**
     * Creates the native subtree belonging to one component instance.
     *
     * <p>The component boundary itself does not create an additional JavaFX
     * node. Its resolved internal root becomes the native root owned by the
     * component mount.</p>
     *
     * <p>The resulting mount initially represents an unattached native
     * subtree. JavaFxComponentMount observes the JavaFX scene graph and
     * changes its mounted state when that root receives a parent.</p>
     */
    private Node renderComponent(ComponentTreeNode node) {

        if (node.getChildren().size() != 1) {
            throw new IllegalStateException("Component node must contain exactly one resolved root: " + node.getType());
        }

        ComponentInstance componentInstance = node.getComponentInstance();

        if (componentInstance == null) {
            throw new IllegalStateException("Component node does not contain a runtime component instance: " + node.getType());
        }

        if (componentMountManager.find(componentInstance) != null) {
            throw new IllegalStateException("Component instance is already mounted: " + componentInstance.getName());
        }

        ComponentInstance previousOwner = currentComponentOwner;

        /*
         * Everything rendered below this point belongs to this component
         * unless another nested component establishes a new boundary.
         */
        currentComponentOwner = componentInstance;

        try {
            Node nativeRoot = render(node.getChildren().get(0));

            componentMountManager.createMount(componentInstance, nativeRoot);

            return nativeRoot;

        } finally {
            /*
             * Restore the previous owner so nested components do not leak
             * their ownership into their parent component's remaining nodes.
             */
            currentComponentOwner = previousOwner;
        }
    }

    /**
     * Installs the initial native event bridge.
     *
     * <p>The first event supported by Slate is Button.onClick.</p>
     */
    private void bindEvents(ComponentTreeNode node, Node nativeNode, ComponentInstance ownerComponent) {
        if (ownerComponent == null) {
            return;
        }

        Object onClick = node.getProps().get("onClick");

        if (onClick == null) {
            return;
        }

        if (!(nativeNode instanceof Button button)) {
            throw new IllegalStateException("The onClick event is currently supported only on <Button>: " + node.getType());
        }

        if (!(onClick instanceof String handlerName) || handlerName.isBlank()) {
            throw new IllegalStateException("Button onClick must contain a non-empty handler method name");
        }

        JavaFxEventSupport.bindButtonClick(button, ownerComponent, handlerName);
    }
}