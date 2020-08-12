package com.sukaiyi.byteutils.analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author sukaiyi
 * @date 2020/07/30
 */
public abstract class BaseByteAnalyzer<T> {
    public List<T> exec(File file) {
        Objects.requireNonNull(file);
        try {
            return exec(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<T> exec(String file) {
        Objects.requireNonNull(file);
        return exec(new File(file));
    }

    public List<T> exec(InputStream is) {
        Objects.requireNonNull(is);
        List<T> ts = new ArrayList<>();

        try {
            byte[] buffer = new byte[1024 * 256];
            int count;
            while ((count = is.read(buffer)) > 0) {
                decode(buffer, count, ts);
            }
            decode(buffer, count, ts);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ts;
    }

    /**
     * 将 bytes 解析为对象
     *
     * @param bytes  字节缓冲区
     * @param count  缓冲区中有效字节数量，count < 0 代表所有字节处理完毕
     * @param result 解析的对象
     */
    protected abstract void decode(byte[] bytes, int count, List<T> result);
}
