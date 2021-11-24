package com.java_polytech.writer;

import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Writer implements IWriter {
    private static final int MAX_BUFFER_SIZE = 1000000;

    private BufferedOutputStream outputStream;
    private int bufferSize;

    @Override
    public RC setOutputStream(OutputStream outputStream) {
        if(outputStream == null) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }

        this.outputStream = new BufferedOutputStream(outputStream, bufferSize);
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {
        Config config = new Config();
        RC rc = config.readConfig(s);
        if (!rc.isSuccess()) {
            return rc;
        }

        bufferSize = config.getBufferSize();
        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] bytes) {
        try {
            if(bytes == null) {
                outputStream.flush();
            } else {
                outputStream.write(bytes);
            }
        } catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }

        return RC.RC_SUCCESS;
    }
}
