package com.lambda.easydqc.utils;

import java.io.*;
import java.util.Properties;

public class FileUtils {
    /**
     * 加载properties文件
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(File propertiesFile) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(propertiesFile));
        Properties p = new Properties();
        p.load(in);
        return p;
    }


    public static Properties loadProperties(String propertiesFilePath) throws IOException {
        return loadProperties(new File(propertiesFilePath));
    }
}
