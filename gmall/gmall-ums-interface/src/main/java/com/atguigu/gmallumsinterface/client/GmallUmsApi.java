package com.atguigu.gmallumsinterface.client;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryMember(@RequestParam("username") String username, @RequestParam("password") String password);

    @GetMapping("ums/memberreceiveaddress/{userId}")
    Resp<List<MemberReceiveAddressEntity>> queryAddressByUserId(@PathVariable("userId") Long userId);

    @GetMapping("ums/member/info/{id}")
    Resp<MemberEntity> queryMemberById(@PathVariable("id") Long id);
}
