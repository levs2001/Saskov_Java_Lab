package com.java_polytech.executor;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IExecutor;
import com.java_polytech.pipeline_interfaces.RC;
import com.java_polytech.universal_config.Grammar;
import com.java_polytech.universal_config.ISyntaxAnalyzer;
import com.java_polytech.universal_config.SyntaxAnalyzer;

public class Executor implements IExecutor {
    private static final String MODE_STRING = "mode";
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final String CODING_MODE_STRING = "coding";
    private static final String DECODING_MODE_STRING = "decoding";

    private static final int MAX_BUFFER_SIZE = 1000000;

    private ArithmeticCodingProcessor codingProcessor;

    IConsumer consumer;

    @Override
    public RC setConfig(String s) {
        ISyntaxAnalyzer config = new SyntaxAnalyzer(RC.RCWho.EXECUTOR,
                new Grammar(MODE_STRING, BUFFER_SIZE_STRING));
        RC rc = config.readConfig(s);
        if (!rc.isSuccess()) {
            return rc;
        }

        int bufferSize;
        try {
            bufferSize = Integer.parseInt(config.getParam(BUFFER_SIZE_STRING));
        } catch (NumberFormatException e) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }

        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }

        switch (config.getParam(MODE_STRING)) {
            case CODING_MODE_STRING:
                codingProcessor = new ArithmeticCoder(new CodingProcessorBuffer(bufferSize, consumer::consume));
                break;
            case DECODING_MODE_STRING:
                codingProcessor = new ArithmeticDecoder(new CodingProcessorBuffer(bufferSize, consumer::consume));
                break;
            default:
                return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
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
