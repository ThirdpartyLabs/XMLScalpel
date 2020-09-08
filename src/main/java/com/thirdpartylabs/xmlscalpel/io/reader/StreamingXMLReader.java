/*
 * XMLScalpel random access XML processor
 *
 * Copyright (c) 2020- Rob Ruchte, rob@thirdpartylabs.com
 *
 * Licensed under the License specified in file LICENSE, included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thirdpartylabs.xmlscalpel.io.reader;

import com.ctc.wstx.stax.WstxInputFactory;
import com.thirdpartylabs.xmlscalpel.entity.Fragment;
import com.thirdpartylabs.xmlscalpel.entity.OuterDocument;
import com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation;
import com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import javax.management.modelmbean.XMLParseException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Streaming XML file reader that uses the {@link com.ctc.wstx.stax.WstxInputFactory Woodstox} stream reader to extract
 * top level XML nodes along with metadata describing their location in the XML file, and send them to an
 * {@link com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor XMLStreamProcessor}.
 * <p>
 * Using the streaming reader allows large files to be processed without significant overhead.
 */
public class StreamingXMLReader
{
    private List<String> targetPaths = new ArrayList<>();
    private final Stack<String> tagStack = new Stack<>();
    private String currentPath = "/";
    private int eventType = 0;
    private ByteTrackingReader byteTrackingReader;
    private XMLStreamReader reader;
    private final Transformer transformer;
    private final Map<String, String> documentElementAttributes = new HashMap<>();
    private final Map<String, String> documentElementAttributeNamespaces = new HashMap<>();
    private String documentElementTagName = null;
    private String documentElementPrefix = null;
    private String encoding = null;
    private String xmlVersion = null;
    private String characterEncodingScheme = null;

    public StreamingXMLReader() throws TransformerConfigurationException
    {
        TransformerFactory tf = TransformerFactory.newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
        transformer = tf.newTransformer();
    }

