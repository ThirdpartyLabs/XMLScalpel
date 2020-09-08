/* XMLScalpel random access XML processor
 *
 * Copyright (c) 2020- Rob Ruchte, rob@thirdpartylabs.com
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thirdpartylabs.xmlscalpel.entity;

import org.w3c.dom.DocumentFragment;

/**
 * Wrapper for a {@link org.w3c.dom.DocumentFragment DocumentFragment} and corresponding
 * {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation}
 */
public class Fragment
{
    private final DocumentFragment documentFragment;
    private final XMLByteLocation xmlByteLocation;

    /**
     * @param documentFragment A {@link org.w3c.dom.DocumentFragment DocumentFragment} extracted from an XML file
     * @param location {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation} Object representing
     * the byte coordinates of the fragment
     */
    public Fragment(DocumentFragment documentFragment, XMLByteLocation location)
    {
        this.documentFragment = documentFragment;
        this.xmlByteLocation = location;
    }

    /**
     * @return {@link org.w3c.dom.DocumentFragment DocumentFragment} extracted from an XML file
     */
    public DocumentFragment getDocumentFragment()
    {
        return documentFragment;
    }

    /**
     *
     * @return {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation} Object representing
     * the byte coordinates of the fragment
     */
    public XMLByteLocation getXmlByteLocation()
    {
        return xmlByteLocation;
    }
}
