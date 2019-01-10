package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/springsession/")
public class UserSpringSessionController {
    @Autowired
    private IUserService iUserService;
    /**
     *
     * @param username 用户名
     * @param password 密码
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession  session, HttpServletResponse response){
        //service --> mybatis --->dao
        ServerResponse<User> userServerResponse=iUserService.login(username,password);
        if (userServerResponse.isSuccess()){
            //登录成功  将登录对象存入session
            session.setAttribute(Const.CURRENT_USER,userServerResponse.getData());
            //420B36A7040E8504A868FEE814F4C170
//            CookieUtil.writeLoginToken(response,session.getId());
//            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(userServerResponse.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return userServerResponse;
    }

 /*   public static void main(String[] args) {
        String password="123456";
        String md5password=MD5Util.MD5EncodeUtf8(password);
        System.out.println(md5password);
    }*/
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request,HttpServletResponse response,HttpSession  session){

//        String loginToken=CookieUtil.readLoginToken(request);
//        CookieUtil.delLoginToken(request,response);
//        RedisShardedPoolUtil.del(loginToken);
        session.removeAttribute(Const.CURRENT_USER);

        return ServerResponse.createBySuccess();
    }


    //4.获取登录用户信息 /user/get_user_info.do
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request,HttpSession  session){

        User user=(User) session.getAttribute(Const.CURRENT_USER);
//        String loginToken=CookieUtil.readLoginToken(request);
//        if (StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
//        }
//        String userJsonStr=RedisShardedPoolUtil.get(loginToken);
//        User user=JsonUtil.string2Obj(userJsonStr,User.class);

        if (user!=null){
            return ServerResponse.createBySuccess(user);  //用户不为空 保存至服务响应对象
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
    }















}
