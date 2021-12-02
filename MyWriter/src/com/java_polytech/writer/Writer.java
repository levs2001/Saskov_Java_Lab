package com.java_polytech.writer;

import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;
import com.java_polytech.universal_config.Grammar;
import com.java_polytech.universal_config.ISyntaxAnalyzer;
import com.java_polytech.universal_config.SyntaxAnalyzer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Writer implements IWriter {
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final int MAX_BUFFER_SIZE = 1000000;

    private BufferedOutputStream outputStream;
    private int bufferSize;

    @Override
    public RC setOutputStream(OutputStream outputStream) {
        if (outputStream == null) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }

        this.outputStream = new BufferedOutputStream(outputStream, bufferSize);
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {
        ISyntaxAnalyzer config = new SyntaxAnalyzer(RC.RCWho.WRITER, new Grammar(BUFFER_SIZE_STRING));
        RC rc = config.readConfig(s);
        if (!rc.isSuccess()) {
            return rc;
        }

        try {
            bufferSize = Integer.parseInt(config.getParam(BUFFER_SIZE_STRING));
        } catch (NumberFormatException e) {
            return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
        }

        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] bytes) {
        try {
            if (bytes == null) {
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
