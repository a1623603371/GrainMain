package com.grain.user.controller;

import com.grain.user.bean.UmsMember;
import com.grain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
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
