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

package com.thirdpartylabs.xmlscalpel.mapper;

import com.thirdpartylabs.xmlscalpel.entity.PurchaseOrderAddress;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Unmarshaller for Address nodes within PurchaseOrder records
 */
public class PurchaseOrderAddressMapper
{
    public static PurchaseOrderAddress fromDomNode(Node domNode) throws Exception
    {
        Element addressElement = (Element) domNode;
        if (!addressElement.getTagName().equals("aw:Address"))
        {
            throw new Exception("This is not a purchase order address.");
        }

        String namespaceURI = "http://www.adventure-works.com";

        PurchaseOrderAddress address = new PurchaseOrderAddress();
        address.setType(addressElement.getAttributeNS(namespaceURI, "Type"));
        address.setCity(addressElement.getElementsByTagNameNS(namespaceURI, "City").item(0).getTextContent());
        address.setCountry(addressElement.getElementsByTagNameNS(namespaceURI, "Country").item(0).getTextContent());
        address.setName(addressElement.getElementsByTagNameNS(namespaceURI, "Name").item(0).getTextContent());
        address.setState(addressElement.getElementsByTagNameNS(namespaceURI, "State").item(0).getTextContent());
        address.setStreet(addressElement.getElementsByTagNameNS(namespaceURI, "Street").item(0).getTextContent());
        address.setZip(addressElement.getElementsByTagNameNS(namespaceURI, "Zip").item(0).getTextContent());

        return address;
    }
}
