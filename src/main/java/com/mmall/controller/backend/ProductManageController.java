package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping(value = "/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    //6.新增OR更新产品
    // /manage/product/save.do
    @RequestMapping(value = "save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始编写增加产品的业务逻辑

            return iProductService.saveOrUpdateProduct(product);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    @RequestMapping(value = "set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始编写增加产品的业务逻辑

            return iProductService.setSaleStatus(productId,status);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    //4.产品详情
    ///manage/product/detail.do
    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始编写 产品详细 的业务逻辑

            return iProductService.manageProductDetail(productId);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    //1.产品list
    //
    //http://localhost:8080/manage/product/list.do
    //
    ///manage/product/list.do

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始编写 产品list 的业务逻辑
            //包含  mybatis 分页插件

            return iProductService.getProductList(pageNum,pageSize);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    //2.产品搜索
    //
    //http://localhost:8080/manage/product/search.do?productName=p
    //
    //http://localhost:8080/manage/product/search.do?productId=1
    //
    ///manage/product/search.do
    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName, Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        System.out.println("======>"+productId);
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //开始编写 查询产品 的业务逻辑
            return iProductService.sreachProduct(productName,productId,pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }


    //3.图片上传
    //
    ///manage/product/upload.do
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(value = "upload_file",required = false)  MultipartFile file, HttpServletRequest request){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            String path=request.getSession().getServletContext().getRealPath("uploda");
            String targetFileName=iFileService.upload(file,path);
            String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map map=Maps.newHashMap();
            map.put("uri",targetFileName);
            map.put("url",url);
            return ServerResponse.createBySuccess(map);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }

    }

    //7.富文本上传图片
    //
    ///manage/product/richtext_img_upload.do
    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false)  MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap=Maps.newHashMap();
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请先登录管理员账号");
            return resultMap;
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //富文本编辑器中对于返回值有自己的规定  且不同的富文本编辑器有不同的约定
            //{
            //    "success": false,
            //    "msg": "error message",
            //    "file_path": "[real file path]"
            //}
            String path=request.getSession().getServletContext().getRealPath("uploda");
            String targetFileName=iFileService.upload(file,path);
            System.out.println("=======================>"+targetFileName);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败文件名为空");
                return resultMap;
            }
            String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);

            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else {
            resultMap.put("success",false);
            resultMap.put("msg","无操作权限，请登录管理员账号");
            return resultMap;
        }

    }

}
