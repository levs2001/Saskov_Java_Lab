package com.java_polytech.manager;

import com.java_polytech.pipeline_interfaces.RC;

public class Main {
    static final int ARG_NUM_FOR_FILENAME = 0;

    public static void main(String[] args) {
        if(args[ARG_NUM_FOR_FILENAME] != null) {
            Manager manager = new Manager();
            RC rc = manager.run(args[ARG_NUM_FOR_FILENAME]);
            if (!rc.isSuccess()) {
                System.out.println("Error: " + rc.who.get() + ": " + rc.info);
            }
        } else {
            System.out.println(RC.RC_MANAGER_INVALID_ARGUMENT.info);
        }
    }
}