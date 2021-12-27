package com.java_polytech.pipeline_interfaces;

public interface IMediator {
    /**
     *
     * @param packetNum - запрашиваемый номер пакета
     * @return
     * if result is NULL, then it is end and prepare for destroy
     */
    Object getData(long packetNum);
}
