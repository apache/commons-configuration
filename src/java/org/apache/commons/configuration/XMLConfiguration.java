/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Reads a XML configuration file.
 *
 * To retrieve the value of an attribute of an element, use
 * <code>X.Y.Z[@attribute]</code>. The '@' symbol was chosen for consistency
 * with XPath.
 *
 * Setting property values will <b>NOT </b> automatically persist changes to
 * disk, unless <code>autoSave=true</code>.
 *
 * @since commons-configuration 1.0
 *
 * @author Jörg Schaible
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan </a>
 * @author <a href="mailto:dlr@apache.org">Daniel Rall </a>
 * @author Emmanuel Bourg
 * @version $Revision: 1.16 $, $Date: 2004/09/26 16:29:24 $
 */
public class XMLConfiguration extends AbstractFileConfiguration
{
    // For conformance with xpath
    private static final String ATTRIBUTE_START = "[@";

    private static final String ATTRIBUTE_END = "]";

    /**
     * For consistency with properties files. Access nodes via an "A.B.C"
     * notation.
     */
    private static final String NODE_DELIMITER = ".";

    /**
     * The XML document from our data source.
     */
    private Document document;

    /**
     * If true, modifications are immediately persisted.
     */
    private boolean autoSave = false;

    /**
     * Creates an empty XML configuration.
     */
    public XMLConfiguration()
    {
        // build an empty document.
        DocumentBuilder builder = null;
        try
        {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new ConfigurationRuntimeException(e.getMessage(), e);
        }

        document = builder.newDocument();
        document.appendChild(document.createElement("configuration"));
    }

    /**
     * Creates and loads the XML configuration from the specified resource.
     *
     * @param resource The name of the resource to load.
     *
     * @throws ConfigurationException Error while loading the XML file
     */
    public XMLConfiguration(String resource) throws ConfigurationException
    {
        this.fileName = resource;
        url = ConfigurationUtils.locate(resource);
        load();
    }

    /**
     * Creates and loads the XML configuration from the specified file.
     *
     * @param file The XML file to load.
     * @throws ConfigurationException Error while loading the XML file
     */
    public XMLConfiguration(File file) throws ConfigurationException
    {
        setFile(file);
        load();
    }

    /**
     * Creates and loads the XML configuration from the specified URL.
     *
     * @param url The location of the XML file to load.
     * @throws ConfigurationException Error while loading the XML file
     */
    public XMLConfiguration(URL url) throws ConfigurationException
    {
        setURL(url);
        load();
    }

