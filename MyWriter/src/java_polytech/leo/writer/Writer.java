package java_polytech.leo.writer;

import com.java_polytech.pipeline_interfaces.*;
import java_polytech.leo.universal_config.Grammar;
import java_polytech.leo.universal_config.ISyntaxAnalyzer;
import java_polytech.leo.universal_config.SyntaxAnalyzer;
import javafx.util.Pair;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Writer implements IWriter {
    private static final String BUFFER_SIZE_STRING = "buffer_size";
    private static final int MAX_BUFFER_SIZE = 1000000;
    private static final int SIZE_OF_INT = 4;
    private static final int NO_PROVIDER_NUM = -1;

    private final TYPE[] supportedTypes = {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};
    private TYPE pickedType;
    // Медиаторы всех провайдеров
    private final List<IMediator> provMediators = new ArrayList<>();
    private BufferedOutputStream outputStream;
    private int bufferSize;
    // Количество провайдеров, которые передали сообщение о том, что сообщений больше не будет
    private int endProvidersC;

    private long packetNum = 0;
    // Доступные пакеты (наши провайдеры сообщили, что мы можем их забрать). Ключ - номер пакета, значение - номер провайдера
    private final Map<Long, Integer> availablePackets = Collections.synchronizedMap(new HashMap<>());
    private volatile RC currentStatus = RC.RC_SUCCESS;

    @Override
    public void run() {
        while (endProvidersC != provMediators.size() || !availablePackets.isEmpty()) {
            if (!availablePackets.containsKey(packetNum)) {
                try {
                    synchronized (this) {
                        wait(100);
                    }
                } catch (InterruptedException e) {
                    currentStatus = new RC(RC.RCWho.WRITER, RC.RCType.CODE_CUSTOM_ERROR, "Problems with waiting in writer");
                }
                continue;
            }

            try {
                // У меня нет случаев, когда сюда может прийти null. Теперь конец сообщений это приход END_OF_MESSAGES в качестве номера пакета
                outputStream.write(Objects.requireNonNull(getDataBytes(availablePackets.remove(packetNum), packetNum)));
            } catch (IOException e) {
                currentStatus = RC.RC_WRITER_FAILED_TO_WRITE;
                return;
            }

            if (packetNum < IConsumer.MAX_PACKET_NUM) {
                packetNum++;
            } else {
                // Защита от переполнения. Нужно учесть что Reader обнуляет текущий номер пакета точно так же
                packetNum = 0;
            }
        }

        try {
            outputStream.flush();
        } catch (IOException e) {
            currentStatus = RC.RC_WRITER_FAILED_TO_WRITE;
        }
    }

    @Override
    public RC consume(long packetNum, int providerNum) {
        if (packetNum != IConsumer.END_OF_MESSAGES) {
            availablePackets.put(packetNum, providerNum);
        } else {
            endProvidersC++;
        }

        return RC.RC_SUCCESS;
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
            return new Pair<>(RC.RC_WRITER_TYPES_INTERSECTION_EMPTY_ERROR, NO_PROVIDER_NUM);
        }

        provMediators.add(provider.getMediator(pickedType));
        //Номер провайдера это просто порядковый номер в массиве provMediators
        return new Pair<>(RC.RC_SUCCESS, provMediators.size() - 1);
    }

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
        if (bufferSize % SIZE_OF_INT != 0) {
            // Проверка для массива char-ов сюда включается, поскольку если он делится на 4, то и на 2 поделится
            return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Size of buffer doesn't fit for ints");
        }
        if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
            return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC getStatus() {
        return currentStatus;
    }

    /**
     * @param provNum   - номер провайдера у которого мы хотим взять пакет
     * @param packetNum - номер пакета, который хотим получить
     * @return Возвращает массив байт, отданный провайдером
     */
    private byte[] getDataBytes(int provNum, long packetNum) {
        Object data = provMediators.get(provNum).getData(packetNum);

        switch (pickedType) {
            case BYTE_ARRAY:
                return (byte[]) data;
            case CHAR_ARRAY:
                return new String((char[]) data).getBytes(StandardCharsets.UTF_8);
            case INT_ARRAY:
                ByteBuffer byteBuff = ByteBuffer.allocate(((int[]) data).length * SIZE_OF_INT);
                byteBuff.asIntBuffer().put((int[]) data);
                return byteBuff.array();
        }

        return null;
    }
}
