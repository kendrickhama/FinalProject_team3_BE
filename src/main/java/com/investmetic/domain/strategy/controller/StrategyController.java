package com.investmetic.domain.strategy.controller;

import com.investmetic.domain.strategy.dto.StrategyRegisterRequestDto;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.service.StrategyRegisterService;
import com.investmetic.global.dto.PresignedUrlResponseDto;
import com.investmetic.global.exception.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@Tag(name = "전략 API", description = "전략 관련 API")
public class StrategyController {
    private final StrategyRegisterService strategyRegisterService;

    @PostMapping("/register")
    @Operation(summary = "전략 등록", description = "새로운 전략 등록 API")
    public BaseResponse<PresignedUrlResponseDto> registerStrategy(@RequestBody StrategyRegisterRequestDto requestDto) {
        return strategyRegisterService.registerStrategy(requestDto);
    }
}
