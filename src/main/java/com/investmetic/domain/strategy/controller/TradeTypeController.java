package com.investmetic.domain.strategy.controller;

import com.investmetic.domain.strategy.dto.request.TradeTypeRequestDTO;
import com.investmetic.domain.strategy.service.TradeTypeService;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/strategies")
public class TradeTypeController {

    private final TradeTypeService tradeTypeService;


    @PostMapping("/trade-type")
    public ResponseEntity<BaseResponse<String>> addTradeType(
            @RequestBody TradeTypeRequestDTO tradeTypeRequestDTO){
        String preSignedURL = tradeTypeService.saveTradeType(tradeTypeRequestDTO);

        return BaseResponse.success(SuccessCode.CREATED,preSignedURL);
    }
}
