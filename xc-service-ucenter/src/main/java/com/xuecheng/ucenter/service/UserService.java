package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private XcUserRepository xcUserRepository;

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    public XcUser findUserByUsername(String username){
        return xcUserRepository.findXcUserByUsername(username);
    }

    /**
     * 根据用户名获取用户扩展信息
     * @param username
     * @return
     */
    public XcUserExt getXcUserExt(String username){
        //获取用户基本信息
        XcUser xcUser = findUserByUsername(username);
        if(xcUser == null){
            return null;
        }

        String userId = xcUser.getId();
        //根据用户id查询公司id
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        String companyId = null;
        if(xcCompanyUser != null){
            companyId = xcCompanyUser.getCompanyId();
        }

        //封装用户扩展信息
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(companyId);

        return xcUserExt;
    }

}
