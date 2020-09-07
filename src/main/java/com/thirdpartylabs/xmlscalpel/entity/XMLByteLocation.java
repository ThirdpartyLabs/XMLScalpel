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

/**
 * Defines the location in a file of a specific XML node.
 */
public class XMLByteLocation
{
    private int index;
    private long offset;
    private long length;

    /**
     * @param index Sequence of the node within the document
     * @param offset Location of the first byte of the string that contains the XML for a node
     * @param length Number of bytes in the string
     */
    public XMLByteLocation(int index, long offset, long length)
    {
        this.index = index;
        this.offset = offset;
        this.length = length;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public long getOffset()
    {
        return offset;
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
    }

    public long getLength()
    {
        return length;
    }

    public void setByte(long length)
    {
        this.length = length;
    }

    @Override
    public String toString()
    {
        return "XMLByteLocation{" +
               "offset=" + offset +
               ", length=" + length +
               '}';
    }
}
