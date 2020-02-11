package com.grain.utils;


import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CookieUtil {
    /**
     * 获取cookie的值
     *
     * @param request
     * @param cookieName
     * @param isDecoder
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, Boolean isDecoder) {
        Cookie[] cookies = request.getCookies();
        if (ObjectUtils.isEmpty(cookies) || StringUtils.isEmpty(cookieName)) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    //如果是涉及中文
                    if (isDecoder) {
                        retValue = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                    }
                } else {
                    retValue = cookies[i].getValue();
                }
                break;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 设置cookie值
     *
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxage
     * @param isEncode
     */
    public static void setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxage, Boolean isEncode) {
        try {
            if (cookieValue == null) {
                cookieName = "";
            } else if (isEncode) {
                cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxage >= 0) {
                cookie.setMaxAge(cookieMaxage);
            }
            if (!ObjectUtils.isEmpty(request)) {//设置域名cookie
                cookie.setDomain(getDomainName(request));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 设置cookie的主域名
     *
     * @param request
     * @return
     */
    private static final String getDomainName(HttpServletRequest request) {
        String domainName = null;
        String serverName = request.getRequestURL().toString();//获取浏览器地址栏url
        if (ObjectUtils.isEmpty(serverName)) {
            domainName = "";
        } else {
            serverName = serverName.toLowerCase();
            serverName = serverName.substring(7);
            final int end = serverName.indexOf("/");
            serverName = serverName.substring(0, end);
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3) {
                domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
            } else if (len <= 3 && len > 1) {
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }
        }
        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        System.out.println("domainName=" + domainName);
        return domainName;

    }

    /**
     * 将cookie的值按照key删除
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        setCookieValue(request, response, cookieName, null, 0, false);

    }
}
