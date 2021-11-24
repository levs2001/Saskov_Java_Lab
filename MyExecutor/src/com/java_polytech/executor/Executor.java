package com.java_polytech.executor;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IExecutor;
import com.java_polytech.pipeline_interfaces.RC;

public class Executor implements IExecutor {
    private static final int MAX_BUFFER_SIZE = 1000000;

    private ArithmeticCodingProcessor codingProcessor;

    IConsumer consumer;

    @Override
    public RC setConfig(String s) {
        Config config = new Config();
        RC rc = config.readConfig(s);
        if (!rc.isSuccess()) {
            return rc;
        }

        Config.WorkType wType = config.getWorkType();
        int bufferSize = config.getBufferSize();
        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }

        switch (wType) {
            case CODING:
                codingProcessor = new ArithmeticCoder(new CodingProcessorBuffer(bufferSize, consumer::consume));
                break;
            case DECODING:
                codingProcessor = new ArithmeticDecoder(new CodingProcessorBuffer(bufferSize, consumer::consume));
                break;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] bytes) {
        RC rc = RC.RC_SUCCESS;
        if (bytes == null) {
            rc = codingProcessor.finish();
            if (!rc.isSuccess()) {
                return rc;
            }
            return consumer.consume(null);
        }
        for (byte b : bytes) {
            rc = codingProcessor.putByte(b);
            if (!rc.isSuccess()) {
                return rc;
            }
        }
        return rc;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer) {
        consumer = iConsumer;
        return RC.RC_SUCCESS;
    }
}
