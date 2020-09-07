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

public class PurchaseOrderItem
{
    private String partNumber;
    private String productName;
    private String quantity;
    private String usPrice;
    private String shipDate;
    private String comment;

    public String getPartNumber()
    {
        return partNumber;
    }

    public void setPartNumber(String partNumber)
    {
        this.partNumber = partNumber;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getQuantity()
    {
        return quantity;
    }

    public void setQuantity(String quantity)
    {
        this.quantity = quantity;
    }

    public String getUsPrice()
    {
        return usPrice;
    }

    public void setUsPrice(String usPrice)
    {
        this.usPrice = usPrice;
    }

    public String getShipDate()
    {
        return shipDate;
    }

    public void setShipDate(String shipDate)
    {
        this.shipDate = shipDate;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
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

        PurchaseOrderItem that = (PurchaseOrderItem) o;

        if (!getPartNumber().equals(that.getPartNumber()))
        {
            return false;
        }
        if (!getProductName().equals(that.getProductName()))
        {
            return false;
        }
        if (!getQuantity().equals(that.getQuantity()))
        {
            return false;
        }
        if (!getUsPrice().equals(that.getUsPrice()))
        {
            return false;
        }
        if (getShipDate() != null ? !getShipDate().equals(that.getShipDate()) : that.getShipDate() != null)
        {
            return false;
        }
        return getComment() != null ? getComment().equals(that.getComment()) : that.getComment() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getPartNumber().hashCode();
        result = 31 * result + getProductName().hashCode();
        result = 31 * result + getQuantity().hashCode();
        result = 31 * result + getUsPrice().hashCode();
        result = 31 * result + (getShipDate() != null ? getShipDate().hashCode() : 0);
        result = 31 * result + (getComment() != null ? getComment().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "PurchaseOrderItem{" +
               "partNumber='" + partNumber + '\'' +
               ", productName='" + productName + '\'' +
               ", quantity='" + quantity + '\'' +
               ", uSPrice='" + usPrice + '\'' +
               '}';
    }
}
