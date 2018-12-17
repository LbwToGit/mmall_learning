package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public ServerResponse<User> login(String username, String password, HttpSession  session){
        //service --> mybatis --->dao
        ServerResponse<User> response=iUserService.login(username,password);
        if (response.isSuccess()){
            //登录成功  将登录对象存入session
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

 /*   public static void main(String[] args) {
        String password="123456";
        String md5password=MD5Util.MD5EncodeUtf8(password);
        System.out.println(md5password);
    }*/
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
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
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
            User user=(User) session.getAttribute(Const.CURRENT_USER);
            if (user==null){
                return ServerResponse.createByErrorMessage("用户未登录");
            }
            return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    //9.登录状态更新个人信息 /user/update_information.do
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        User currentUser=(User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId()); //复制登录用户ID
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response= iUserService.updateInformation(user);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    //10.获取当前登录用户的详细信息，并强制登录 /user/get_information.do
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录 status=10");
        }
        return iUserService.getInformation(user.getId());
    }














}
