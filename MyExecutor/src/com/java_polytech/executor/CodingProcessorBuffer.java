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
            // To avoid Out of bounds
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
        //TODO: Вот это место потенциально может быть причиной ошибок из-за каста
        buffer[filled++] = (byte) sym;
        if (filled == buffer.length) {
            return flush();
        }
        return RC.RC_SUCCESS;
    }

    public RC flush() {
        //TODO: Вот эта функция тоже может быть источником проблем, проверить, что она не записывает пустой массив, что не делает бесконечную рекурсию
        if (bufferByteFreeBits != BITS_IN_BYTE_COUNT) {
            // Case when some bits are written in bufferByte
            bufferByte <<= bufferByteFreeBits;
            //Potential recursion and loop, but I don't use writeBit and writeByte together in one class, so it's safe
            //bufferByteFreeBits to default need to avoid recursion in case of last byte during writing bit
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
        if (res.length != 0) {
            return out.apply(res);
        } else {
            // This case in for flush -> writeByte -> flush (It can be because of opportunity to write addition of byteBuffer from flush)
            return RC.RC_SUCCESS;
        }
    }


    private void clearBuffByte() {
        bufferByte = 0;
        bufferByteFreeBits = BITS_IN_BYTE_COUNT;
    }
}
