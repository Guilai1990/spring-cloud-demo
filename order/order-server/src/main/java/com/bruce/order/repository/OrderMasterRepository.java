package com.bruce.order.repository;

import com.bruce.order.dataobject.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {


}
