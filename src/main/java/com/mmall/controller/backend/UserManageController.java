package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private IUserService iUserService;


    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> userResponse=iUserService.login(username,password);
        if (userResponse.isSuccess()){
            User user=userResponse.getData();
            if (user.getRole()==Const.Role.ROLE_ADMIN){
                //说明是管理员 登录成功
//                session.setAttribute(Const.CURRENT_USER,user);
                //新增redis 共享 cookie  session 的方法
                CookieUtil.writeLoginToken(httpServletResponse,session.getId());
                RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(userResponse.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
                return userResponse;
            }else {
                return ServerResponse.createByErrorMessage("用户身份不符,无法登录");
            }
        }
        return userResponse;
    }
}
