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

public class Person
{
    private String uid;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String postcode;
    private String country;
    private String description;

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getPostcode()
    {
        return postcode;
    }

    public void setPostcode(String postcode)
    {
        this.postcode = postcode;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

        Person person = (Person) o;

        if (!getUid().equals(person.getUid()))
        {
            return false;
        }
        if (!getName().equals(person.getName()))
        {
            return false;
        }
        if (!getPhone().equals(person.getPhone()))
        {
            return false;
        }
        if (!getEmail().equals(person.getEmail()))
        {
            return false;
        }
        if (!getAddress().equals(person.getAddress()))
        {
            return false;
        }
        if (!getCity().equals(person.getCity()))
        {
            return false;
        }
        if (!getPostcode().equals(person.getPostcode()))
        {
            return false;
        }
        if (!getCountry().equals(person.getCountry()))
        {
            return false;
        }
        return getDescription().equals(person.getDescription());
    }

    @Override
    public String toString()
    {
        return "Person{" +
               "uid='" + uid + '\'' +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
