package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;


@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    //日志对象
    private Logger logger=LoggerFactory.getLogger(CategoryServiceImpl.class);


    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId!=null && StringUtils.isNotBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); //标记此分类可用

        int rowCount=categoryMapper.insert(category);
        if (rowCount>0){
            return ServerResponse.createBySuccess("增加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if (categoryId!=null && StringUtils.isNotBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount>0){
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }


    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id 和 子节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> categoryIdList=Lists.newArrayList();
        if (categoryId !=null){
            for (Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }


    //递归算法  -------------  自己调自己
    // 算出子节点 和自己的节点

    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){

        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            System.out.println(category.getId());
            System.out.println(category.getName());
            categorySet.add(category);
        }
        //查找子节点  递归算法 要有退出条件
        //mybatis 查询集合时就算查不到数据也不会返回NULL
        System.out.println("查找子节点的ID "+categoryId);
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId); //查找子节点
        for (Category categoryItem : categoryList){
            System.out.println("遍历时节点ID： "+categoryItem.getId());
            System.out.println("遍历时节点名称： "+categoryItem.getName());
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
