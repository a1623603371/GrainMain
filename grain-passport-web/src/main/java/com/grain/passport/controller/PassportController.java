package com.grain.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.grain.api.bean.enume.GenderEnum;
import com.grain.api.bean.user.UmsMember;
import com.grain.api.service.user.UserService;
import com.grain.utils.JwtUtil;
import com.grain.utlis.HttpClientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Administrator
 */
@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("/vlogin")
    public String vLogin(String code, HttpServletRequest request) {
        //授权码换取access_token
        //换取access_token
        //access_secret=c5df0fc655b6572dc53cec25acc24bad
        //client_id=600920342
        String vLoginUrl = "https://api.weibo.com/oauth2/access_token";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "600920342");
        paramMap.put("client_secret", "c5df0fc655b6572dc53cec25acc24bad");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://127.0.0.1:8800/vlogin");
        //code 授权有效期可以使用，没新生成一次code，说明用户重新对第三方数据进行了重新授权，之前的access_token和授权码全部过期
        paramMap.put("code", code);
        String access_token_json = HttpClientUtil.doPost(vLoginUrl, paramMap);
        Map<String, Object> access_map = JSON.parseObject(access_token_json, Map.class);
        //access_token换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");
        String show_user_url = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String user_json = HttpClientUtil.doGet(show_user_url);
        Map<String, Object> user_map = JSON.parseObject(user_json, Map.class);
        //将用户信息保存到数据库，用户类型设置为微博
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String) user_map.get("screen_name"));
        GenderEnum genderEnum = GenderEnum.UNKNOWN;
        String gender = (String) user_map.get("gender");
        if ("m".equals(gender)) {
            genderEnum = GenderEnum.MAN;
        } else if ("f".equals(gender)) {
            genderEnum = GenderEnum.WOMAN;
        }
        umsMember.setGender(genderEnum);
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);
        if (ObjectUtils.isEmpty(umsMemberCheck)) {
            umsMember = userService.addOauthUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
        }
        //生成jwt的token，并且重定向到首页，携带该token
        String token = null;
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", memberId);
        userMap.put("nickname", nickname);
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip)) {
            //request中获取ip
            ip = request.getRemoteAddr();
            if (StringUtils.isEmpty(ip)) {
                ip = "127.0.0.1";
            }
        }
        //生成jwttoken
        token = JwtUtil.encode("grain", userMap, ip);
        //将token备份一份到缓存中
        userService.addUserToken(token, memberId);
        return "redirect:http://127.0.0.1:8500/index?token=" + token;
    }


    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, String currIp, HttpServletRequest request) {

        //通过jwt验证token真假
        Map<String, String> map = new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "grainmain", currIp);
        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));

        } else {
            map.put("status", "fail");
        }


        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = "";
        //调用服务验证用户民和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null) {
            //登录成功
            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);
            //通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip)) {
                //从request获取ip
                ip = request.getRemoteAddr();
                if (StringUtils.isEmpty(ip)) {
                    ip = "127.0.0.1";
                }
            }
            //按照设计的算法进行加密，生成token
            token = JwtUtil.encode("grainMain", userMap, ip);
            //存放一份放入缓存
            userService.addUserToken(token, umsMemberLogin.getId());


        } else {
            //等入失败
            token = "fail";
        }
        return token;
    }

    @RequestMapping("/index")
    public String index(String ReturnUrl, ModelMap modelMap) {
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";

    }


}
