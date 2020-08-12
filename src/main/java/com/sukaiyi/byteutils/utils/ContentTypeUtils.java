package com.sukaiyi.byteutils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeUtils {


    private static final String UNKNOWN = "application/octet-stream";

    private static Map<String, String> contentTypeSuffixMap;

    private ContentTypeUtils() {

    }

    /**
     * 根据文件名选择合适的 Content-Type
     *
     * @param fileName 文件名
     * @return Content-Type
     */
    public static String chooseForFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ".";
        }
        String suffix = fileName.substring(lastDotIndex);

        if (contentTypeSuffixMap != null) {
            return contentTypeSuffixMap.getOrDefault(suffix, UNKNOWN);
        }
        synchronized (ContentTypeUtils.class) {
            if (contentTypeSuffixMap != null) {
                return contentTypeSuffixMap.getOrDefault(suffix, UNKNOWN);
            }
            return initContentTypeSuffixMap().getOrDefault(suffix, UNKNOWN);
        }
    }

    private static Map<String, String> initContentTypeSuffixMap() {
        contentTypeSuffixMap = new HashMap<>();

        try (InputStream is = ContentTypeUtils.class.getClassLoader().getResourceAsStream("content-type.map")) {
            if (is == null) {
                return contentTypeSuffixMap;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(":");
                contentTypeSuffixMap.put(data[0], data[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentTypeSuffixMap;
    }

}
