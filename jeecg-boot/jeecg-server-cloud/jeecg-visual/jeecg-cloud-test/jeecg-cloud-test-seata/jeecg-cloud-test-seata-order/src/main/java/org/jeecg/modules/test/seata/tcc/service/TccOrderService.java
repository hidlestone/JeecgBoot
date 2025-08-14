package org.jeecg.modules.test.seata.tcc.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;

@LocalTCC
public interface TccOrderService {

    /**
     * 创建订单准备
     *
     * @param orderRequest
     * @param actionContext 业务动作上下文
     * @return
     */
    @TwoPhaseBusinessAction(name = "createOrderTcc", commitMethod = "createOrderCommit", rollbackMethod = "createOrderCancel")
    Boolean createOrderPrepare(BusinessActionContext actionContext,
                               @BusinessActionContextParameter(paramName = "orderRequest") PlaceOrderRequest orderRequest);

    /**
     * 创建订单提交
     *
     * @param actionContext 业务动作上下文
     * @return
     */
    Boolean createOrderCommit(BusinessActionContext actionContext);

    /**
     * 创建订单取消
     *
     * @param actionContext 业务动作上下文
     * @return
     */
    Boolean createOrderCancel(BusinessActionContext actionContext);

}
