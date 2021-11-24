package com.java_polytech.writer;

import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    private static final int PARAM_NAME_IND = 0;
    private static final int PARAM_IND = 1;
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final String SPLITTER_STRING = "=";

    private int bufferSize;

    public int getBufferSize() {
        return bufferSize;
    }

    public RC readConfig(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return setParam(reader.readLine());
        } catch (IOException e) {
            return RC.RC_READER_CONFIG_FILE_ERROR;
        }
    }

    private RC setParam(String paramStr) {
        String[] paramSet = paramStr.split(SPLITTER_STRING);

        if (!paramSet[PARAM_NAME_IND].equals(BUFFER_SIZE_STRING)) {
            return RC.RC_READER_CONFIG_GRAMMAR_ERROR;
        }

        bufferSize = Integer.parseInt(paramSet[PARAM_IND]);
        return RC.RC_SUCCESS;
    }
}
