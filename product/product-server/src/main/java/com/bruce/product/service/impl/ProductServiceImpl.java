package com.bruce.product.service.impl;

import com.bruce.product.dto.CartDTO;
import com.bruce.product.dataobject.ProductInfo;
import com.bruce.product.enums.ProductStatusEnums;
import com.bruce.product.enums.ResultEnum;
import com.bruce.product.exception.ProductException;
import com.bruce.product.repository.ProductInfoRepository;
import com.bruce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class  ProductServiceImpl implements ProductService {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Override
    public List<ProductInfo> findUpAll() {
        return productInfoRepository.findByProductStatus(ProductStatusEnums.UP.getCode());
    }

    @Override
    public List<ProductInfo> findList(List<String> productIdList) {
        return productInfoRepository.findByProductIdIn(productIdList);
    }

    @Override
    @Transactional
    public void dereaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO: cartDTOList) {
          Optional<ProductInfo> productInfoOptional =  productInfoRepository.findById(cartDTO.getProductId());

          // 判断商品是否存在
          if (!productInfoOptional.isPresent()) {
              throw new ProductException(ResultEnum.PRODUCT_NOT_EXIST);
          }

          // 判断库存是否足够
          ProductInfo productInfo = productInfoOptional.get();
          Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
          if (result < 0) {
              throw new ProductException(ResultEnum.PRODUCT_STOCK_ERROR);
          }

          productInfo.setProductStock(result);
          productInfoRepository.save(productInfo);
        }
    }
}