    /**
     * {@inheritDoc}
     */
    public void load(Reader in) throws ConfigurationException
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new InputSource(in));
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }

        initProperties(document.getDocumentElement(), new StringBuffer());
    }

    /**
     * Loads and initializes from the XML file.
     *
     * @param element
     *            The element to start processing from. Callers should supply
     *            the root element of the document.
     * @param hierarchy
     */
    private void initProperties(Element element, StringBuffer hierarchy)
    {
        StringBuffer buffer = new StringBuffer();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;

                StringBuffer subhierarchy = new StringBuffer(hierarchy.toString());
                subhierarchy.append(child.getTagName());
                processAttributes(subhierarchy.toString(), child);
                initProperties(child, subhierarchy.append(NODE_DELIMITER));
            }
            else if (node instanceof CDATASection || node instanceof Text)
            {
                CharacterData data = (CharacterData) node;
                buffer.append(data.getData());
            }
        }

        String text = buffer.toString().trim();
        if (text.length() > 0 && hierarchy.length() > 0)
        {
            super.addProperty(hierarchy.substring(0, hierarchy.length() - 1), text);
        }
    }

    /**
     * Helper method for constructing properties for the attributes of the given
     * XML element.
     *
     * @param hierarchy
     *            the actual hierarchy
     * @param element
     *            the actual XML element
     */
    private void processAttributes(String hierarchy, Element element)
    {
        // Add attributes as x.y{ATTRIBUTE_START}att{ATTRIBUTE_END}
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Attr attr = (Attr) attributes.item(i);
            String attrName = hierarchy + ATTRIBUTE_START + attr.getName() + ATTRIBUTE_END;
            super.addProperty(attrName, attr.getValue());
        }
    }

    /**
     * Calls super method, and also ensures the underlying {@linkDocument} is
     * modified so changes are persisted when saved.
     *
     * @param name
     * @param value
     */
    public void addProperty(String name, Object value)
    {
        super.addProperty(name, value);
        addXmlProperty(name, value);
        possiblySave();
    }

    public Object getXmlProperty(String name)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        // get all the matching elements
        List children = findElementsForPropertyNodes(nodes);

        List properties = new ArrayList();
        if (attName == null)
        {
            // return text contents of elements
            Iterator cIter = children.iterator();
            while (cIter.hasNext())
            {
                Element child = (Element) cIter.next();
                // add non-empty strings
                String text = getChildText(child);
                if (StringUtils.isNotEmpty(text))
                {
                    properties.add(text);
                }
            }
        }
        else
        {
            // return text contents of attributes
            Iterator cIter = children.iterator();
            while (cIter.hasNext())
            {
                Element child = (Element) cIter.next();
                if (child.hasAttribute(attName))
                {
                    properties.add(child.getAttribute(attName));
                }
            }
        }

        switch (properties.size())
        {
            case 0:
                return null;
            case 1:
                return properties.get(0);
            default:
                return properties;
        }
    }

    /**
     * TODO Add comment.
     *
     * @param nodes
     * @return
     */
    private List findElementsForPropertyNodes(String[] nodes)
    {
        List children = new ArrayList();
        List elements = new ArrayList();

        children.add(document.getDocumentElement());
        for (int i = 0; i < nodes.length; i++)
        {
            elements.clear();
            elements.addAll(children);
            children.clear();

            String eName = nodes[i];
            Iterator eIter = elements.iterator();
            while (eIter.hasNext())
            {
                Element element = (Element) eIter.next();
                NodeList list = element.getChildNodes();
                for (int j = 0; j < list.getLength(); j++)
                {
                    Node node = list.item(j);
                    if (node instanceof Element)
                    {
                        Element child = (Element) node;
                        if (eName.equals(child.getTagName()))
                        {
                            children.add(child);
                        }
                    }
                }
            }
        }

        return children;
    }

    private static String getChildText(Node node)
    {
        // is there anything to do?
        if (node == null)
        {
            return null;
        }

        // concatenate children text
        StringBuffer str = new StringBuffer();
        Node child = node.getFirstChild();
        while (child != null)
        {
            short type = child.getNodeType();
            if (type == Node.TEXT_NODE)
            {
                str.append(child.getNodeValue());
            }
            else if (type == Node.CDATA_SECTION_NODE)
            {
                str.append(child.getNodeValue());
            }
            child = child.getNextSibling();
        }

        // return text value
        return StringUtils.trimToNull(str.toString());

    } // getChildText(Node):String

    /**
     * Calls super method, and also ensures the underlying {@linkDocument} is
     * modified so changes are persisted when saved.
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value)
    {
        super.setProperty(name, value);
        setXmlProperty(name, value);
        possiblySave();
    }

    /**
     * Sets the property value in our document tree, auto-saving if appropriate.
     *
     * @param name
     *            The name of the element to set a value for.
     * @param value
     *            The value to set.
     */
    private void setXmlProperty(String name, Object value)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        Element element = document.getDocumentElement();
        for (int i = 0; i < nodes.length; i++)
        {
            String eName = nodes[i];
            Element child = getChildElementWithName(eName, element);
            // If we don't find this part of the property in the XML hierarchy
            // we add it as a new node
            if (child == null)
            {
                child = document.createElement(eName);
                element.appendChild(child);
            }
            element = child;
        }

        if (attName == null)
        {
            CharacterData data = document.createTextNode(String.valueOf(value));
            element.appendChild(data);
        }
        else
        {
            element.setAttribute(attName, String.valueOf(value));
        }
    }

    private Element getChildElementWithName(String eName, Element element)
    {
        Element child = null;

        NodeList list = element.getChildNodes();
        for (int j = 0; j < list.getLength(); j++)
        {
            Node node = list.item(j);
            if (node instanceof Element)
            {
                child = (Element) node;
                if (eName.equals(child.getTagName()))
                {
                    break;
                }
                child = null;
            }
        }
        return child;
    }

    /**
     * Adds the property value in our document tree, auto-saving if appropriate.
     *
     * @param name
     *            The name of the element to set a value for.
     * @param value
     *            The value to set.
     */
    private void addXmlProperty(String name, Object value)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        Element element = document.getDocumentElement();
        Element parent = element;

        for (int i = 0; i < nodes.length; i++)
        {
            if (element == null)
                break;
            parent = element;
            String eName = nodes[i];
            Element child = getChildElementWithName(eName, element);

            element = child;
        }

        Element child = document.createElement(nodes[nodes.length - 1]);
        parent.appendChild(child);
        if (attName == null)
        {
            CharacterData data = document.createTextNode(String.valueOf(value));
            child.appendChild(data);
        }
        else
        {
            child.setAttribute(attName, String.valueOf(value));
        }
    }

    /**
     * Calls super method, and also ensures the underlying {@link Document}is
     * modified so changes are persisted when saved.
     *
     * @param name
     *            The name of the property to clear.
     */
    public void clearProperty(String name)
    {
        super.clearProperty(name);
        clearXmlProperty(name);
        possiblySave();
    }

    private void clearXmlProperty(String name)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        // get all the matching elements
        List children = findElementsForPropertyNodes(nodes);

        if (attName == null)
        {
            // remove children with no subelements
            Iterator cIter = children.iterator();
            while (cIter.hasNext())
            {
                Element child = (Element) cIter.next();

                // determine if child has subelments
                boolean hasSubelements = false;
                Node subchild = child.getFirstChild();
                while (subchild != null)
                {
                    if (subchild.getNodeType() == Node.ELEMENT_NODE)
                    {
                        hasSubelements = true;
                        break;
                    }
                    subchild = subchild.getNextSibling();
                }

                if (!hasSubelements)
                {
                    // safe to remove
                    if (!child.hasAttributes())
                    {
                        // remove entire node
                        Node parent = child.getParentNode();
                        parent.removeChild(child);
                    }
                    else
                    {
                        // only remove node contents
                        subchild = child.getLastChild();
                        while (subchild != null)
                        {
                            child.removeChild(subchild);
                            subchild = child.getLastChild();
                        }
                    }
                }
            }
        }
        else
        {
            // remove attributes from children
            Iterator cIter = children.iterator();
            while (cIter.hasNext())
            {
                Element child = (Element) cIter.next();
                child.removeAttribute(attName);
            }
        }
    }

    /**
     * Save the configuration if the automatic persistence is enabled and a file
     * is specified.
     */
    private void possiblySave()
    {
        if (autoSave && fileName != null)
        {
            try
            {
                save();
            }
            catch (ConfigurationException ce)
            {
                throw new ConfigurationRuntimeException("Failed to auto-save", ce);
            }
        }
    }

    /**
     * If true, changes are automatically persisted.
     *
     * @param autoSave
     */
    public void setAutoSave(boolean autoSave)
    {
        this.autoSave = autoSave;
    }

    /**
     * {@inheritDoc}
     */
    public void save(Writer writer) throws ConfigurationException
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource(document);
            Result result = new StreamResult(writer);

            transformer.setOutputProperty("indent", "yes");
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    public String toString()
    {
        StringWriter writer = new StringWriter();
        try
        {
            save(writer);
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * Parse a property key and return an array of the element hierarchy it
     * specifies. For example the key "x.y.z[@abc]" will result in [x, y, z].
     *
     * @param key
     *            the key to parse
     *
     * @return the elements in the key
     */
    protected static String[] parseElementNames(String key)
    {
        if (key == null)
        {
            return new String[]{};
        }
        else
        {
            // find the beginning of the attribute name
            int attStart = key.indexOf(ATTRIBUTE_START);

            if (attStart > -1)
            {
                // remove the attribute part of the key
                key = key.substring(0, attStart);
            }

            return StringUtils.split(key, NODE_DELIMITER);
        }
    }

    /**
     * Parse a property key and return the attribute name if it existst.
     *
     * @param key
     *            the key to parse
     *
     * @return the attribute name, or null if the key doesn't contain one
     */
    protected static String parseAttributeName(String key)
    {
        String name = null;

        if (key != null)
        {
            // find the beginning of the attribute name
            int attStart = key.indexOf(ATTRIBUTE_START);

            if (attStart > -1)
            {
                // find the end of the attribute name
                int attEnd = key.indexOf(ATTRIBUTE_END);
                attEnd = attEnd > -1 ? attEnd : key.length();

                name = key.substring(attStart + ATTRIBUTE_START.length(), attEnd);
            }
        }

        return name;
    }
}
