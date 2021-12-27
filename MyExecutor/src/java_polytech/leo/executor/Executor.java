/*
  Поскольку адаптивное арифметическое кодирование не подходит для многопоточки, executor просто пересылает полученные данные дальше,
  ничего с ними не делая
 */
package java_polytech.leo.executor;

import com.java_polytech.pipeline_interfaces.*;
import java_polytech.leo.universal_config.Grammar;
import java_polytech.leo.universal_config.ISyntaxAnalyzer;
import java_polytech.leo.universal_config.SyntaxAnalyzer;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Executor implements IExecutor {
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final int SIZE_OF_INT = 4;
    private static final int READER_PROVIDER_NUM = 0;

    private static final int MAX_BUFFER_SIZE = 1000000;
    private static final TYPE[] supportedTypes = {TYPE.BYTE_ARRAY};
    private TYPE pickedType;
    private IMediator mediator;

    IConsumer consumer;

    //Номер полученный от writer
    private int numFromConsumer;
    private final Queue<Long> availablePackets = new ConcurrentLinkedQueue<>();
    //обработанные пакеты
    private final Map<Long, byte[]> processedPackets = Collections.synchronizedMap(new HashMap<>());
    private volatile RC currentStatus = RC.RC_SUCCESS;


    @Override
    public RC setConfig(String s) {
        ISyntaxAnalyzer config = new SyntaxAnalyzer(RC.RCWho.EXECUTOR,
                new Grammar(BUFFER_SIZE_STRING));
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
        if (bufferSize % SIZE_OF_INT != 0) {
            // Проверка для массива char-ов сюда включается, поскольку если он делится на 4, то и на 2 поделится
            return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Size of buffer doesn't fit for ints");
        }
        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(long packetNum, int providerNum) {
        //Provider может быть всего один, так что игнорируем providerNum
        availablePackets.add(packetNum);
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        this.consumer = consumer;
        Pair<RC, Integer> pair = consumer.setProvider(this);
        numFromConsumer = pair.getValue();
        return pair.getKey();
    }

    @Override
    public TYPE[] getOutputTypes() {
        return supportedTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        if (type.equals(TYPE.BYTE_ARRAY)) {
            return new ByteMediator();
        }
        return null;
    }

    @Override
    public Pair<RC, Integer> setProvider(IProvider provider) {
        for (TYPE prType : provider.getOutputTypes()) {
            for (TYPE supType : supportedTypes) {
                if (prType.equals(supType)) {
                    pickedType = prType;
                    break;
                }
            }
        }

        if (pickedType == null) {
            return new Pair<>(RC.RC_WRITER_TYPES_INTERSECTION_EMPTY_ERROR, READER_PROVIDER_NUM);
        }

        mediator = provider.getMediator(pickedType);
        return new Pair<>(RC.RC_SUCCESS, READER_PROVIDER_NUM);
    }

    @Override
    public RC getStatus() {
        return currentStatus;
    }

    @Override
    public void run() {
        while (true) {
            if (availablePackets.isEmpty()) {
                try {
                    synchronized (this) {
                        wait(100);
                    }
                } catch (InterruptedException e) {
                    currentStatus = new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "Problems with waiting in executor " + numFromConsumer);
                }
                continue;
            }

            long packetNum = availablePackets.poll();
            if (packetNum == IConsumer.END_OF_MESSAGES) {
                break;
            }

            processedPackets.put(packetNum, (byte[]) mediator.getData(packetNum));
            RC rc = consumer.consume(packetNum, numFromConsumer);
            if (!rc.isSuccess()) {
                currentStatus = rc;
                break;
            }
        }

        consumer.consume(IConsumer.END_OF_MESSAGES, numFromConsumer);
    }

    private class ByteMediator implements IMediator {
        @Override
        public Object getData(long packetNum) {
            byte[] packet = processedPackets.remove(packetNum);
            byte[] outputData = new byte[packet.length];
            System.arraycopy(packet, 0, outputData, 0, packet.length);
            return outputData;
        }
    }
}
