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

import com.thirdpartylabs.xmlscalpel.entity.PurchaseOrder;
import com.thirdpartylabs.xmlscalpel.entity.PurchaseOrderAddress;
import com.thirdpartylabs.xmlscalpel.entity.PurchaseOrderItem;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;

/**
 * Unmarshaller for PurchaseOrder records
 */
public class PurchaseOrderMapper
{
    public static PurchaseOrder fromDocumentFragment(DocumentFragment fragment) throws Exception
    {
        Element poElement = (Element) fragment.getFirstChild();
        if (!poElement.getTagName().equals("aw:PurchaseOrder"))
        {
            throw new Exception("This is not a purchase order.");
        }

        PurchaseOrder po = new PurchaseOrder();

        String namespaceURI = "http://www.adventure-works.com";

        po.setOrderNumber(poElement.getAttributeNS(namespaceURI, "PurchaseOrderNumber"));
        po.setOrderDate(poElement.getAttributeNS(namespaceURI, "OrderDate"));

        Element deliveryNotesElement = (Element) poElement.getElementsByTagNameNS(namespaceURI, "DeliveryNotes").item(0);
        if (deliveryNotesElement != null)
        {
            po.setDeliveryNotes(deliveryNotesElement.getTextContent());
        }

        NodeList addressNodeList = poElement.getElementsByTagNameNS(namespaceURI, "Address");
        Set<PurchaseOrderAddress> addresses = new HashSet<>();

        for (int i = 0; i < addressNodeList.getLength(); i++)
        {
            addresses.add(PurchaseOrderAddressMapper.fromDomNode(addressNodeList.item(i)));
        }

        po.setAddresses(addresses);

        Element itemsElement = (Element) poElement.getElementsByTagNameNS(namespaceURI, "Items").item(0);
        if (itemsElement != null)
        {
            NodeList itemNodeList = itemsElement.getElementsByTagNameNS(namespaceURI, "Item");

            Set<PurchaseOrderItem> items = new HashSet<>();

            for (int i = 0; i < itemNodeList.getLength(); i++)
            {
                items.add(PurchaseOrderItemMapper.fromDomNode(itemNodeList.item(i)));
            }

            po.setItems(items);
        }

        return po;
    }
}
