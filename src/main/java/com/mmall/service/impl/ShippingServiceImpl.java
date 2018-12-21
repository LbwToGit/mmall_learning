package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount=shippingMapper.insert(shipping);
        //todo  新增后立马获取MySQL自动生成的主键
        if (rowCount>0){
            Map date=Maps.newHashMap();
            date.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功",date);
        }
        return ServerResponse.createByErrorMessage("新增地址失败");
    }



    public ServerResponse del(Integer userId,Integer shippingId){

        // 横向越权！！！！
       int rowCount=shippingMapper.deleteByUserIdAndSippingId(userId,shippingId);
       if (rowCount>0){
           return  ServerResponse.createBySuccess("删除地址成功");
       }else {
           return ServerResponse.createBySuccessMessage("删除地址失败");
       }
    }


    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount=shippingMapper.updateBySipping(shipping);

        if (rowCount>0){

            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId,Integer shippingId){
        Shipping shipping=shippingMapper.selectByUserIdAndShippingId(userId,shippingId);
        if (shipping ==null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess(shipping);
    }


    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList=shippingMapper.selectByUserId(userId);
        PageInfo pageInfo=new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
