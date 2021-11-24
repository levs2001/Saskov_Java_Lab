package com.java_polytech.manager;

import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    private static final int PARAM_NAME_IND = 0;
    private static final int PARAM_IND = 1;

    private static final String INPUT_FILE_STRING = "input_file";
    private static final String OUTPUT_FILE_STRING = "output_file";
    private static final String READER_CONFIG_FILE_STRING = "reader_config_file";
    private static final String WRITER_CONFIG_FILE_STRING = "writer_config_file";
    private static final String EXECUTOR_CONFIG_FILE_STRING = "executor_config_file";
    private static final String READER_CLASS_STRING = "reader_class";
    private static final String WRITER_CLASS_STRING = "writer_class";
    private static final String EXECUTOR_CLASS_STRING = "executor_class";
    private static final String SPLITTER_STRING = "=";

    private String inputFilename;
    private String outputFilename;
    private String readerConfigFilename;
    private String writerConfigFilename;
    private String executorConfigFilename;
    private String readerClassName;
    private String writerClassName;
    private String executorClassName;

    public RC readConfig(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                RC rc = setParam(line);
                if (!rc.isSuccess()) {
                    return rc;
                }
            }
        } catch (IOException e) {
            return RC.RC_READER_CONFIG_FILE_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    private RC setParam(String paramStr) {
        String[] paramSet = paramStr.split(SPLITTER_STRING);

        switch (paramSet[PARAM_NAME_IND]) {
            case INPUT_FILE_STRING:
                inputFilename = paramSet[PARAM_IND];
                break;
            case OUTPUT_FILE_STRING:
                outputFilename = paramSet[PARAM_IND];
                break;
            case READER_CONFIG_FILE_STRING:
                readerConfigFilename = paramSet[PARAM_IND];
                break;
            case WRITER_CONFIG_FILE_STRING:
                writerConfigFilename = paramSet[PARAM_IND];
                break;
            case EXECUTOR_CONFIG_FILE_STRING:
                executorConfigFilename = paramSet[PARAM_IND];
                break;
            case READER_CLASS_STRING:
                readerClassName = paramSet[PARAM_IND];
                break;
            case WRITER_CLASS_STRING:
                writerClassName = paramSet[PARAM_IND];
                break;
            case EXECUTOR_CLASS_STRING:
                executorClassName = paramSet[PARAM_IND];
                break;
            default:
                return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    public String getInputFilename() {
        return inputFilename;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public String getReaderConfigFilename() {
        return readerConfigFilename;
    }

    public String getWriterConfigFilename() {
        return writerConfigFilename;
    }

    public String getExecutorConfigFilename() {
        return executorConfigFilename;
    }

    public String getReaderClassName() {
        return readerClassName;
    }

    public String getWriterClassName() {
        return writerClassName;
    }

    public String getExecutorClassName() {
        return executorClassName;
    }
}
