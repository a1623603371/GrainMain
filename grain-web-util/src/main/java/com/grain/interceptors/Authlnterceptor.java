package com.grain.interceptors;

import com.alibaba.fastjson.JSON;
import com.grain.annotations.LoginRequrired;
import com.grain.utils.CookieUtil;
import com.grain.utlis.HttpClientUtil;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class Authlnterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码
        //判断被拦截的请求的访问的方法的注解（是否需要拦截）
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequrired requrired = handlerMethod.getMethodAnnotation(LoginRequrired.class);
        //是否拦截
        if (ObjectUtils.isEmpty(requrired)) {
            return true;
        }
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (!StringUtils.isEmpty(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (!StringUtils.isEmpty(newToken)) {
            token = newToken;
        }
        //是否必须登录
        boolean loginSuccess = requrired.loginSuccess(); //获得该请必须登录成功
        //调用认证中心进行效验
        String success = "fall";
        Map<String, String> successMap = new HashMap<>();
        if (!StringUtils.isEmpty(token)) {
            //通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip)) {
                //从request中获取ip
                ip = request.getRemoteAddr();
                if (StringUtils.isEmpty(ip)) {
                    ip = "127.0.0.1";
                }
            }
            String successJson = HttpClientUtil.doGet("http://127.0.0.1:8800/verify?token=" + token + "&currentIp=" + ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");

        }
        if (loginSuccess) {
            //必须成功登录才能使用
            if (!success.equals("success")) {
                //重定向会passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://127.0.0.1:8800/index?RentrnUrl=" + requestURL);
                return false;
            }
            //需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            //验证通过  覆盖cookie中的token
            if (!StringUtils.isEmpty(token)) {
                CookieUtil.setCookieValue(request, response, "oldToken", token, 60 * 60 * 2, true);
            }
        } else {
            //没有登录也可访问，但是必须验证
            if (success.equals("success")) {
                //需要将token携带的用户写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if (!StringUtils.isEmpty(token)) {
                    CookieUtil.setCookieValue(request, response, "oldToken", token, 60 * 60 * 2, true);
                }

            }
        }
        return true;
    }
}
