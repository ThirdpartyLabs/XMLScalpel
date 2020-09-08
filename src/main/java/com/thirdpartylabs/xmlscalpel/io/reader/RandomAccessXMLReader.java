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

package com.thirdpartylabs.xmlscalpel.io.reader;

import com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Fast retrieval of strings from a file by byte offset and length as defined by an
 * {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation} object
 */
public class RandomAccessXMLReader
{
    /**
     * Get a {@link java.lang.String String} from the provided {@link java.io.File File} using the offset and
     * length from the {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation}, encoded with the
     * provided charset
     * @param file The {@link java.io.File File} to extract data from
     * @param xmlByteLocation Object containing the byte coordinates
     * @return String representation of the requested bytes
     * @throws IOException
     */
    public static String read(File file, XMLByteLocation xmlByteLocation) throws IOException
    {
        return read(file, xmlByteLocation, StandardCharsets.UTF_8);
    }

    /**
     * Get a {@link java.lang.String String} from the provided {@link java.io.File File} using the offset and length
     * from the {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation}, encoded with the
     * provided charset
     * @param file The file to extract data from
     * @param xmlByteLocation Object containing the byte coordinates
     * @param charset Charset to be used when creating the {@link java.lang.String String} from extracted bytes
     * @return String representation of the requested bytes
     * @throws IOException
     */
    public static String read(File file, XMLByteLocation xmlByteLocation, String charset) throws IOException
    {
        return read(file, xmlByteLocation, Charset.forName(charset));
    }

    /**
     * Get a {@link java.lang.String String} from the provided {@link java.io.File File} using the offset and length
     * from the {@link com.thirdpartylabs.xmlscalpel.entity.XMLByteLocation XMLByteLocation}, encoded with the
     * provided charset
     * @param file The file to extract data from
     * @param xmlByteLocation Object containing the byte coordinates
     * @param charset Charset to be used when creating the String from extracted bytes
     * @return String representation of the requested bytes
     * @throws IOException
     */
    public static String read(File file, XMLByteLocation xmlByteLocation, Charset charset) throws IOException
    {
        long byteOffset = xmlByteLocation.getOffset();
        long length = xmlByteLocation.getLength();

        // Open the file for reading as a RandomAccessFile
        RandomAccessFile ram = new RandomAccessFile(file, "r");

        // Keep track of how many bytes we've read
        int progress = 0;

        // Set the pointer to our offset
        ram.seek(byteOffset);

        // Set up a byte buffer with the appropriate length
        byte[] buffer = new byte[(int) length];

        // Read in 8K chunks
        int maxReadLength = (int) Math.min(length, 8192);
        while (progress < length)
        {
            /*
                Once we're within the range of the final chunk,
                change the length to the number of remaining bytes
             */
            int currLength = Math.min((int) length - progress, maxReadLength);

            // Get the current chunk
            int currRead = ram.read(buffer, progress, currLength);

            // Update our progress
            progress += currRead;
        }

        return new String(buffer, charset);
    }
}
