package com.java_polytech.manager;

import com.java_polytech.pipeline_interfaces.*;

import java.io.*;

public class Manager {
    private Config config;

    InputStream inputStream;
    OutputStream outputStream;

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
        config = new Config();
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
        if (!(rc = reader.setConfig(config.getReaderConfigFilename())).isSuccess()) {
            return rc;
        }
        if (!(rc = executor.setConfig(config.getExecutorConfigFilename())).isSuccess()) {
            return rc;
        }
        if (!(rc = writer.setConfig(config.getWriterConfigFilename())).isSuccess()) {
            return rc;
        }
        if (!(rc = reader.setInputStream(inputStream)).isSuccess()) {
            return rc;
        }

        return writer.setOutputStream(outputStream);
    }

    private RC openStreams() {
        try {
            inputStream = new FileInputStream(config.getInputFilename());
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }
        try {
            outputStream = new FileOutputStream(config.getOutputFilename());
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
        reader = (IReader) getInstance(config.getReaderClassName(), IReader.class);
        if (reader == null) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }

        writer = (IWriter) getInstance(config.getWriterClassName(), IWriter.class);
        if (writer == null) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }

        executor = (IExecutor) getInstance(config.getExecutorClassName(), IExecutor.class);
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
