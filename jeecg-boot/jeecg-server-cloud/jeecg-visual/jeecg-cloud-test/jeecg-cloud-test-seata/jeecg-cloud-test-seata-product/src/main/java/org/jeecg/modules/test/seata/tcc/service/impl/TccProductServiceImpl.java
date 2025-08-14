package org.jeecg.modules.test.seata.tcc.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.test.seata.product.entity.SeataProduct;
import org.jeecg.modules.test.seata.product.mapper.SeataProductMapper;
import org.jeecg.modules.test.seata.tcc.service.TccProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class TccProductServiceImpl implements TccProductService {

    @Resource
    private SeataProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal decreaseStoragePrepare(BusinessActionContext actionContext, Long productId, Integer count) throws Exception {
        SeataProduct product = productMapper.selectById(productId);
        /*if (product.getStock() < count) {
            throw new Exception("商品库存不足");
        }*/

        LambdaUpdateWrapper<SeataProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataProduct::getId, productId)  // 条件：根据ID更新
                .set(SeataProduct::getFrozenStock, product.getFrozenStock() + count)
                .set(SeataProduct::getStock, product.getStock() - count);   // 设置：库存减10
        // 执行更新
        int result = productMapper.update(null, updateWrapper);
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(count));
        return totalPrice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseStorageCommit(BusinessActionContext actionContext) {
        // 从上下文中获取参数
        Integer productId = (Integer) actionContext.getActionContext("productId");
        Integer count = (Integer) actionContext.getActionContext("count");

        SeataProduct product = productMapper.selectById(productId);
        LambdaUpdateWrapper<SeataProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataProduct::getId, productId)
                .set(SeataProduct::getFrozenStock, product.getFrozenStock() - count);  // 解冻库存
        int result = productMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseStorageCancel(BusinessActionContext actionContext) {
        // 从上下文中获取参数
        Integer productId = (Integer) actionContext.getActionContext("productId");
        Integer count = (Integer) actionContext.getActionContext("count");

        SeataProduct product = productMapper.selectById(productId);
        LambdaUpdateWrapper<SeataProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataProduct::getId, productId)  // 条件：根据ID更新
                .set(SeataProduct::getFrozenStock, product.getFrozenStock() - count)
                .set(SeataProduct::getStock, product.getStock() + count);
        // 执行更新
        int result = productMapper.update(null, updateWrapper);
        return result > 0;
    }
}
