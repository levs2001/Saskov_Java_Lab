package com.java_polytech.reader;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IReader;
import com.java_polytech.pipeline_interfaces.RC;
import com.java_polytech.universal_config.Grammar;
import com.java_polytech.universal_config.ISyntaxAnalyzer;
import com.java_polytech.universal_config.SyntaxAnalyzer;

import java.io.IOException;
import java.io.InputStream;

public class Reader implements IReader {
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final int MAX_BUFFER_SIZE = 1000000;

    private InputStream inputStream;
    private byte[] buffer;
    private int bufferSize;
    private IConsumer consumer;

    @Override
    public RC setInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return RC.RC_READER_FAILED_TO_READ;
        }

        this.inputStream = inputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC run() {
        try {
            int readCount;
            byte[] resultBuffer;
            while ((readCount = inputStream.read(buffer)) > 0) {
                if (readCount == bufferSize) {
                    resultBuffer = buffer;
                } else {
                    resultBuffer = new byte[readCount];
                    System.arraycopy(buffer, 0, resultBuffer, 0, readCount);
                }

                RC rc = consumer.consume(resultBuffer);
                if (!rc.isSuccess()) {
                    return rc;
                }
            }
        } catch (IOException e) {
            return RC.RC_READER_FAILED_TO_READ;
        }

        return consumer.consume(null);
    }

    @Override
    public RC setConfig(String s) {
        ISyntaxAnalyzer config = new SyntaxAnalyzer(RC.RCWho.READER, new Grammar(BUFFER_SIZE_STRING));
        RC rc = config.readConfig(s);
        if (!rc.isSuccess()) {
            return rc;
        }

        try {
            bufferSize = Integer.parseInt(config.getParam(BUFFER_SIZE_STRING));
        } catch (NumberFormatException e) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        buffer = new byte[bufferSize];
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer) {
        consumer = iConsumer;
        return RC.RC_SUCCESS;
    }
}
