# Overview

Extract XML elements from a file by byte offset. Includes a streaming reader that uses [Woodstox](https://github.com/FasterXML/woodstox) Stax implementation to extract elements from large files and report their byte offsets and lengths for later random-access retrieval, along with a random-access reader to efficiently extract elements from any point in very large files.

### Status
[![Build Status](https://travis-ci.org/ThirdpartyLabs/XMLScalpel.svg?branch=main)](https://travis-ci.org/ThirdpartyLabs/XMLScalpel)
[![Maven Central](https://img.shields.io/maven-central/v/com.thirdpartylabs/xmlscalpel-core?color=45bf17)](https://search.maven.org/artifact/com.thirdpartylabs/xmlscalpel-core)
[![Javadoc](https://javadoc.io/badge/com.thirdpartylabs/xmlscalpel-core.svg)](http://www.javadoc.io/doc/com.thirdpartylabs/xmlscalpel-core)

## Use Case
We developed XMLScalpel to solve a particular problem that arises when developing applications or integrations that generate 
or consume XML files. Often you need to extract specific records within very large XML files in order to verify 
the contents. We've employed a number of techniques over the years from copy and pasting from vi (slow and painful) to 
XSLT (slow and painful in its own unique way). For larger projects we've been building tools specifically for this type
of reporting, and it turned out to be more difficult than we anticipated. 

Say you have a feed of product data with 10K
records, and you would like to be able to pull some metadata for all records to display in a list to allow searching and 
selection, then display the full details for selected records. The first part is easy, the second, not so much. 
As it happens, most Stax implementations do not accurately report the character or byte offsets for nodes as they are 
being parsed, which is required if you want to go back into the file and pull out a specific record. We found 
[Woodstox](https://github.com/FasterXML/woodstox) to be rock-solid in this regard, which allowed us to gather accurate
character offsets for each extracted element, and build a solution that works, although there is still a problem. 

Since we're dealing with character offsets and multi-byte characters, there is no way to simply use the offset to set 
a pointer into a file and start reading in data, you have to start at the beginning of the file, reading in and
discarding characters until you reach your offset. That means the deeper into the file you need to go, the longer it 
will take to retrieve the data. We needed a way to get accurate byte offsets for elements that we extract using a
streaming reader so that we could just treat the XML file like RAM, pulling out ranges of bytes to rehydrate into 
document fragments whenever we want access to an entire element subtree. So that's what we built:
 
* A fairly simple low-level reader that gives the Stax implementation the character data it expects, while keeping 
track of those sweet, sweet byte offsets
* An easy-to-use streaming reader that encapsulates the extraction of all top-level XML nodes initially, along with the 
offset data 
* A random-access reader that can take the offset data generated during stream parsing and return XML strings from the
file
* A facility for rehydrating XML strings that contain namespace prefixes into document fragments with correct 
namespace bindings.

```
File bigFile = new File("bigfeed.xml");

// Create a collection to hold the Fragments and instantiate an XMLStreamProcessor with it
List<Fragment> nodeEntities = new ArrayList<>();
XMLStreamProcessor processor = new CollectionPopulatingXMLStreamProcessor(nodeEntities);

// Instantiate a StreamingXMLReader and kick it off with our file and processor
StreamingXMLReader reader = new StreamingXMLReader();
reader.readFile(bigFile, processor);

// Set up collections for our Product and XMLByteLocation objects
List<Product> products = new ArrayList<>();
Map<String, XMLByteLocation> locationMap = new HashMap<>();

/*
  Spin through the extracted fragments and unmarshall them
  Associate each corresponding XMLByteLocation to the product via the PartNumber field on the Product
 */
for (Fragment fragment : nodeEntities)
{
    Product product = ProductMapper.fromDomNode(fragment.getDocumentFragment());
    products.add(product);
    locationMap.put(product.getPartNumber(), fragment.getXmlByteLocation());
}

// Pick a product
Product targetProduct = products.get(23);

// Get the corresponding XMLByteLocation for this product
XMLByteLocation location = locationMap.get(targetProduct.getPartNumber());

// Read in the string as defined by the range in the XMLByteLocation
String retrievedXml = RandomAccessXMLReader.read(bigFile, location);

// Are there namespace prefixes? Hydrate a DocumentFragment with the proper bindings
OuterDocument outerDocument = reader.getOuterDocument();
DocumentFragment fragment = outerDocument.getDocumentFragmentForXmlString(retrievedXml);
```
## Maven

Use Maven (or Ivy) to to add as a dependency from Maven Central repository:

* Group id: `com.thirdpartylabs`
* Artifact id: `xmlscalpel-core`
* Latest published version: 0.0.1 (2020-09-08)

## Requirements

Requires Woodstox version 6, which requires Java 6 (JDK 1.6); as well as Stax API that is included in JDK.

## License

XMLScalpel is licensed under [Apache 2](http://www.apache.org/licenses/LICENSE-2.0.txt) license.
