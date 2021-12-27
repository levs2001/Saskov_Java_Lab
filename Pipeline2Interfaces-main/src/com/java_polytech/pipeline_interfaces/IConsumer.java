package com.java_polytech.pipeline_interfaces;

import javafx.util.Pair;

public interface IConsumer {
    long MAX_PACKET_NUM = Long.MAX_VALUE;
    long END_OF_MESSAGES = -1;
    /**
     *
     * @param provider
     * @return
     * RC - код возврата, позволяющий проверить все ли ок
     * Integer - consumerNum (номер выданный провайдером, провайдер нумерует своих консюмеров)
     */
    Pair<RC, Integer> setProvider(IProvider provider);

    /**
     *
     * @param packetNum - номер пакета данных
     *                  (пакеты должны нумероваться, чтобы последний элемент пайплайна смог все записать в правильной последовательности)
     * @param providerNum - консюмер должен знать номер провайдера, от которрого пришел пакет, чтобы потом запросить данные именно у него
     * @return
     * RC - код возврата, позволяющий проверить все ли ок
     */
    RC consume(long packetNum, int providerNum);
}

