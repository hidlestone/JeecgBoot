package org.jeecg.modules.test.seata.tcc.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.entity.SeataOrder;
import org.jeecg.modules.test.seata.order.enums.OrderStatus;
import org.jeecg.modules.test.seata.order.mapper.SeataOrderMapper;
import org.jeecg.modules.test.seata.tcc.service.TccOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class TccOrderServiceImpl implements TccOrderService {

    @Resource
    private SeataOrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrderPrepare(BusinessActionContext actionContext, PlaceOrderRequest request) {
        String orderNo = request.getOrderNo();
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer count = request.getCount();
        log.info("收到下单请求,用户:{}, 订单号:{},商品:{},数量:{}", userId, orderNo, productId, count);

        SeataOrder order = SeataOrder.builder()
                .orderNo(orderNo)
                .userId(userId)
                .productId(productId)
                .status(OrderStatus.INIT)
                .count(count)
                .build();
        orderMapper.insert(order);
        log.info("订单一阶段生成，等待扣库存付款中");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrderCommit(BusinessActionContext actionContext) {
        PlaceOrderRequest request = JSON.parseObject(JSON.toJSONString(actionContext.getActionContext("orderRequest")), PlaceOrderRequest.class);
        LambdaUpdateWrapper<SeataOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataOrder::getOrderNo, request.getOrderNo());
        updateWrapper.set(SeataOrder::getStatus, OrderStatus.SUCCESS);
        orderMapper.update(null, updateWrapper);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrderCancel(BusinessActionContext actionContext) {
        PlaceOrderRequest request = JSON.parseObject(JSON.toJSONString(actionContext.getActionContext("orderRequest")), PlaceOrderRequest.class);
        LambdaUpdateWrapper<SeataOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataOrder::getOrderNo, request.getOrderNo());
        updateWrapper.set(SeataOrder::getStatus, OrderStatus.FAIL);
        orderMapper.update(null, updateWrapper);
        return true;
    }
}
