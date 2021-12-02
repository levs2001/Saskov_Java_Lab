package com.java_polytech.universal_config;

import com.java_polytech.pipeline_interfaces.RC;

public interface ISyntaxAnalyzer {
    RC readConfig(String filename);
    String getParam(String lexeme);
}
