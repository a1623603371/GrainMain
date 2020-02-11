package com.grain.api.service.user;


import com.grain.api.bean.user.UmsMember;
import com.grain.api.bean.user.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    public List<UmsMember> getAllUser();

    void addUserToken(String token, String memberId);

    UmsMember login(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember umsCheck);

    UmsMember addOauthUser(UmsMember umsMember);

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
