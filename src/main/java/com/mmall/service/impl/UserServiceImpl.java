package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Service(value = "iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount=userMapper.checkUsername(username);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        System.out.println("=======================================");
        // TODO 密码登录MD5
        String md5Password=MD5Util.MD5EncodeUtf8(password);
        User user=userMapper.selectLogin(username,md5Password);
        if (user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY); //登录成功 将密码置空

        return ServerResponse.createBySuccess("登陆成功",user); //将用户保存至泛型
    }

    public ServerResponse<String> register(User user){
        ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;

        }
        user.setRole(Const.Role.ROLE_CUSTOMER); //设置用户等级 通过Const 的内部接口实现
        //MD5 进行密码加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount=userMapper.insert(user);  //新增用户
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("新增用户失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }
    //校验 用户名和邮箱是否存在
    public ServerResponse<String> checkValid(String str,String type){
            if (StringUtils.isNotBlank(type)){
                //开始验证
                if (Const.USERNAME.equals(type)){
                    int resultCount=userMapper.checkUsername(str);
                    if (resultCount>0){
                        return ServerResponse.createByErrorMessage("用户名已存在");
                    }
                }
                if (Const.EMAIL.equals(type)){
                    int resultCount=userMapper.checkEmail(str);
                    if (resultCount>0){
                        return ServerResponse.createByErrorMessage("email已存在");
                    }
                }
            }else {
                return ServerResponse.createByErrorMessage("参数错误");
            }
            return ServerResponse.createBySuccess("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");

        }
        //如果用户名存在  开始查找问题
        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("用户问题为空");
    }

    //验证问题和答案
    public ServerResponse<String> checkAnswer(String username, String question,String answer){
        int resultCount=userMapper.checkAnswer(username,question,answer);
        if (resultCount>0){
            //用户问题和用户答案是这个用户的并且答案是对的
            String forgetToken=UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken); //存入本地缓存
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    //7.忘记密码的重设密码
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username); //获取本地缓存中的 token
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            //密码记得用MD5加密
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);

            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew,User user){
            //防止横向越权 要验证旧密码是这个用户的  因为单单用旧密码countde的话 只要有重复的密码就会大于1
            int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
            if (resultCount==0){
                return ServerResponse.createByErrorMessage("旧密码错误");
            }
            user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
            int updateCount=userMapper.updateByPrimaryKeySelective(user);
            if (updateCount>0){
                return ServerResponse.createBySuccess("密码修改成功");
            }
            return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user){
        //username不能更新
        //验证email 新email是否已经存在，并且如果email相同的话不能是当前用户的
        int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount>0){
            return ServerResponse.createByErrorMessage("email已被其它用户占用，请更换email");

        }
        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setPhone(user.getPhone());

        int updateCount=userMapper.updateByPrimaryKeySelective(updateUser);

        if (updateCount>0){
            return  ServerResponse.createBySuccess("个人信息更新成功",updateUser);

        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");



    }

    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if (user==null) {
            return  ServerResponse.createByErrorMessage("用户不存在");

        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend

    /**
     * 校验是否为管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if (user != null && user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }



























}
