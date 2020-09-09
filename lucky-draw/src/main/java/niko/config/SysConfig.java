package niko.config;//import frank.config.interceptor.LoginInterceptor;
//import frank.config.web.RequestResponseBodyMethodProcessorWrapper;
import niko.config.web.RequestResponseBodyMethodProcessorWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SysConfig implements WebMvcConfigurer, InitializingBean{

    @Resource
    private RequestMappingHandlerAdapter adapter;

    // 和之前以 @ControllerAdvice + 实现 ResponseBodyAdvice 接口, 统一处理返回数据的包装
    // 之前的方式无法解决返回值为 null 的情况, 改用现在这种方式, 可以解决返回 null 包装为自定义类型
    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList(returnValueHandlers);
        for(int i=0; i<handlers.size(); i++){
            HandlerMethodReturnValueHandler handler = handlers.get(i);
            if(handler instanceof RequestResponseBodyMethodProcessor){
                handlers.set(i, new RequestResponseBodyMethodProcessorWrapper(handler));
            }
        }
        adapter.setReturnValueHandlers(handlers);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Controller 路径, 统一添加请求的路径前缀, 第二个参数表示是否添加
        // 表示所有 Controller 请求路径, 都要带 /api 的前缀
        configurer.addPathPrefix("api", c->true);
    }
}


