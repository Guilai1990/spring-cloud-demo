package com.bruce.product.controller;

import com.bruce.product.dto.CartDTO;
import com.bruce.product.VO.ProductInfoVO;
import com.bruce.product.VO.ProductVO;
import com.bruce.product.VO.ResultVO;
import com.bruce.product.dataobject.ProductCategory;
import com.bruce.product.dataobject.ProductInfo;
import com.bruce.product.service.CategoryService;
import com.bruce.product.service.ProductService;
import com.bruce.product.utils.ResultVOUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 1. 查询所有在架的商品
     * 2. 获取类目type列表
     * 3. 查询类目
     * 4. 构造数据
     */
    @GetMapping("/list")
    public ResultVO<ProductVO> list() {
        // 1. 查询所有在架的商品
        List<ProductInfo> productInfoList = productService.findUpAll();

        // 2. 获取类目type列表
        List<Integer> categoryTypeList = productInfoList.stream().map(ProductInfo::getCategoryType).collect(Collectors.toList());

        // 3. 查询类目
        List<ProductCategory> categoryList = categoryService.findByCategoryTypeIn(categoryTypeList);

        // 4. 构造数据
        List<ProductVO> productVOList = new ArrayList<>();
        for (ProductCategory productCategory:categoryList) {
            ProductVO productVO = new ProductVO();
            productVO.setCategoryName(productCategory.getCategoryName());
            productVO.setCategoryType(productCategory.getCategoryType());

            List<ProductInfoVO> productInfoVOList = new ArrayList<>();
            for (ProductInfo productInfo:productInfoList) {
                if (productInfo.getCategoryType().equals(productCategory.getCategoryType())) {
                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    BeanUtils.copyProperties(productInfo, productInfoVO);
                    productInfoVOList.add(productInfoVO);
                }
            }
            productVO.setProductInfoVOList(productInfoVOList);
            productVOList.add(productVO);
        }

        return ResultVOUtil.success(productVOList);
    }

    /**
     * 获取商品列表（给订单服务使用）
     * @param productIdList
     * @return
     */
    @PostMapping("/listForOrder")
    public List<ProductInfo> listForOrderList(@RequestBody List<String> productIdList) {

        return productService.findList(productIdList);

    }

    @PostMapping("/decreaseStock")
    public void decreaseStock(@RequestBody List<CartDTO> cartDTOList) {
        productService.dereaseStock(cartDTOList);
    }


}
