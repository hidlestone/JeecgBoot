package org.jeecg.modules.test.seata.tcc.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

@LocalTCC
public interface TccAccountService {
    /**
     * 从账号扣钱准备
     *
     * @param userId
     * @param amount
     * @param actionContext 业务动作上下文
     * @return 是/否
     */
    @TwoPhaseBusinessAction(name = "decreaseAccountTcc", commitMethod = "decreaseAccountCommit", rollbackMethod = "decreaseAccountCancel")
    Boolean decreaseAccountPrepare(BusinessActionContext actionContext,
                                   @BusinessActionContextParameter(paramName = "userId") Long userId,
                                   @BusinessActionContextParameter(paramName = "amount") BigDecimal amount) throws Exception;

    /**
     * 从账号扣钱提交
     *
     * @param actionContext
     * @return 是/否
     */
    Boolean decreaseAccountCommit(BusinessActionContext actionContext);


    /**
     * 从账号扣钱取消
     *
     * @param actionContext
     * @return 是/否
     */
    Boolean decreaseAccountCancel(BusinessActionContext actionContext);
}
