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

import java.util.HashSet;
import java.util.Set;

public class PurchaseOrder
{
    private String orderNumber;
    private String orderDate;
    private String deliveryNotes;

    private Set<PurchaseOrderAddress> addresses = new HashSet<>();
    private Set<PurchaseOrderItem> items = new HashSet<>();

    public String getOrderNumber()
    {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber)
    {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate()
    {
        return orderDate;
    }

    public void setOrderDate(String orderDate)
    {
        this.orderDate = orderDate;
    }

    public String getDeliveryNotes()
    {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes)
    {
        this.deliveryNotes = deliveryNotes;
    }

    public Set<PurchaseOrderAddress> getAddresses()
    {
        return addresses;
    }

    public void setAddresses(Set<PurchaseOrderAddress> addresses)
    {
        this.addresses = addresses;
    }

    public Set<PurchaseOrderItem> getItems()
    {
        return items;
    }

    public void setItems(Set<PurchaseOrderItem> items)
    {
        this.items = items;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PurchaseOrder that = (PurchaseOrder) o;

        if (!getOrderNumber().equals(that.getOrderNumber()))
        {
            return false;
        }
        if (!getOrderDate().equals(that.getOrderDate()))
        {
            return false;
        }
        if (getDeliveryNotes() != null ? !getDeliveryNotes().equals(that.getDeliveryNotes()) : that.getDeliveryNotes() != null)
        {
            return false;
        }
        if (getAddresses() != null ? !getAddresses().equals(that.getAddresses()) : that.getAddresses() != null)
        {
            return false;
        }
        return getItems() != null ? getItems().equals(that.getItems()) : that.getItems() == null;
    }

    @Override
    public String toString()
    {
        return "PurchaseOrder{" +
               "orderNumber='" + orderNumber + '\'' +
               '}';
    }
}
