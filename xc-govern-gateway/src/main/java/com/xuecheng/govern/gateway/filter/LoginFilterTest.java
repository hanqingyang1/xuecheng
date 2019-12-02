package com.xuecheng.govern.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

//@Component
public class LoginFilterTest extends ZuulFilter {

    @Override
    public String filterType() {
        /**
         * pre：请求在被路由之前执行
         * routing：在路由请求时调用
         * post：在routing和errror过滤器之后调用
         * error：处理请求时发生错误调用
         */
        return "pre";
    }

    /**
     * filterOrder : 此方法返回整型数值，通过此数值来定义过滤器的执行顺序，数字越小优先级越高。
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * shouldFilter：返回一个Boolean值，判断该过滤器是否需要执行。返回true表示要执行此过虑器，否则不执行。
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return false;
    }

    /**
     *  run：过滤器的业务逻辑
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        return null;
    }
}
