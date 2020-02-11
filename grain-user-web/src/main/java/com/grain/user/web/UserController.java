package com.grain.user.web;


import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.user.UmsMember;
import com.grain.api.service.user.UserService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @RequestMapping(value = "/getAllUser", method = RequestMethod.GET)
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userService.getAllUser();
        if (ObjectUtils.isEmpty(umsMembers)) {
            return null;

        }
        return umsMembers;
    }
}
