package com.bruce.order.service.impl;


import com.bruce.order.client.ProductClient;
import com.bruce.order.dataobject.OrderDetail;
import com.bruce.order.dataobject.OrderMaster;
import com.bruce.order.dataobject.ProductInfo;
import com.bruce.order.dto.CartDTO;
import com.bruce.order.dto.OrderDTO;
import com.bruce.order.enums.OrderStatusEnum;
import com.bruce.order.enums.PayStatusEnum;
import com.bruce.order.repository.OrderDetailRepository;
import com.bruce.order.repository.OrderMasterRepository;
import com.bruce.order.service.OrderService;
import com.bruce.order.utils.KeyUtil;
import com.bruce.product.common.ProductInfoOutput;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    @Transactional
    public OrderDTO create(OrderDTO orderDTO) {

        String orderId = KeyUtil.genUniqueKey();

        // 2. 查询商品信息（调用商品服务）
        List<String> productIdList = orderDTO.getOrderDetailList().stream()
                .map(OrderDetail::getProductId)
                .collect(Collectors.toList());
        List<ProductInfo> productInfoList = productClient.listForOrder(productIdList);

        // 3. 计算总价
        BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            for (ProductInfo productInfo: productInfoList) {
                if (productInfo.getProductId().equals(orderDetail.getProductId())) {
                    // 单价*数量
                   orderAmount = productInfo.getProductPrice()
                           .multiply(new BigDecimal(orderDetail.getProductQuantity()))
                           .add(orderAmount);
                   BeanUtils.copyProperties(productInfo, orderDetail);
                   orderDetail.setOrderId(orderId);
                   orderDetail.setDetailId(KeyUtil.genUniqueKey());
                   //订单详情入库
                    orderDetailRepository.save(orderDetail);
                }
            }

    }

        // 4. 扣库存（调用商品服务）
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
                .map(e -> new CartDTO(e.getProductId(), e.getProductQuantity()))
                .collect(Collectors.toList());
                productClient.decreaseStock(cartDTOList);


        // 5. 订单入库
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
        BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(orderAmount);
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        orderMasterRepository.save(orderMaster);
        return orderDTO;
    }
}
