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

package com.thirdpartylabs.xmlscalpel.entity;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper for the empty document read by the
 * {@link  com.thirdpartylabs.xmlscalpel.io.reader.StreamingXMLReader StreamingXMLReader}
 * <p>
 * Provides the ability to generate new {@link org.w3c.dom.DocumentFragment DocumentFragments} from XML strings from
 * the document that have namespaces bound appropriately. It is only necessary to pass the retrieved XML
 * strings through this process if the XML uses namespaces with prefixes, or if you want to generate XML documents
 * that wrap the retrieved nodes in the original document element.
 */
public class OuterDocument
{
    private Document emptyDocument;
    private String characterEncoding = StandardCharsets.UTF_8.toString();
    private String emptyDocumentString;

    /**
     * @param emptyDocument {@link org.w3c.dom.Document Document} containing an a document node
     */
    public OuterDocument(Document emptyDocument)
    {
        this.emptyDocument = emptyDocument;
    }

    /**
     *
     * @param emptyDocument {@link org.w3c.dom.Document Document} containing an a document node
     * @param characterEncoding Specify character encoding og the file
     */
    public OuterDocument(Document emptyDocument, String characterEncoding)
    {
        this.emptyDocument = emptyDocument;
        this.characterEncoding = characterEncoding;
    }

    /**
     * The XML {@link org.w3c.dom.Document Document} containing only the document element
     * @return {@link org.w3c.dom.Document Document}
     */
    public Document getBareDocument()
    {
        return emptyDocument;
    }

    /**
     * @param emptyDocument {@link org.w3c.dom.Document Document} containing only a document element
     */
    public void setBareDocument(Document emptyDocument)
    {
        this.emptyDocument = emptyDocument;
        emptyDocumentString = null;
    }

    /**
     * Returns a {@link org.w3c.dom.DocumentFragment DocumentFragment} from the provided XML that is bound with the
     * namespaces from the original outer document. Intended to be used for rehydrating nodes retrieved by byte offset
     * from the original XML file.
     *<p>
     * This process is only necessary when retrieving nodes that contain namespace prefixes
     *
     * @param xml XML {@link java.lang.String String} to hydrate
     * @return {@link org.w3c.dom.DocumentFragment DocumentFragment} representation of the XML.
     * Namespaces are bound, if applicable
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     * @throws SAXException
     */
    public DocumentFragment getDocumentFragmentForXmlString(String xml) throws ParserConfigurationException,
            TransformerException, IOException, SAXException
    {
        // If we have not created a string representation of the document yet, do it now
        if (emptyDocumentString == null)
        {
            /*
             Place a comment inside the document element that we can use as a token that can
             be replaced with retrieved XML
             */
            Comment replacementToken = emptyDocument.createComment("replace");
            emptyDocument.getDocumentElement().appendChild(replacementToken);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, characterEncoding);

            StringWriter writer = new StringWriter();
            StreamResult output = new StreamResult(writer);

            Source input = new DOMSource(emptyDocument);

            transformer.transform(input, output);

            writer.flush();

            emptyDocumentString = writer.toString();

            // Remember to remove the comment we added
            emptyDocument.getDocumentElement().removeChild(replacementToken);
        }

        // Replace the token with the provided XML string
        String newDocumentString = emptyDocumentString.replace("<!--replace-->", xml);

        // Set up everything we need to parse the XML, make sure we're namespace aware
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document parsed = builder.parse(new InputSource(new StringReader(newDocumentString)));

        // Create a DocumentFragment wrapper for our output
        DocumentFragment fragment = parsed.createDocumentFragment();

        // There is probably whitespace to go through before we get to our element...
        NodeList children = parsed.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node currNode = children.item(i);

            // When we hit our element, se it in the output DocumentFragment and bail out
            if (currNode.getNodeType() == Node.ELEMENT_NODE)
            {
                fragment.appendChild(currNode);
                break;
            }
        }

        // Send it
        return fragment;
    }
}
