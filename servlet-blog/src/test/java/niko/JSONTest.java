package niko;

import com.fasterxml.jackson.databind.ObjectMapper;
import niko.model.Article;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONTest {

    /**
     * 模拟 http 接受 http 请求的 json 数据:json 格式解析/反序列化为 java 对象
     * 响应 http 的 json 格式数据:java 对象封装/序列化为 json 数据类型
     * 使用 jackson 框架做 json 的序列化/反序列化
     */


    @Test
    public void t1() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Article a1   = new Article();
        a1.setId(1);
        a1.setTitle("标题1");
        a1.setContent("内容1");
        Article a2   = new Article();
        a2.setId(1);
        a2.setTitle("标题2");
        a2.setContent("内容2");
        List<Article> articles = new ArrayList<>();
        articles.add(a1);
        articles.add(a2);

        String s = mapper.writeValueAsString(articles);
        System.out.println(s);
//        Map m = mapper.readValue(s, Map.class);
//        System.out.println(m);
    }
}
