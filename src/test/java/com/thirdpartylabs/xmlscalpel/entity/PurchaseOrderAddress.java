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

public class PurchaseOrderAddress
{
    private String type;
    private String name;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getZip()
    {
        return zip;
    }

    public void setZip(String zip)
    {
        this.zip = zip;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
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

        PurchaseOrderAddress that = (PurchaseOrderAddress) o;

        if (!getType().equals(that.getType()))
        {
            return false;
        }
        if (!getName().equals(that.getName()))
        {
            return false;
        }
        if (!getStreet().equals(that.getStreet()))
        {
            return false;
        }
        if (!getCity().equals(that.getCity()))
        {
            return false;
        }
        if (!getState().equals(that.getState()))
        {
            return false;
        }
        if (!getZip().equals(that.getZip()))
        {
            return false;
        }
        return getCountry().equals(that.getCountry());
    }

    @Override
    public int hashCode()
    {
        int result = getType().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getStreet().hashCode();
        result = 31 * result + getCity().hashCode();
        result = 31 * result + getState().hashCode();
        result = 31 * result + getZip().hashCode();
        result = 31 * result + getCountry().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "PurchaseOrderAddress{" +
               "type='" + type + '\'' +
               ", name='" + name + '\'' +
               ", stree='" + street + '\'' +
               ", city='" + city + '\'' +
               ", state='" + state + '\'' +
               ", zip='" + zip + '\'' +
               ", country='" + country + '\'' +
               '}';
    }
}
