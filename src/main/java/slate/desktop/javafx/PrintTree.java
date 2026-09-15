package slate.desktop.javafx;

import slate.core.ComponentTreeNode;
import slate.core.renderer.Renderer;
import slate.core.renderer.RendererProvider;

public class PrintTree implements Renderer {

    @Override
    public void render(ComponentTreeNode root) {
        System.out.println("========== RENDERER ==========");
        printTree(root, 0);
        System.out.println("==============================");
    }

    private void printTree(ComponentTreeNode node, int depth) {

//        String indent = "  ".repeat(depth);
//
//        if (node.isText()) {
//            System.out.println(indent + "\"" + node.getText());
//            return;
//        }
//
//        if (!node.isComponent()){
//            System.out.println(indent + "<" + node.getType() + ">");
//        }


//        System.out.println(node.getType());
//        System.out.println(node.getChildren().size());
//        System.out.println(node.getChildren());

//        ComponentTreeNode node1 = node.getChildren().get(0);

//        System.out.println(node1.getType());
//        System.out.println(node1.getChildren().size());
//        System.out.println(node1.getChildren());
//        System.out.println(node1.getKind());

//        ComponentTreeNode node2 = node1.getChildren().get(0);
//
//        System.out.println(node2);
//        System.out.println(node2.getType());
//        System.out.println(node2.getChildren().size());
//        System.out.println(node2.getChildren());
//        System.out.println(node2.getKind());
//
//        ComponentTreeNode node3 = node2.getChildren().get(0);
//        System.out.println(node3);


//        for (ComponentTreeNode child : node.getChildren()) {
//            printTree(child, depth + 1);
//        }

        String indent = "  ".repeat(depth);

        if (node.isText()) {

            System.out.println(indent + "TEXT: \"" + node.getText() + "\"");

            return;
        }

        if (node.isComponent()) {

            System.out.println(indent + "COMPONENT: <" + node.getType() + ">");

            ComponentTreeNode instanceRoot = node.getChildren().size() == 1 ? node.getChildren().get(0) : null;

            if (instanceRoot != null) {
                printTree(instanceRoot, depth + 1);
            }

            return;
        }

        System.out.println(indent + "ELEMENT: <" + node.getType() + ">");

        for (ComponentTreeNode child : node.getChildren()) {
            printTree(child, depth + 1);
        }
    }

    public static class Provider implements RendererProvider {

        @Override
        public Renderer create() {
            return new PrintTree();
        }
    }
}