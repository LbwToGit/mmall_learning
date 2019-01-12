package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();

    List<Product> selectByNameAndProductId(@Param(value = "productName") String productName, @Param(value = "productId")Integer productId);

    List<Product> selectByNameAndCategoryIds(@Param(value = "productName") String productName,@Param(value = "categoryIdList")List<Integer> categoryIdList);


    //这里一定要用Integer 因为int不能为null  考虑到很多产品已经删除的情况
    Integer selectStockByProductId(Integer id);
}