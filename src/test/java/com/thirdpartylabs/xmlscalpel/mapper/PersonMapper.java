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

import com.thirdpartylabs.xmlscalpel.entity.Person;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Unmarshaller for Person
 */
public class PersonMapper
{
    public static Person fromDomNode(Node domNode) throws Exception
    {
        Element personElement = (Element) domNode.getFirstChild();
        if (!personElement.getTagName().equals("person"))
        {
            throw new Exception("This is not a person.");
        }

        Person person = new Person();
        person.setUid(personElement.getElementsByTagName("uid").item(0).getTextContent());
        person.setName(personElement.getElementsByTagName("name").item(0).getTextContent());
        person.setAddress(personElement.getElementsByTagName("address").item(0).getTextContent());
        person.setCity(personElement.getElementsByTagName("city").item(0).getTextContent());
        person.setCountry(personElement.getElementsByTagName("country").item(0).getTextContent());
        person.setDescription(personElement.getElementsByTagName("description").item(0).getTextContent());
        person.setEmail(personElement.getElementsByTagName("email").item(0).getTextContent());
        person.setPhone(personElement.getElementsByTagName("phone").item(0).getTextContent());
        person.setPostcode(personElement.getElementsByTagName("postcode").item(0).getTextContent());

        return person;
    }
}
