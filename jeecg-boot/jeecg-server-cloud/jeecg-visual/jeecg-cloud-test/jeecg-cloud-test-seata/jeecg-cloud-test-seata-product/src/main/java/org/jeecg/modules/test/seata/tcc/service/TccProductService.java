package org.jeecg.modules.test.seata.tcc.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

@LocalTCC
public interface TccProductService {

    /**
     * 扣减库存准备
     *
     * @param actionContext 业务动作上下文
     * @param productId     产品ID
     * @param count         数量
     * @return 是/否
     */
    @TwoPhaseBusinessAction(name = "decreaseStorageTcc", commitMethod = "decreaseStorageCommit", rollbackMethod = "decreaseStorageCancel")
    BigDecimal decreaseStoragePrepare(BusinessActionContext actionContext,
                                      @BusinessActionContextParameter(paramName = "productId") Long productId,
                                      @BusinessActionContextParameter(paramName = "count") Integer count) throws Exception;

    /**
     * 扣减库存提交
     *
     * @param actionContext 业务动作上下文
     * @return 是/否
     */
    Boolean decreaseStorageCommit(BusinessActionContext actionContext);

    /**
     * 扣减库存回滚
     *
     * @param actionContext 业务动作上下文
     * @return 是/否
     */
    Boolean decreaseStorageCancel(BusinessActionContext actionContext);
}
