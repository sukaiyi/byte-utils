package com.sukaiyi.byteutils.analyzer;

import com.sukaiyi.byteutils.utils.MathUtils;
import com.sukaiyi.byteutils.utils.ReflectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/07/30
 */
public abstract class AbstractStateBasedFileAnalyzer<T> extends BaseByteAnalyzer<T> {

    private final ThreadLocal<AnalyzeState> analyzeStateLocal = ThreadLocal.withInitial(() -> {
        AnalyzeState analyzeState = new AnalyzeState();
        analyzeState.state = initState();
        analyzeState.buff = new byte[1024];
        return analyzeState;
    });
    private final ThreadLocal<Map<Class<?>, List<Block<?>>>> blockAlreadyDecodeLocal = ThreadLocal.withInitial(HashMap::new);

    @Override
    @SuppressWarnings("all")
    protected void decode(byte[] bytes, int count, List<T> result) {
        AnalyzeState analyzeState = analyzeStateLocal.get();
        Map<Class<?>, List<Block<?>>> blockAlreadyDecode = blockAlreadyDecodeLocal.get();
        if (count <= 0) {
            analyzeState.block = null;
            analyzeState.state = initState();
            analyzeState.bufPos = 0;
            analyzeState.read = 0L;
            analyzeState.size = 0L;
            blockAlreadyDecode.clear();
        }
        for (int i = 0; i < count; ) {
            Class<? extends Block> state = analyzeState.state;
            if (state == null) {
                break;
            }
            Block<?> block = analyzeState.block;
            if (block == null) {
                block = ReflectUtils.newInstance(state);
                blockAlreadyDecode.computeIfAbsent(state, key -> new ArrayList<>()).add(block);
                analyzeState.size = block.size(blockAlreadyDecode);
                analyzeState.read = 0L;
                analyzeState.bufPos = 0;
                analyzeState.block = block;
            }
            long left = analyzeState.size - analyzeState.read;
            if (left > analyzeState.buff.length) { // 如果剩余未读字节数大于了缓冲区大小，就不拷贝到缓冲区
                int read = MathUtils.min((long) count - i, analyzeState.size - analyzeState.read).intValue();
                i += read;
                analyzeState.read += read;
                if (analyzeState.read - analyzeState.size == 0) { // 数读取完了
                    block.decode(blockAlreadyDecode, bytes, i - read, read, true);
                    analyzeState.state = block.next(blockAlreadyDecode, result);
                    analyzeState.block = null;
                } else {
                    block.decode(blockAlreadyDecode, bytes, i - read, read, false);
                }
            } else { // 拷贝到缓冲区
                int read = MathUtils.min((long) count - i, (long) analyzeState.buff.length - analyzeState.bufPos, analyzeState.size - analyzeState.read).intValue();
                System.arraycopy(bytes, i, analyzeState.buff, analyzeState.bufPos, read);
                i += read;
                analyzeState.bufPos += read;
                analyzeState.read += read;

                if (analyzeState.read - analyzeState.size == 0) { // 数读取完了
                    block.decode(blockAlreadyDecode, analyzeState.buff, 0, analyzeState.bufPos, true);
                    analyzeState.bufPos = 0;
                    analyzeState.state = block.next(blockAlreadyDecode, result);
                    analyzeState.block = null;
                } else if (analyzeState.buff.length - analyzeState.bufPos == 0) { // 缓冲区满了
                    block.decode(blockAlreadyDecode, analyzeState.buff, 0, analyzeState.bufPos, false);
                    analyzeState.bufPos = 0;
                }
            }
        }
    }

    @SuppressWarnings("all")
    protected abstract Class<? extends Block> initState();

    @SuppressWarnings("all")
    private static final class AnalyzeState {
        private Class<? extends Block> state;
        private Block<?> block;
        private byte[] buff;
        private Integer bufPos = 0;
        private Long read; // 已经读取的字节数
        private Long size; // 当前 Block 总共需要读取的字节数
    }

}
