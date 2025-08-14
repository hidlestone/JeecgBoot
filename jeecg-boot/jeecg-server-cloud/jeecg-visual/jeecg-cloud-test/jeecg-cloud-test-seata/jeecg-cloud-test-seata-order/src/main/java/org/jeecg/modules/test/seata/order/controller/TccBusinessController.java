package org.jeecg.modules.test.seata.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.service.SeataOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test/seata/business")
@Tag(name = "seata tcc 测试")
public class TccBusinessController {

    @Autowired
    private SeataOrderService orderService;

    @PostMapping("/buy")
    @Operation(summary = "测试tcc")
    public String handleBusiness(@Validated @RequestBody PlaceOrderRequest request) {
        log.info("请求参数：{}", request.toString());
        Boolean result = orderService.handleBusiness(request);
        if (result) {
            return "ok";
        }
        return "fail";
    }

}
