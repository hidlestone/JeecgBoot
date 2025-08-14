package org.jeecg.modules.test.seata.tcc.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.test.seata.account.entity.SeataAccount;
import org.jeecg.modules.test.seata.account.mapper.SeataAccountMapper;
import org.jeecg.modules.test.seata.tcc.service.TccAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class TccAccountServiceImpl implements TccAccountService {

    @Resource
    private SeataAccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseAccountPrepare(BusinessActionContext actionContext, Long userId, BigDecimal amount) throws Exception {
        SeataAccount account = accountMapper.selectById(userId);
        /*if (account.getBalance().compareTo(amount) < 0) {
            throw new Exception("账号余额不足");
        }*/

        LambdaUpdateWrapper<SeataAccount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataAccount::getId, userId)
                .set(SeataAccount::getFrozenBalance, account.getFrozenBalance().add(amount))
                .set(SeataAccount::getBalance, account.getBalance().subtract(amount));
        // 执行更新
        int result = accountMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseAccountCommit(BusinessActionContext actionContext) {
        // 从上下文中获取参数
        Integer userId = (Integer) actionContext.getActionContext("userId");
        BigDecimal amount = (BigDecimal) actionContext.getActionContext("amount");

        SeataAccount account = accountMapper.selectById(userId);
        LambdaUpdateWrapper<SeataAccount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataAccount::getId, userId)
                .set(SeataAccount::getFrozenBalance, account.getFrozenBalance().subtract(amount));
        int result = accountMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseAccountCancel(BusinessActionContext actionContext) {
        // 从上下文中获取参数
        Integer userId = (Integer) actionContext.getActionContext("userId");
        BigDecimal amount = (BigDecimal) actionContext.getActionContext("amount");

        SeataAccount account = accountMapper.selectById(userId);
        LambdaUpdateWrapper<SeataAccount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeataAccount::getId, userId)
                .set(SeataAccount::getFrozenBalance, account.getFrozenBalance().subtract(amount))
                .set(SeataAccount::getBalance, account.getBalance().add(amount));
        // 执行更新
        int result = accountMapper.update(null, updateWrapper);
        return result > 0;
    }
}
