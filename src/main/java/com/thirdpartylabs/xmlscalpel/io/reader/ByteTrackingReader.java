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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * {@link java.io.FilterReader FilterReader} that tracks byte progress as chars are read.
 * <p>
 * Char offsets are mapped to byte offsets, so that known char offsets can be used to
 * determine byte offsets. Only a certain number (set in charMemoryDepth, currently 8192)
 * of the most recent offsets are remembered. This reader is specifically intended to be used
 * in conjunction with the {@link com.ctc.wstx.stax.WstxInputFactory Woodstox} streaming reader,
 * which seems to read 2048 chars ahead of the reported progress, so we need to account for that, and accommodate
 * potentially large tag names, since the end tag plus the read-ahead determine the length of memory we need.
 */
public class ByteTrackingReader extends FilterReader
{
    private static final int CHAR_ONE_BYTE_MASK = 0xFFFFFF80;
    private static final int CHAR_TWO_BYTES_MASK = 0xFFFFF800;
    private static final int CHAR_THREE_BYTES_MASK = 0xFFFF0000;
    private static final int CHAR_FOUR_BYTES_MASK = 0xFFE00000;
    private static final int CHAR_FIVE_BYTES_MASK = 0xFC000000;
    private static final int CHAR_SIX_BYTES_MASK = 0x80000000;

    // Number char offset to byte offset records to keep
    private static final int charMemoryDepth = 8192;

    // Maximum skip-buffer size
    private static final int maxSkipBufferSize = 8192;

    // Skip buffer, null until allocated
    private char[] skipBuffer = null;

    // Total bytes read
    private long byteProgress = 0;

    // Total chars read
    private long charProgress = 0;

    // The memory buffer
    private final LinkedList<Long> memory = new LinkedList<>();

    /**
     * Creates a new byte tracking filtered reader.
     *
     * @param in a {@link java.io.Reader Reader} object providing the underlying stream.
     * @throws NullPointerException if {@code in} is {@code null}
     */
    protected ByteTrackingReader(Reader in)
    {
        super(in);
    }

    /**
     * We read and count the bytes in chars we're skipping over
     *
     * @param n Number of chars to skip
     * @return Number of chars actually skipped
     * @throws java.io.IOException
     */
    @Override
    public long skip(long n) throws IOException
    {
        long endBytes = byteProgress + n;
        long skipProgress = 0L;

        while (byteProgress < endBytes)
        {
            int currSkipLength = (int) Math.min(endBytes - byteProgress, maxSkipBufferSize);

            if ((skipBuffer == null) || (skipBuffer.length < currSkipLength))
            {
                skipBuffer = new char[currSkipLength];
            }

            int currRead = super.read(skipBuffer, 0, skipBuffer.length);

            for (char currChar : skipBuffer)
            {
                rememberCharOffset(currChar);
            }

            skipProgress += currRead;
        }

        return skipProgress;
    }

    /**
     * Read and count the bytes in the chars that are read
     *
     * @param buffer Char array to be filled
     * @param off The buffer will be filled starting at this offset
     * @param len The number of chars to read
     * @return the number of chars read
     * @throws java.io.IOException
     */
    @Override
    public int read(char[] buffer, int off, int len) throws IOException
    {
        int red = super.read(buffer, off, len);

        for (char currChar : buffer)
        {
            rememberCharOffset(currChar);
        }

        return red;
    }

    /**
     * Read a char and count the bytes
     *
     * @return A single char read from the file
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException
    {
        int red = super.read();
        rememberCharOffset((char) red);
        return red;
    }

    /**
     * Return the byte offset for the provided char offset
     *
     * @param charOffset A character offset
     * @return -1 if if the charOffset is negative, or if the offset is outside the bounds of our memory
     */
    public long getByteOffsetForCharOffset(long charOffset)
    {
        if (charOffset < 0)
        {
            return -1;
        }

        int rewindCount = (int) (charProgress - charOffset);

        if (rewindCount < 0 || rewindCount >= memory.size())
        {
            return -1;
        }

        return memory.get(rewindCount);
    }

    /**
     * Determine the number of bytes in the character, update the byte offset, and store the mapping in memory
     * @param red a character to store data for in the offset memory
     */
    private void rememberCharOffset(char red)
    {
        charProgress++;
        byteProgress += countBytesForChar(red);

        if (memory.size() >= charMemoryDepth)
        {
            memory.removeLast();
        }

        memory.push(byteProgress);
    }

    /**
     * Return the number of bytes that represent the character
     *
     * @param character The character to be decoded
     * @return The number of bytes used to represent the char.
     */
    private int countBytesForChar(char character)
    {
        if ((character & CHAR_ONE_BYTE_MASK) == 0)
        {
            return 1;
        }
        else if ((character & CHAR_TWO_BYTES_MASK) == 0)
        {
            return 2;
        }
        else if ((character & CHAR_THREE_BYTES_MASK) == 0)
        {
            return 3;
        }
        else if ((character & CHAR_FOUR_BYTES_MASK) == 0)
        {
            return 4;
        }
        else if ((character & CHAR_FIVE_BYTES_MASK) == 0)
        {
            return 5;
        }
        else if ((character & CHAR_SIX_BYTES_MASK) == 0)
        {
            return 6;
        }
        else
        {
            return -1;
        }
    }
}