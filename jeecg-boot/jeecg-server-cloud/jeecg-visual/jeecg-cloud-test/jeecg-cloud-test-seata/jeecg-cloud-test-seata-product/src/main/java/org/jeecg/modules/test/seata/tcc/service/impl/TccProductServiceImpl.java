package org.jeecg.modules.test.seata.tcc.service.impl;

import io.seata.rm.tcc.api.BusinessActionContext;
import org.jeecg.modules.test.seata.tcc.service.TccProductService;

public class TccProductServiceImpl implements TccProductService {

    @Override
    public Boolean decreaseStoragePrepare(BusinessActionContext actionContext, Long productId, Integer count) {
        return null;
    }

    @Override
    public Boolean decreaseStorageCommit(BusinessActionContext actionContext) {
        return null;
    }

    @Override
    public Boolean decreaseStorageCancel(BusinessActionContext actionContext) {
        return null;
    }
}
