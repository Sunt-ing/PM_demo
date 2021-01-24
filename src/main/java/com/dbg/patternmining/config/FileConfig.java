package com.dbg.patternmining.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * File path configuration
 */
@Component
public class FileConfig {
    @Value("${file.dataset-path}")
    private String datasetPath;

    public String getDatasetPath() {
        return datasetPath;
    }
}