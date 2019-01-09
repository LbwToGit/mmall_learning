package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;
    /**
     *
     * @param username 用户名
     * @param password 密码
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession  session, HttpServletResponse response){
        //service --> mybatis --->dao
        ServerResponse<User> userServerResponse=iUserService.login(username,password);
        if (userServerResponse.isSuccess()){
            //登录成功  将登录对象存入session
            //session.setAttribute(Const.CURRENT_USER,response.getData());
            //420B36A7040E8504A868FEE814F4C170
            CookieUtil.writeLoginToken(response,session.getId());
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(userServerResponse.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return userServerResponse;
    }

 /*   public static void main(String[] args) {
        String password="123456";
        String md5password=MD5Util.MD5EncodeUtf8(password);
        System.out.println(md5password);
    }*/
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request,HttpServletResponse response){

        String loginToken=CookieUtil.readLoginToken(request);
        CookieUtil.delLoginToken(request,response);
        RedisShardedPoolUtil.del(loginToken);
        //session.removeAttribute(Const.CURRENT_USER);

        return ServerResponse.createBySuccess();
    }

    //2.注册 /user/register.do
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    //校验 用户名和邮箱是否存在
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    //4.获取登录用户信息 /user/get_user_info.do
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request){

//        User user=(User) session.getAttribute(Const.CURRENT_USER);
        String loginToken=CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr=RedisShardedPoolUtil.get(loginToken);
        User user=JsonUtil.string2Obj(userJsonStr,User.class);

        if (user!=null){
            return ServerResponse.createBySuccess(user);  //用户不为空 保存至服务响应对象
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
    }

    //5.忘记密码 /user/forget_get_question.do
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    //6.提交问题答案 /user/forget_check_answer.do
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    //7.忘记密码的重设密码 /user/forget_reset_password.do
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    //8.登录中状态重置密码 /user/reset_password.do
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest request,String passwordOld,String passwordNew){
//          User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user==null){
                return ServerResponse.createByErrorMessage("用户未登录");
            }
            return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    //9.登录状态更新个人信息 /user/update_information.do
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpServletRequest request,User user){
//        User currentUser=(User) session.getAttribute(Const.CURRENT_USER);
        String loginToken=CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr=RedisShardedPoolUtil.get(loginToken);
        User currentUser=JsonUtil.string2Obj(userJsonStr,User.class);
        if (currentUser==null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId()); //复制登录用户ID
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response= iUserService.updateInformation(user);
        if (response.isSuccess()){
            //session.setAttribute(Const.CURRENT_USER,response.getData());
            RedisShardedPoolUtil.setEx(loginToken, JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

        }
        return response;
    }
    //10.获取当前登录用户的详细信息，并强制登录 /user/get_information.do
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpServletRequest request){
//        User user=(User) session.getAttribute(Const.CURRENT_USER);
        String loginToken=CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr=RedisShardedPoolUtil.get(loginToken);
        User user=JsonUtil.string2Obj(userJsonStr,User.class);
        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录 status=10");
        }
        return iUserService.getInformation(user.getId());
    }














}
