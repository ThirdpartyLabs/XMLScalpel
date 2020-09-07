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

import com.thirdpartylabs.xmlscalpel.entity.PurchaseOrderItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Unmarshaller for Item nodes within PurchaseOrder records
 */
public class PurchaseOrderItemMapper
{
    public static PurchaseOrderItem fromDomNode(Node domNode) throws Exception
    {
        Element itemElement = (Element) domNode;
        if (!itemElement.getTagName().equals("aw:Item"))
        {
            throw new Exception("This is not a purchase order item.");
        }

        String namespaceURI = "http://www.adventure-works.com";

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPartNumber(itemElement.getAttributeNS(namespaceURI, "PartNumber"));
        item.setProductName(itemElement.getElementsByTagNameNS(namespaceURI, "ProductName").item(0).getTextContent());
        item.setQuantity(itemElement.getElementsByTagNameNS(namespaceURI, "Quantity").item(0).getTextContent());
        item.setUsPrice(itemElement.getElementsByTagNameNS(namespaceURI, "USPrice").item(0).getTextContent());

        Element commentElement = (Element) itemElement.getElementsByTagNameNS(namespaceURI, "Comment").item(0);
        if (commentElement != null)
        {
            item.setComment(commentElement.getTextContent());
        }

        Element shipDateElement = (Element) itemElement.getElementsByTagNameNS(namespaceURI, "ShipDate").item(0);
        if (shipDateElement != null)
        {
            item.setShipDate(shipDateElement.getTextContent());
        }

        return item;
    }
}
