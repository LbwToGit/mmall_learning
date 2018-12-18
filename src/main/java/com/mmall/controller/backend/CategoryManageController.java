package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.spi.ServiceRegistry;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;
    //2.增加节点
    //
    ///manage/category/add_category.do

    @RequestMapping(value = "add_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始处理分类逻辑
            return iCategoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }

    }

    //3.修改品类名字
    ///manage/category/set_category_name.do
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess()){
            //更新CategoryName
            return iCategoryService.updateCategoryName(categoryId,categoryName);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }

    }

    //4.获取品类子节点(平级)
    // /manage/category/get_category.do
    @RequestMapping(value = "get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //查询子节点的信息 不递归 保持平级
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    //4.获取当前分类id及递归子节点categoryId
    // /manage/category/get_deep_category.do
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //查询当前节点的ID  和递归子节点的ID
            // 0--->1000--->100000
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
            
            
        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

}
