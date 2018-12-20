package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    //2.购物车添加商品
    //
    ///cart/add.do
    //
    //http://localhost:8080/cart/add.do?productId=1&count=10
    //
    //请注意这个字段，超过数量会返回这样的标识"limitQuantity"
    //
    //失败的：LIMIT_NUM_FAIL 成功的：LIMIT_NUM_SUCCESS

    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        if (productId == null || count==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart=cartMapper.selectCarByUserIdAndProductId(userId,productId);
        if (cart ==null){
            //无此记录 就加入购物车
            Cart cartItem=new Cart();
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setQuantity(count);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{
            //这个商品已经在该用户的购物车里了
            //相当于商品重复添加购物车  商品数量就要加一
            count=cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);

        }
        return this.getList(userId);
    }

    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if (productId == null || count==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart=cartMapper.selectCarByUserIdAndProductId(userId,productId);
        if (cart !=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.getList(userId);
    }

    public ServerResponse<CartVo> delectProduct(Integer userId,String productIds){
        List<String> productIdList=Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productIdList);
        return this.getList(userId);
    }

    public ServerResponse<CartVo> getList(Integer userId){
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUnCheckedProduct(userId,productId,checked);
        return this.getList(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if (userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }



    //todo 库存和数据的校验
    //计算 扩展的购物车
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo=new CartVo();

        List<Cart> cartList=cartMapper.selectCartByUserId(userId); //查询此用户所有的购物车

        List<CartProductVo> cartProductVoList=Lists.newArrayList();
        //初始化 总价
        //  todo 数字计算是如何处理丢失精度的问题？
        BigDecimal cartTotalPrice=new BigDecimal("0");  //这个购物车的总价
        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem:cartList){
                //开始组装 CartProductVo 对象
                //购物车部分
                CartProductVo cartProductVo=new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                //产品部分
                Product product=productMapper.selectByPrimaryKey(cartProductVo.getProductId());
                if (product !=null){
                    //开始组装 产品部分
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductChecked(product.getStock());
                    //开始  判断库存
                    int buyLimitCount=0;
                    if (product.getStock() >= cartItem.getQuantity()){
                        //库存充足时
                        buyLimitCount=cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        buyLimitCount=product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        //购物车中更新有效库存
                        Cart cartForQuantity=new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);

                    }
                    cartProductVo.setQuantity(buyLimitCount);


                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());

                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选 增加到购物车的总价中
                    //如果传个 不存在的 产品ID 过来 cartProductVo.getProductTotalPrice() 就为空
                    //调用doubleValue（）就会报空值针 异常  用三元运算符就判断传参
                    cartTotalPrice=BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice()==null?0.0:cartProductVo.getProductTotalPrice().doubleValue());

                }
                cartProductVoList.add(cartProductVo);  //这个购物车就做好了
            }
        }
        //组装 购物车
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getCheckAllStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getCheckAllStatus(Integer userId){
        if (userId==null){
            return false;
        }
        return cartMapper.selectCartProductCheckStatusByUserid(userId)==0;
    }


}
