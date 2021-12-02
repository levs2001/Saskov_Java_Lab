package com.java_polytech.manager;

import com.java_polytech.pipeline_interfaces.*;
import com.java_polytech.universal_config.Grammar;
import com.java_polytech.universal_config.ISyntaxAnalyzer;
import com.java_polytech.universal_config.SyntaxAnalyzer;

import java.io.*;

public class Manager {
    private static final String INPUT_FILE_STRING = "input_file";
    private static final String OUTPUT_FILE_STRING = "output_file";
    private static final String READER_CONFIG_FILE_STRING = "reader_config_file";
    private static final String WRITER_CONFIG_FILE_STRING = "writer_config_file";
    private static final String EXECUTOR_CONFIG_FILE_STRING = "executor_config_file";
    private static final String READER_CLASS_STRING = "reader_class";
    private static final String WRITER_CLASS_STRING = "writer_class";
    private static final String EXECUTOR_CLASS_STRING = "executor_class";

    private ISyntaxAnalyzer config;

    private InputStream inputStream;
    private OutputStream outputStream;

    private IReader reader;
    private IWriter writer;
    private IExecutor executor;

    public RC run(String configFilename) {
        RC rc = setConfig(configFilename);
        if (!rc.isSuccess()) {
            return rc;
        }

        rc = buildPipeline();
        if (!rc.isSuccess()) {
            return rc;
        }

        rc = reader.run();
        if (!rc.isSuccess()) {
            return rc;
        }

        return closeStreams();
    }

    private RC setConfig(String s) {
        config = new SyntaxAnalyzer(RC.RCWho.MANAGER,
                new Grammar(INPUT_FILE_STRING, OUTPUT_FILE_STRING, READER_CONFIG_FILE_STRING, WRITER_CONFIG_FILE_STRING,
                        EXECUTOR_CONFIG_FILE_STRING, READER_CLASS_STRING, WRITER_CLASS_STRING, EXECUTOR_CLASS_STRING
                ));
        return config.readConfig(s);
    }

    private RC buildPipeline() {
        RC rc;
        if (!(rc = openStreams()).isSuccess()) {
            return rc;
        }
        if (!(rc = setParticipants()).isSuccess()) {
            return rc;
        }
        if (!(rc = reader.setConsumer(executor)).isSuccess()) {
            return rc;
        }
        if (!(rc = executor.setConsumer(writer)).isSuccess()) {
            return rc;
        }
        if (!(rc = reader.setConfig(config.getParam(READER_CONFIG_FILE_STRING))).isSuccess()) {
            return rc;
        }
        if (!(rc = executor.setConfig(config.getParam(EXECUTOR_CONFIG_FILE_STRING))).isSuccess()) {
            return rc;
        }
        if (!(rc = writer.setConfig(config.getParam(WRITER_CONFIG_FILE_STRING))).isSuccess()) {
            return rc;
        }
        if (!(rc = reader.setInputStream(inputStream)).isSuccess()) {
            return rc;
        }

        return writer.setOutputStream(outputStream);
    }

    private RC openStreams() {
        try {
            inputStream = new FileInputStream(config.getParam(INPUT_FILE_STRING));
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }
        try {
            outputStream = new FileOutputStream(config.getParam(OUTPUT_FILE_STRING));
        } catch (IOException ex) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }

        return RC.RC_SUCCESS;
    }

    private RC closeStreams() {
        boolean isClosed = true;
        try {
            inputStream.close();
        } catch (IOException ex) {
            isClosed = false;
        }
        try {
            outputStream.close();
        } catch (IOException ex) {
            isClosed = false;
        }

        if (!isClosed) {
            return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CUSTOM_ERROR, "Error during closing stream");
        }
        return RC.RC_SUCCESS;
    }

    private RC setParticipants() {
        reader = (IReader) getInstance(config.getParam(READER_CLASS_STRING), IReader.class);
        if (reader == null) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }

        writer = (IWriter) getInstance(config.getParam(WRITER_CLASS_STRING), IWriter.class);
        if (writer == null) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }

        executor = (IExecutor) getInstance(config.getParam(EXECUTOR_CLASS_STRING), IExecutor.class);
        if (executor == null) {
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }

        return RC.RC_SUCCESS;
    }

    private Object getInstance(String className, Class<?> inter) {
        Object ans = null;
        try {
            Class<?> clazz = Class.forName(className);
            if (inter.isAssignableFrom(clazz)) {
                ans = clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            return null;
        }

        return ans;
    }
}
