package com.java_polytech.executor;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.Arrays;
import java.util.function.Function;

public class CodingProcessorBuffer {
    private static final int BITS_IN_BYTE_COUNT = 8;
    private static final int SHIFT = 1;

    private final Function<byte[], RC> out;
    private final byte[] buffer;
    private int filled;
    private int bufferByte;
    private int bufferByteFreeBits = BITS_IN_BYTE_COUNT;

    CodingProcessorBuffer(int bufferSize, Function<byte[], RC> out) {
        buffer = new byte[bufferSize];
        this.out = out;
    }

    public RC writeBit(int bit) {
        RC rc = RC.RC_SUCCESS;
        if (bufferByteFreeBits == 0) {
            // bufferByteFreeBits to default need to avoid recursion in case of last byte during writing bit
            bufferByteFreeBits = BITS_IN_BYTE_COUNT;
            rc = writeByte(bufferByte);
            if (!rc.isSuccess()) {
                return rc;
            }

            clearBuffByte();
        }
        bufferByte = ((bufferByte << SHIFT) | bit);
        bufferByteFreeBits--;
        return rc;
    }

    public RC writeByte(int sym) {
        buffer[filled++] = (byte) sym;
        if (filled == buffer.length) {
            return flush();
        }
        return RC.RC_SUCCESS;
    }

    public RC flush() {
        if (bufferByteFreeBits != BITS_IN_BYTE_COUNT) {
            // Case when some bits are written in bufferByte
            bufferByte <<= bufferByteFreeBits;
            // bufferByteFreeBits to default need to avoid recursion in case of last byte during writing bit
            bufferByteFreeBits = BITS_IN_BYTE_COUNT;
            RC rc = writeByte(bufferByte);
            if (!rc.isSuccess()) {
                return rc;
            }

            clearBuffByte();
        }

        byte[] res = buffer;
        if (filled < buffer.length) {
            res = Arrays.copyOf(buffer, filled);
        }
        filled = 0;

        return out.apply(res);
    }


    private void clearBuffByte() {
        bufferByte = 0;
        bufferByteFreeBits = BITS_IN_BYTE_COUNT;
    }
}
