package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/cart/")
public class CarController {

    @Autowired
    private ICartService  iCartService;

        //2.购物车添加商品
        //
        ///cart/add.do
        //
        //http://localhost:8080/cart/add.do?productId=1&count=10
        //
        //请注意这个字段，超过数量会返回这样的标识"limitQuantity"
        //
        //失败的：LIMIT_NUM_FAIL 成功的：LIMIT_NUM_SUCCESS
        @RequestMapping(value = "add.do")
        @ResponseBody
        public ServerResponse<CartVo> add(HttpServletRequest request, Integer count, Integer productId){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.add(user.getId(),productId,count);
        }

        //3.更新购物车某个产品数量
        //
        ///cart/update.do
        @RequestMapping(value = "update.do")
        @ResponseBody
        public ServerResponse<CartVo> update(HttpServletRequest request, Integer count, Integer productId){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.update(user.getId(),productId,count);
        }

        //4.移除购物车某个产品
        //
        ///cart/delete_product.do
        @RequestMapping(value = "delete_product.do")
        @ResponseBody
        public ServerResponse<CartVo> deleteProduct(HttpServletRequest request,String productIds){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.delectProduct(user.getId(),productIds);
        }

        //1.购物车List列表
        //
        ///cart/list.do
        @RequestMapping(value = "list.do")
        @ResponseBody
        public ServerResponse<CartVo> list(HttpServletRequest request){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.getList(user.getId());
        }


        //8.购物车全选
        //
        ///cart/select_all.do
        @RequestMapping(value = "select_all.do")
        @ResponseBody
        public ServerResponse<CartVo> selectAll(HttpServletRequest request){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
        }
        //9.购物车取消全选
        //
        ///cart/un_select_all.do
        @RequestMapping(value = "un_select_all.do")
        @ResponseBody
        public ServerResponse<CartVo> unSelectAll(HttpServletRequest request){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
        }

        @RequestMapping(value = "select.do")
        @ResponseBody
        public ServerResponse<CartVo> select(HttpServletRequest request,Integer productId){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
        }

        @RequestMapping(value = "un_select.do")
        @ResponseBody
        public ServerResponse<CartVo> unSelect(HttpServletRequest request,Integer productId){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
            }
            return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
        }


        //7.查询在购物车里的产品数量
        //
        // /cart/get_cart_product_count.do
        @RequestMapping(value = "get_cart_product_count.do")
        @ResponseBody
        public ServerResponse<Integer> getCartProductCount(HttpServletRequest request){
//            User user=(User) session.getAttribute(Const.CURRENT_USER);
            String loginToken=CookieUtil.readLoginToken(request);
            if (StringUtils.isEmpty(loginToken)){
                return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
            }
            String userJsonStr=RedisShardedPoolUtil.get(loginToken);
            User user=JsonUtil.string2Obj(userJsonStr,User.class);
            if (user ==null){
                return ServerResponse.createBySuccess(0);
            }

            return iCartService.getCartProductCount(user.getId());
        }

}
