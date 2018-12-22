package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {
    private static final Logger logger=LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;
    //1.支付
    //
    ///order/pay.do
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //todo 将生成的二维码地址上传到FTP 并返回地址给前端展示
        String path=request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),orderNo,path);
    }

    //3.支付宝回调
    //
    //参考支付宝回调文档： https://support.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.mFogPC&treeId=193&articleId=103296&docType=1
    //这是支付宝 发出的回调
    ///order/alipay_callback.do
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        System.out.println("=========支付宝开始回调============");
        System.out.println("=========支付宝开始回调============");
        System.out.println("=========支付宝开始回调============");
        System.out.println("=========支付宝开始回调============");
        Map<String,String> parms=Maps.newHashMap();
        Map requesrParams=request.getParameterMap();
        for (Iterator iterator=requesrParams.keySet().iterator();iterator.hasNext();){
            String name=(String) iterator.next();
            String[] values=(String[]) requesrParams.get(name);
            String valuesStr="";
            for (int i=0;i<values.length;i++){
                valuesStr=i==(values.length-1)?valuesStr+values[i]:valuesStr+values[i]+",";
            }
            parms.put(name,valuesStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",parms.get("sign"),parms.get("trade_status"),parms.toString());

        //非常重要的东西   验证回调是不是支付宝发的  并且还要避免重复通知

        //第一步： 在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数。
        parms.remove("sign_type");

        try {
            boolean alipayRSACheckedV2= AlipaySignature.rsaCheckV2(parms,Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，多次请求后将自动报警");
            }
        }catch (AlipayApiException e){
            logger.error("支付宝回调异常："+e);
        }
        //todo 验证各种数据


        ServerResponse serverResponse=iOrderService.aliCallback(parms);
        if (serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //2.查询订单支付状态
    //
    ///order/query_order_pay_status.do
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse=iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if (serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
        //return iOrderService.pay(user.getId(),orderNo,path);
    }
}
