package niko.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JSONUtil {

    private static volatile ObjectMapper MAPPER;

    private static ObjectMapper getMAPPER() {
        if (MAPPER == null) {
            synchronized (JSONUtil.class) {
                if (MAPPER == null) {
                    MAPPER = new ObjectMapper();
                }
            }
        }
        return MAPPER;
    }

    // 对象序列化为 JSON 对象
    public static String serialize(Object o) {
        try {
            return getMAPPER().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化对象出错: " + o, e);
        }
    }

    // 反序列化输入流中的 JSON 字符串为 java 对象
    // http 请求发送 json 数据,请求头 Content - Type = application / json
    // HttpServletRequest 获取 json 字符串,只能通过输入流获取
    public static <T> T deserialize(InputStream is, Class<T> valueType) {
        try {
            return getMAPPER().readValue(is, valueType);
        } catch (IOException e) {
            throw new RuntimeException("反序列化 json 字符串出错", e);
        }
    }
}
