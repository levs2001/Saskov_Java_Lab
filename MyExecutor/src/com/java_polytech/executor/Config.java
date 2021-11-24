package com.java_polytech.executor;

import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    private static final int PARAM_NAME_IND = 0;
    private static final int PARAM_IND = 1;
    private static final String MODE_STRING = "mode";
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final String CODING_MODE_STRING = "coding";
    private static final String DECODING_MODE_STRING = "decoding";
    private static final String SPLITTER_STRING = "=";

    private WorkType wType;
    private int bufferSize;

    public RC readConfig(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            RC rc = setParam(reader.readLine());
            if (!rc.isSuccess()) {
                return rc;
            }
            rc = setParam(reader.readLine());
            return rc;
        } catch (IOException e) {
            return RC.RC_READER_CONFIG_FILE_ERROR;
        }
    }

    public WorkType getWorkType() {
        return wType;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    private RC setParam(String paramStr) {
        String[] paramSet = paramStr.split(SPLITTER_STRING);

        switch (paramSet[PARAM_NAME_IND]) {
            case MODE_STRING:
                return setWorkType(paramSet[PARAM_IND]);
            case BUFFER_SIZE_STRING:
                bufferSize = Integer.parseInt(paramSet[PARAM_IND]);
                return RC.RC_SUCCESS;
            default:
                return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        }
    }

    private RC setWorkType(String workTypeStr) {
        if (workTypeStr.equals(CODING_MODE_STRING)) {
            wType = WorkType.CODING;
        } else if (workTypeStr.equals(DECODING_MODE_STRING)) {
            wType = WorkType.DECODING;
        } else {
            return RC.RC_WRITER_CONFIG_GRAMMAR_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    public enum WorkType {
        CODING,
        DECODING
    }
}
