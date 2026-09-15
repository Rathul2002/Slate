package slate.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses Slate XML resources into {@link SlateNode} objects.
 *
 * <p>The parser preserves element order and meaningful text while ignoring
 * formatting-only whitespace between XML elements.</p>
 *
 * <p>External XML resources are deliberately disabled. Slate XML should
 * describe UI rather than act as a mechanism for loading arbitrary external
 * documents or entities.</p>
 */
public class XmlParser {

    public SlateNode parse(InputStream inputStream) {

        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            configureSecureFactory(factory);

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);

            Element root = document.getDocumentElement();

            if (root == null) {
                throw new IllegalStateException("XML document does not contain a root element");
            }

            return parseElement(root);

        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }

            throw new IllegalStateException("Failed to parse XML", e);
        }
    }

    private void configureSecureFactory(DocumentBuilderFactory factory) throws Exception {

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
    }

    private SlateNode parseElement(Element element) {

        Map<String, Object> props = new HashMap<>();

        var attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {

            var attribute = attributes.item(i);

            props.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        List<SlateNode> children = new ArrayList<>();

        NodeList childNodes = element.getChildNodes();

        /*
         * DOM returns children in their original document order.
         *
         * We therefore process ELEMENT_NODE and TEXT_NODE
         * in the same loop so that mixed content keeps its order.
         */
        for (int i = 0; i < childNodes.getLength(); i++) {

            Node child = childNodes.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {

                children.add(parseElement((Element) child));
            } else if (child.getNodeType() == Node.TEXT_NODE) {

                String text = child.getNodeValue();

                /*
                 * XML formatting commonly creates whitespace-only
                 * text nodes between elements:
                 * <View>
                 *     <Button />
                 * </View>
                 * We do not want indentation to become UI text.
                 */
                if (text != null && !text.isBlank()) {

                    children.add(new SlateTextNode(text.trim()));
                }
            }
        }

        return new SlateNode(
                element.getTagName(),
                props,
                children
        );
    }
}
