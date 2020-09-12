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

package com.thirdpartylabs.xmlscalpel.processor;

import com.thirdpartylabs.xmlscalpel.entity.*;
import com.thirdpartylabs.xmlscalpel.io.reader.RandomAccessXMLReader;
import com.thirdpartylabs.xmlscalpel.io.reader.StreamingXMLReader;
import com.thirdpartylabs.xmlscalpel.mapper.PersonMapper;
import com.thirdpartylabs.xmlscalpel.mapper.PurchaseOrderAddressMapper;
import com.thirdpartylabs.xmlscalpel.mapper.PurchaseOrderItemMapper;
import com.thirdpartylabs.xmlscalpel.mapper.PurchaseOrderMapper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeTest
{
    /**
     * Basic test that reads an 100 node XML file with no namespace, unmarshalls each Fragment
     * to a POJO that represents the record, then randomly chooses 25 unique entries to retrieve from the
     * file via the RandomAccessXMLReader and unmarshall to compare to the objects that were created from
     * the data that was extracted via the streaming reader.
     */
    @Test
    void testReaderPopulatedCollection() throws Exception
    {
        // Get the people.xml test file
        URL fileUrl = getClass().getResource("/data/people.xml");
        String decodedPath = URLDecoder.decode(fileUrl.getFile(), StandardCharsets.UTF_8.toString());

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        File testFile = new File(decodedPath);

        // Create a collection to hold the Fragments and instantiate an XMLStreamProcessor with it
        List<Fragment> nodeEntities = new ArrayList<>();
        XMLStreamProcessor processor = new CollectionPopulatingXMLStreamProcessor(nodeEntities);

        // Instantiate a StreamingXMLReader and kick it off with our file and processor
        StreamingXMLReader reader = new StreamingXMLReader();
        reader.readFile(testFile, processor);

        // Make sure we have all of the expected nodes.
        assertEquals(100, nodeEntities.size(), "Expected 100 nodes");

        // Set up collections for our Person and XMLByteLocation objects
        List<Person> people = new ArrayList<>();
        Map<String, XMLByteLocation> locationMap = new HashMap<>();

        /*
            Spin through all of the extracted fragments and unmarshall them
            Associate each corresponding XMLByteLocation to the person via the UID field on the Person
         */
        for (Fragment fragment : nodeEntities)
        {
            Person person = PersonMapper.fromDomNode(fragment.getDocumentFragment());
            people.add(person);
            locationMap.put(person.getUid(), fragment.getXmlByteLocation());
        }

        int totalPeople = people.size();
        int numToTest = 25;

        ArrayList<Integer> testedIndices = new ArrayList<>();

        // Randomly grab 25 people to test
        Random random = new Random();
        int testIndex = random.nextInt(totalPeople);

        while (numToTest > 0)
        {
            // No repeats
            while (testedIndices.contains(testIndex))
            {
                testIndex = random.nextInt(totalPeople);
            }

            testedIndices.add(testIndex);

            // Get the Person object that was created using the standard streaming read technique
            Person controlPerson = people.get(testIndex);

            // Get the corresponding XMLByteLocation for this person
            XMLByteLocation location = locationMap.get(controlPerson.getUid());

            // Read in the string as defined by the range in the XMLByteLocation
            String xml = RandomAccessXMLReader.read(testFile, location);

            // Create a document from the string and unmarshall it to a Person
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));

            Document document = builder.parse(is);
            document.setXmlStandalone(true);

            Person testPerson = PersonMapper.fromDomNode(document);

            // Test to verify that these people are identical
            assertEquals(controlPerson, testPerson, "Control and test person entities should be equal");

            // One down...
            numToTest--;
        }
    }

    /**
     * More complex test to verify namespace rehydration
     */
    @Test
    void testReaderPopulatedCollectionWithNamespace() throws Exception
    {
        // Get the po_namespace.xml file
        URL fileUrl = getClass().getResource("/data/po_namespace.xml");
        String decodedPath = URLDecoder.decode(fileUrl.getFile(), StandardCharsets.UTF_8.toString());

        File testFile = new File(decodedPath);

        // Create a collection to hold the Fragments and instantiate an XMLStreamProcessor with it
        List<Fragment> nodeEntities = new ArrayList<>();
        XMLStreamProcessor processor = new CollectionPopulatingXMLStreamProcessor(nodeEntities);

        // Instantiate a StreamingXMLReader and kick it off with our file and processor
        StreamingXMLReader reader = new StreamingXMLReader();
        reader.readFile(testFile, processor);

        // Make sure we have all of the expected nodes
        assertEquals(3, nodeEntities.size(), "Expected 3 nodes");

        // Set up collections for our PurchaseOrder and XMLByteLocation objects
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        Map<String, XMLByteLocation> locationMap = new HashMap<>();

        /*
            Spin through all of the extracted fragments and unmarshall them
            Associate each corresponding XMLByteLocation to the person via the OrderNumber field on
            the PurchaseOrder
         */
        for (Fragment fragment : nodeEntities)
        {
            PurchaseOrder purchaseOrder = PurchaseOrderMapper.fromDocumentFragment(fragment.getDocumentFragment());
            purchaseOrders.add(purchaseOrder);
            locationMap.put(purchaseOrder.getOrderNumber(), fragment.getXmlByteLocation());
        }

        // This there are only three items, let's test them all
        int totalPurchaseOrders = purchaseOrders.size();
        int numToTest = totalPurchaseOrders;
        ArrayList<Integer> testedIndices = new ArrayList<>();

        Random random = new Random();
        int testIndex = random.nextInt(totalPurchaseOrders);

        while (numToTest > 0)
        {
            // No repeats
            while (testedIndices.contains(testIndex))
            {
                testIndex = random.nextInt(totalPurchaseOrders);
            }

            testedIndices.add(testIndex);

            // Get the PurchaseOrder object that was created using the standard streaming read technique
            PurchaseOrder controlPurchaseOrder = purchaseOrders.get(testIndex);

            // Get the corresponding XMLByteLocation for this PurchaseOrder
            XMLByteLocation location = locationMap.get(controlPurchaseOrder.getOrderNumber());

            // Read in the string as defined by the range in the XMLByteLocation
            String retrievedXml = RandomAccessXMLReader.read(testFile, location);

            /*
                Since we're dealing with namespaces, we need to run this retrieved XML through the original outer
                document to set up all of the bindings correctly.

                Get an OuterDocument instance from the reader  and use it to generate a DocumentFragment from the
                retrieved XML string.
             */
            OuterDocument outerDocument = reader.getOuterDocument();
            DocumentFragment fragment = outerDocument.getDocumentFragmentForXmlString(retrievedXml);

            /*
                Unmarshall the retrieved fragment.
                This will chuck a wobbly if the namespaces are not bound correctly.
             */
            PurchaseOrder testPurchaseOrder = PurchaseOrderMapper.fromDocumentFragment(fragment);

            // Test to verify that the objects are identical
            assertEquals(controlPurchaseOrder, testPurchaseOrder,
                    "Control and test purchase orders should be equal");

            numToTest--;
        }
    }

    /**
     * More complex test to verify namespace rehydration with pretty-printed XML
     *
     * Verify traversal logic with whitespace
     */
    @Test
    void testReaderPopulatedCollectionWithNamespaceAndTargetNodesWhitespace() throws Exception
    {
        URL fileUrl = getClass().getResource("/data/po_namespace.xml");
        testReaderPopulatedCollectionWithNamespaceAndTargetNodes(fileUrl);
    }

    /**
     * More complex test to verify namespace rehydration with compact (non-whitespace) XML
     *
     * Verify traversal logic without whitespace
     */
    @Test
    void testReaderPopulatedCollectionWithNamespaceAndTargetNodesFlat() throws Exception
    {
        URL fileUrl = getClass().getResource("/data/po_namespace_flat.xml");
        testReaderPopulatedCollectionWithNamespaceAndTargetNodes(fileUrl);
    }

    /**
     * Process purchase order data, extracting different sub-node types
     */
    void testReaderPopulatedCollectionWithNamespaceAndTargetNodes(URL fileUrl) throws Exception
    {
        // Get the po_namespace.xml file
        String decodedPath = URLDecoder.decode(fileUrl.getFile(), StandardCharsets.UTF_8.toString());

        File testFile = new File(decodedPath);

        // Create a collection to hold the Fragments and instantiate an XMLStreamProcessor with it
        List<Fragment> nodeEntities = new ArrayList<>();
        XMLStreamProcessor processor = new CollectionPopulatingXMLStreamProcessor(nodeEntities);

        /*
            Pass in a list of paths
            Address and Item nodes should be extracted.
            Try sending both leading and trailing slashed to make sure they are properly normalized by the reader
         */
        List<String> targetNodes = Arrays.asList(
                "/aw:PurchaseOrders/aw:PurchaseOrder/aw:Address",
                "aw:PurchaseOrders/aw:PurchaseOrder/aw:Items/aw:Item/"
        );

        // Instantiate a StreamingXMLReader and kick it off with our file and processor
        StreamingXMLReader reader = new StreamingXMLReader();
        reader.readFile(testFile, processor, targetNodes);

        // Make sure we have all of the expected nodes
        assertEquals(11, nodeEntities.size(), "Expected 11 nodes");

        // Set up collections for our PurchaseOrder and XMLByteLocation objects
        List<PurchaseOrderAddress> addresses = new ArrayList<>();
        List<PurchaseOrderItem> items = new ArrayList<>();
        Map<Integer, XMLByteLocation> addressLocationMap = new HashMap<>();
        Map<Integer, XMLByteLocation> itemLocationMap = new HashMap<>();

        /*
            Spin through all of the extracted fragments and unmarshall them
            Associate each corresponding XMLByteLocation to the person via the OrderNumber field on
            the PurchaseOrder
         */
        for (Fragment fragment : nodeEntities)
        {
            switch (fragment.getDocumentFragment().getFirstChild().getLocalName())
            {
                case "Address":
                    PurchaseOrderAddress currAddress = PurchaseOrderAddressMapper.fromDomNode(fragment.getDocumentFragment().getFirstChild());
                    addresses.add(currAddress);
                    addressLocationMap.put(currAddress.hashCode(), fragment.getXmlByteLocation());
                    break;
                case "Item":
                    PurchaseOrderItem currItem = PurchaseOrderItemMapper.fromDomNode(fragment.getDocumentFragment().getFirstChild());
                    items.add(currItem);
                    itemLocationMap.put(currItem.hashCode(), fragment.getXmlByteLocation());
                    break;
                default:
                    throw new Exception("Unexpected element type");
            }
        }

        // Make sure we have all of the expected nodes
        assertEquals( 6, addresses.size(), "Expected 6 address");
        assertEquals(5, items.size(), "Expected 5 items");

        // Test all addresses
        int totalAddresses = addresses.size();
        int numToTest = totalAddresses;
        ArrayList<Integer> testedIndices = new ArrayList<>();

        Random random = new Random();
        int testIndex = random.nextInt(totalAddresses);

        while (numToTest > 0)
        {
            // No repeats
            while (testedIndices.contains(testIndex))
            {
                testIndex = random.nextInt(totalAddresses);
            }

            testedIndices.add(testIndex);

            // Get the PurchaseOrderAddress object that was created using the standard streaming read technique
            PurchaseOrderAddress controlAddress = addresses.get(testIndex);

            // Get the corresponding XMLByteLocation for this PurchaseOrderAddress
            XMLByteLocation location = addressLocationMap.get(controlAddress.hashCode());

            // Read in the string as defined by the range in the XMLByteLocation
            String retrievedXml = RandomAccessXMLReader.read(testFile, location);

            /*
                Since we're dealing with namespaces, we need to run this retrieved XML through the original outer
                document to set up all of the bindings correctly.

                Get an OuterDocument instance from the reader  and use it to generate a DocumentFragment from the
                retrieved XML string.
             */
            OuterDocument outerDocument = reader.getOuterDocument();
            DocumentFragment fragment = outerDocument.getDocumentFragmentForXmlString(retrievedXml);

            /*
                Unmarshall the retrieved fragment.
                This will chuck a wobbly if the namespaces are not bound correctly.
             */
            PurchaseOrderAddress testAddress = PurchaseOrderAddressMapper.fromDomNode(fragment.getFirstChild());

            // Test to verify that the objects are identical
            assertEquals(controlAddress, testAddress,
                    "Control and test addresses should be equal");

            numToTest--;
        }

        // Test all items
        int totalItems = items.size();
        numToTest = totalItems;
        testedIndices.clear();

        testIndex = random.nextInt(totalItems);

        while (numToTest > 0)
        {
            // No repeats
            while (testedIndices.contains(testIndex))
            {
                testIndex = random.nextInt(totalItems);
            }

            testedIndices.add(testIndex);

            // Get the PurchaseOrderItem object that was created using the standard streaming read technique
            PurchaseOrderItem controlItem = items.get(testIndex);

            // Get the corresponding XMLByteLocation for this PurchaseOrderItem
            XMLByteLocation location = itemLocationMap.get(controlItem.hashCode());

            // Read in the string as defined by the range in the XMLByteLocation
            String retrievedXml = RandomAccessXMLReader.read(testFile, location);

            /*
                Since we're dealing with namespaces, we need to run this retrieved XML through the original outer
                document to set up all of the bindings correctly.

                Get an OuterDocument instance from the reader  and use it to generate a DocumentFragment from the
                retrieved XML string.
             */
            OuterDocument outerDocument = reader.getOuterDocument();
            DocumentFragment fragment = outerDocument.getDocumentFragmentForXmlString(retrievedXml);

            /*
                Unmarshall the retrieved fragment.
                This will chuck a wobbly if the namespaces are not bound correctly.
             */
            PurchaseOrderItem testItem = PurchaseOrderItemMapper.fromDomNode(fragment.getFirstChild());

            // Test to verify that the objects are identical
            assertEquals(controlItem, testItem,
                    "Control and test items should be equal");

            numToTest--;
        }
    }

    @Test
    public void testCdataReadProperly() throws Exception
    {
        // Get the people.xml test file
        URL fileUrl = getClass().getResource("/data/test_cdata.xml");
        String decodedPath = URLDecoder.decode(fileUrl.getFile(), StandardCharsets.UTF_8.toString());

        File testFile = new File(decodedPath);

        // Create a collection to hold the Fragments and instantiate an XMLStreamProcessor with it
        List<Fragment> nodeEntities = new ArrayList<>();
        XMLStreamProcessor processor = new CollectionPopulatingXMLStreamProcessor(nodeEntities);

        // Instantiate a StreamingXMLReader and kick it off with our file and processor
        StreamingXMLReader reader = new StreamingXMLReader();
        reader.readFile(testFile, processor);

        // Make sure we have all of the expected nodes
        assertEquals(2, nodeEntities.size(), "Expected 2 nodes");

        List<Person> people = new ArrayList<>();

        for (Fragment fragment : nodeEntities)
        {
            // Read in the string as defined by the range in the XMLByteLocation and verify "<![CDATA[" is present
            String retrievedXml = RandomAccessXMLReader.read(testFile, fragment.getXmlByteLocation());
            assertTrue(retrievedXml.contains("<![CDATA["), "XML should contain string CDATA");

            Person person = PersonMapper.fromDomNode(fragment.getDocumentFragment());
            people.add(person);
        }

        String controlDescription = "<p>Lorem <b>ipsum</b> dolor sit amet</p>";
        for(Person currPerson:people)
        {
            assertEquals(controlDescription, currPerson.getDescription(), "Description read from CDATA should match expected value");
        }
    }
}