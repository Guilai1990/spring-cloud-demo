package com.bruce.product.service;

import com.bruce.product.dataobject.ProductCategory;

import java.util.List;

/**
 * 类目
 */

public interface CategoryService {

    List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList);
}