    /**
     * Read an XML file using the {@link com.ctc.wstx.stax.WstxInputFactory Woodstox} streaming API and supply the
     * {@link com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor XMLStreamProcessor} with
     * {@link com.thirdpartylabs.xmlscalpel.entity.Fragment Fragment} objects.
     * Specify a {@link java.util.List List} of node paths to extract. Example:
     * <pre>
     * {@code
     * <xml>
     * <Feed>
     *     <Category>
     *         <Name>Bolts</Name>
     *         <Product>Large</Product>
     *         <Product>Small</Product>
     *         <Services>
     *             <Service>Tightening</Service>
     *             <Service>Loosening</Service>
     *         </Services>
     *     </Category>
     *     <Category>
     *         <Name>Hammers</Name>
     *         <Product>Framing</Product>
     *         <Product>Dead Blow</Product>
     *         <Services>
     *             <Service>Banging</Service>
     *         </Services>
     *     </Category>
     * </Feed>
     * }
     * </pre>
     * <p>
     * You can extract all {@code product} and {@code service} elements in the same read operation by
     * passing in these paths:<br>
     * {@code /feed/category/Product}<br>
     * {@code /feed/category/Services/Service}
     * <p>
     * Namespace prefixes may be specified as they appear in the XML:
     * {@code /aw:PurchaseOrders/aw:PurchaseOrder/aw:Address}
     * <p>
     * Paths are absolute with respect to the document root, they will be normalized to always have a leading slash
     * and never have a trailing slash. Overlapping paths are not supported, the least specific path will be used in
     * such a case.
     * <p>
     * {@link com.thirdpartylabs.xmlscalpel.entity.Fragment Fragment} objects wrap the dom node as a
     * {@link org.w3c.dom.DocumentFragment DocumentFragment} and an
     * {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation} object that describes the
     * node's location in the XML file. This allows efficient retrieval of the nodes later using the
     * {@link com.thirdpartylabs.xmlscalpel.io.reader.RandomAccessXMLReader RandomAccessXMLReader}
     * <p>
     *
     * @param file        The XML file to process
     * @param processor   {@link com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor XMLStreamProcessor} instance
     * @param targetPaths {@link java.util.List List} of node paths to target for extraction
     *
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws TransformerException
     */
    public void readFile(File file, XMLStreamProcessor processor, List<String> targetPaths) throws FileNotFoundException, XMLStreamException, TransformerException
    {
        setTargetPaths(targetPaths);
        tagStack.clear();

        // Tell the processor how many bytes are in the file
        processor.setBytesTotal(file.length());

        int nodeCount = 0;

        initializeDocument(file);

        /*
            We have to be careful with how we traverse the document here, we can't simply call
            reader.nextTag() in the while statement. That will only work if there is whitespace between the
            target elements. We need to check the current event type after the transform, it will have rolled forward
            to START_ELEMENT if there was no whitespace.
         */
        eventType = reader.nextTag();
        while (eventType == XMLStreamConstants.START_ELEMENT)
        {
            // Keep track of our location
            pushTag(reader.getName());

            // Should we extract this element?
            if (elementIsEligibleForProcessing())
            {
                /*
                    Get the char offset from the Woodstox reader and use it to get a byte offset
                    from our underlying ByteTrackingReader

                    This will be the exact offset for the beginning of the opening tag of this node
                 */
                int startCharOffset = reader.getLocation().getCharacterOffset();
                long startByteOffset = byteTrackingReader.getByteOffsetForCharOffset(startCharOffset);

                // Use the transformer to extract the current top level node
                DOMResult result = new DOMResult();
                transformer.transform(new StAXSource(reader), result);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, characterEncodingScheme);
                Document resultDocument = (Document) result.getNode();

                // Create a DocumentFragment from the document node of the resulting document
                DocumentFragment outputFragment = resultDocument.createDocumentFragment();
                outputFragment.appendChild(resultDocument.getDocumentElement());

                /*
                    Get the char offset from the Woodstox reader and use it to get a byte offset
                    from our underlying ByteTrackingReader

                    This will be the exact offset for the end of the closing tag of this node
                 */
                int endCharOffset = reader.getLocation().getCharacterOffset();
                long endByteOffset = byteTrackingReader.getByteOffsetForCharOffset(endCharOffset);

                // Calculate the number of bytes in our element
                int byteLength = (int) (endByteOffset - startByteOffset);

                // Instantiate an XMLByteLocation with the current index and byte  offsets
                XMLByteLocation xmlByteLocation = new XMLByteLocation(nodeCount, startByteOffset, byteLength);

                // Wrap the DocumentFragment and XMLByteLocation in a Fragment object
                Fragment fragment = new Fragment(outputFragment, xmlByteLocation);

                // Send the Fragment object to the XMLStreamProcessor
                processor.process(fragment);

                // Bump the index
                nodeCount++;

                // Get the current event type, only move forward if necessary
                eventType = reader.getEventType();

                // Pop our current tag off the stack
                popTag();

                // If we are not rolling into a sibling, we need to do some housekeeping
                if (eventType != XMLStreamConstants.START_ELEMENT)
                {
                    advanceToNextStartElement();
                }
            }
            else
            {
                advanceToNextStartElement();
            }
        }
    }

    /**
     * Skip to the next readable element, keeping state as needed
     * @throws XMLStreamException
     */
    private void advanceToNextStartElement() throws XMLStreamException
    {
        // If we're at the end of the current set of target nodes, pop the outer element off of the stack
        if (eventType == XMLStreamConstants.END_ELEMENT)
        {
            popTag();
        }

        do
        {
            // Read events until we find something useful
            eventType = reader.next();

            // If we are closing an element, pop it off of the stack
            if (eventType == XMLStreamConstants.END_ELEMENT)
            {
                popTag();
            }
            // Keep rolling until we either hit another start element or the end of the document
        } while (eventType != XMLStreamConstants.START_ELEMENT
                 && eventType != XMLStreamConstants.END_DOCUMENT);
    }

    /**
     * Read an XML file using the {@link com.ctc.wstx.stax.WstxInputFactory Woodstox} streaming API and supply the
     * {@link com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor XMLStreamProcessor} with
     * {@link com.thirdpartylabs.xmlscalpel.entity.Fragment Fragment} objects.
     * <p>
     * All (and only) top level elements are returned. For example, given an XML file with a structure like
     * <pre>
     * {@code
     * <feed>
     *  <product></product>
     *  <product></product>
     *  <product></product>
     * </feed>
     * }
     * </pre>
     * <p>
     * All {@code product} nodes will be returned.
     * <p>
     * {@link com.thirdpartylabs.xmlscalpel.entity.Fragment Fragment} objects wrap the dom node as a
     * {@link org.w3c.dom.DocumentFragment DocumentFragment} and an
     * {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation} object that describes the
     * node's location in the XML file. This allows efficient retrieval of the nodes later using the
     * {@link com.thirdpartylabs.xmlscalpel.io.reader.RandomAccessXMLReader RandomAccessXMLReader}
     * <p>
     *
     * @param file      The XML file to process
     * @param processor {@link com.thirdpartylabs.xmlscalpel.processor.XMLStreamProcessor XMLStreamProcessor} instance
     *
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws TransformerException
     */
    public void readFile(File file, XMLStreamProcessor processor) throws FileNotFoundException, XMLStreamException, TransformerException
    {
        readFile(file, processor, null);
    }

    /**
     * A map containing the attribute name-value pairs from the document element
     *
     * @return {@link java.util.Map Map}&lt;{@link java.lang.String}, {@link java.lang.String}&gt;
     */
    public Map<String, String> getDocumentElementAttributes()
    {
        return documentElementAttributes;
    }

    /**
     * A map containing the namespace prefix to URI pairs from the document element
     *
     * @return {@link java.util.Map Map}&lt;{@link java.lang.String}, {@link java.lang.String}&gt;
     */
    public Map<String, String> getDocumentElementAttributeNamespaces()
    {
        return documentElementAttributeNamespaces;
    }

    /**
     * The local name of the document element tag
     *
     * @return The local name of the document element tag
     */
    public String getDocumentElementTagName()
    {
        if (documentElementTagName.isEmpty())
        {
            return null;
        }

        return documentElementTagName;
    }

    /**
     * Returns the prefix of the current event or null if the event does not have a prefix
     *
     * @return the prefix or null
     */
    public String getPrefix()
    {
        if (documentElementPrefix.isEmpty())
        {
            return null;
        }

        return documentElementPrefix;
    }

    /**
     * Returns the character encoding declared on the xml declaration
     * Returns null if none was declared
     *
     * @return the encoding declared in the document or null
     * @see javax.xml.stream.XMLStreamReader
     */
    public String getCharacterEncodingScheme()
    {
        if (characterEncodingScheme.isEmpty())
        {
            return null;
        }

        return characterEncodingScheme;
    }

    /**
     * Return input encoding if known or null if unknown.
     *
     * @return the encoding of this instance or null
     * @see javax.xml.stream.XMLStreamReader
     */
    public String getEncoding()
    {
        if (encoding.isEmpty())
        {
            return null;
        }

        return encoding;
    }

    /**
     * Get the xml version declared on the xml declaration
     * Returns null if none was declared
     *
     * @return the XML version or null
     * @see javax.xml.stream.XMLStreamReader
     */
    public String getVersion()
    {
        if (xmlVersion.isEmpty())
        {
            return null;
        }

        return xmlVersion;
    }

    /**
     * Open an XML file, extract relevant metadata, sn prepare for reading
     *
     * @param file The XML file to initialize
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    private void initializeDocument(File file) throws FileNotFoundException, XMLStreamException
    {
        // We need to use a Reader so we can get char offsets
        FileReader fileReader = new FileReader(file);

        // Our ByteTrackingReader will map byte offsets to char offsets
        byteTrackingReader = new ByteTrackingReader(fileReader);

        /*
            We use woodstox for speed and char offset accuracy.

            We don't need to explicitly invoke WstxInputFactory here, XMLInputFactory will give
            us the same result as long as we have woodstox on the classpath, but we're making the compiler
            enforce it because the default StAX implementation won't work for our purposes.

            The char offsets returned by most XMLStreamReader implementations are inaccurate wns will not allow us to
            pull out specific elements by byte offset later.
         */
        XMLInputFactory xif = WstxInputFactory.newInstance();
        reader = xif.createXMLStreamReader(byteTrackingReader);

        // Advance to the document element
        reader.nextTag();

        // Keep track of our location
        pushTag(reader.getName());

        // Extract XML metadata
        encoding = reader.getEncoding();
        xmlVersion = reader.getVersion();
        if (xmlVersion == null)
        {
            xmlVersion = "1.0";
        }

        characterEncodingScheme = reader.getCharacterEncodingScheme();
        if (characterEncodingScheme == null)
        {
            characterEncodingScheme = StandardCharsets.UTF_8.toString();
        }

        /*
            Extract namespace attributes, and standard attributes from the document node
         */
        documentElementTagName = reader.getLocalName();
        documentElementPrefix = reader.getPrefix();

        documentElementAttributeNamespaces.clear();
        for (int i = 0; i < reader.getNamespaceCount(); i++)
        {
            documentElementAttributeNamespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }

        documentElementAttributes.clear();
        for (int i = 0; i < reader.getAttributeCount(); i++)
        {
            String currAttributeName = reader.getAttributeName(i).getLocalPart();
            String currAttributeValue = reader.getAttributeValue(i);

            documentElementAttributes.put(currAttributeName, currAttributeValue);
        }
    }

    /**
     * @param file XML file to extract an empty document for
     * @return {@link org.w3c.dom.Document Document} containing only the document element from the file provided
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws ParserConfigurationException
     * @throws XMLParseException
     */
    public Document getEmptyDocument(File file) throws FileNotFoundException, XMLStreamException, ParserConfigurationException, XMLParseException
    {
        initializeDocument(file);

        return getEmptyDocument();
    }

    /**
     * @return {@link org.w3c.dom.Document Document} containing only the document element from the last file provided to this instance of
     * StreamingXMLReader
     * @throws ParserConfigurationException
     * @throws XMLParseException
     */
    public Document getEmptyDocument() throws ParserConfigurationException, XMLParseException
    {
        if (documentElementTagName == null)
        {
            return null;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document output = builder.newDocument();
        output.setXmlVersion(xmlVersion);
        output.setXmlStandalone(true);

        Element documentElement;
        if (documentElementPrefix.isEmpty())
        {
            documentElement = output.createElement(documentElementTagName);
            output.appendChild(documentElement);
        }
        else
        {
            String nsURI = documentElementAttributeNamespaces.get(documentElementPrefix);

            if (nsURI == null)
            {
                throw new XMLParseException("Document element prefix not found in namespace attributes");
            }

            documentElement = output.createElementNS(nsURI, documentElementPrefix + ":" + documentElementTagName);
            output.appendChild(documentElement);
        }

        for (Map.Entry<String, String> currNamespace : documentElementAttributeNamespaces.entrySet())
        {
            String currNsPrefix = currNamespace.getKey();
            String currURI = currNamespace.getValue();
            String currAttrName = (currNsPrefix.isEmpty()) ? XMLConstants.XMLNS_ATTRIBUTE : XMLConstants.XMLNS_ATTRIBUTE + ":" + currNsPrefix;

            output.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, currAttrName, currURI);
        }

        for (Map.Entry<String, String> currNamespace : documentElementAttributes.entrySet())
        {
            String currAttrName = currNamespace.getKey();
            String currValue = currNamespace.getValue();

            output.getDocumentElement().setAttribute(currAttrName, currValue);
        }

        return output;
    }

    /**
     * @param file XML file to parse into an {@link com.thirdpartylabs.xmlscalpel.entity.OuterDocument OuterDocument}
     * @return  {@link com.thirdpartylabs.xmlscalpel.entity.OuterDocument OuterDocument} wrapper containing the
     * empty {@link org.w3c.dom.Document Document} containing only the document element from the
     * file provided
     * @throws Exception
     */
    public OuterDocument getOuterDocument(File file) throws Exception
    {
        initializeDocument(file);

        Document emptyDocument = getEmptyDocument();
        return new OuterDocument(emptyDocument, characterEncodingScheme);
    }

    /**
     * @return  {@link com.thirdpartylabs.xmlscalpel.entity.OuterDocument OuterDocument} wrapper containing the empty
     * {@link org.w3c.dom.Document Document} containing only the document element from the last file provided to this
     * instance of {@link com.thirdpartylabs.xmlscalpel.io.reader.StreamingXMLReader StreamingXMLReader}
     * @throws Exception
     */
    public OuterDocument getOuterDocument() throws Exception
    {
        Document emptyDocument = getEmptyDocument();
        return new OuterDocument(emptyDocument, characterEncodingScheme);
    }

    /**
     * Normalize and set the paths
     * @param targetPathInput The list of path Strings
     */
    private void setTargetPaths(List<String> targetPathInput)
    {
        if (targetPathInput == null)
        {
            targetPaths.clear();
            return;
        }

        // Normalize the paths and set them as the targetPath list
        targetPaths = targetPathInput.stream()
                .map(this::normalizeTargetPath)
                .collect(Collectors.toList());
    }

    private void pushTag(QName qName)
    {
        tagStack.push(normalizeQname(qName));
        currentPath = getCurrentTagPath();
    }

    private void popTag()
    {
        tagStack.pop();
        currentPath = getCurrentTagPath();
    }

    private boolean elementIsEligibleForProcessing()
    {
        // If there are no path specs, always handle the opening element events
        if (targetPaths.isEmpty())
        {
            return true;
        }

        // Only handle the event if the current path matches a user path
        return targetPaths.contains(currentPath);
    }

    /**
     * Get the current path based upon the tag stack
     */
    private String getCurrentTagPath()
    {
        String path = String.join("/", tagStack);

        return normalizeTargetPath(path);
    }

    /**
     * Create a prefix:localName representation of the provided {@link javax.xml.namespace.QName QName}
     * @param qName {@link javax.xml.namespace.QName QName} to normalize
     * @return prefix:localName
     */
    private String normalizeQname(QName qName)
    {
        String prefix = qName.getPrefix();
        String tagName = qName.getLocalPart();

        if (prefix != null && !prefix.isBlank())
        {
            tagName = prefix + ":" + tagName;
        }

        return tagName;
    }

    /**
     * Always ensure paths are formatted as we expect them to be
     * @param path Path to normalize
     * @return Normalized path
     */
    private String normalizeTargetPath(String path)
    {
        return path.replaceAll("^/?(.*[^/$])/?", "\\/$1");
    }
}
