package com.investmetic.domain.strategy.service;

import com.investmetic.domain.strategy.dto.request.TraderDailyAnalysisRequestDto;
import com.investmetic.domain.strategy.dto.response.DailyAnalysisResponse;
import com.investmetic.domain.strategy.model.entity.DailyAnalysis;
import com.investmetic.domain.strategy.model.entity.Proceed;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.repository.DailyAnalysisRepository;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StrategyAnalysisService {
    private final DailyAnalysisRepository dailyAnalysisRepository;
    private final StrategyRepository strategyRepository;

    @Transactional
    public void createDailyAnalysis(Long strategyId, List<TraderDailyAnalysisRequestDto> analysisRequests,
                                    Long userId) {
        Set<LocalDate> dateSet = new HashSet<>();
        for (TraderDailyAnalysisRequestDto request : analysisRequests) {
            if (!dateSet.add(request.getDate())) {
                throw new BusinessException(ErrorCode.DUPLICATE_DATE_IN_REQUEST);
            }
        }

        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        for (TraderDailyAnalysisRequestDto analysisRequest : analysisRequests) {
            Optional<DailyAnalysis> existsDailyData = dailyAnalysisRepository.findDailyAnalysisByStrategyAndDate(
                    strategy,
                    analysisRequest.getDate());

            if (existsDailyData.isPresent()) {
                throw new BusinessException(ErrorCode.DAILY_ANALYSIS_ALREADY_EXISTS);
            }

            DailyAnalysis dailyAnalysis = DailyAnalysis.builder()
                    .strategy(strategy)
                    .dailyDate(analysisRequest.getDate())
                    .transaction(analysisRequest.getTransaction())
                    .dailyProfitLoss(analysisRequest.getDailyProfitLoss())
                    .proceed(Proceed.NO)
                    .build();

            dailyAnalysisRepository.save(dailyAnalysis);
        }
    }

    @Transactional
    public void modifyDailyAnalysis(Long strategyId, TraderDailyAnalysisRequestDto analysisRequest, Long userId) {
        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        DailyAnalysis existsDailyData = dailyAnalysisRepository.findDailyAnalysisByStrategyAndDate(
                        strategy,
                        analysisRequest.getDate())
                .orElseThrow(() -> new BusinessException(ErrorCode.DAILY_ANALYSIS_NOT_FOUND));

        if (existsDailyData.getProceed() == Proceed.YES) {
            DailyAnalysis dailyAnalysis = DailyAnalysis.builder()
                    .strategy(strategy)
                    .dailyDate(analysisRequest.getDate())
                    .transaction(analysisRequest.getTransaction())
                    .dailyProfitLoss(analysisRequest.getDailyProfitLoss())
                    .proceed(Proceed.NO)
                    .build();

            dailyAnalysisRepository.save(dailyAnalysis);
        } else {
            existsDailyData.modifyDailyAnalysis(analysisRequest.getTransaction(), analysisRequest.getDailyProfitLoss());
        }
    }

    @Transactional
    public void deleteStrategyAllDailyAnalysis(Long strategyId, Long userId) {
        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        strategy.resetStrategyDailyAnalysis();

        dailyAnalysisRepository.deleteAllByStrategy(strategy);
    }

    @Transactional
    public void deleteStrategyDailyAnalysis(Long strategyId, Long analysisId, Long userId) {
        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        DailyAnalysis dailyAnalysis = dailyAnalysisRepository.findByDailyAnalysisId(analysisId)
                .orElseThrow(() -> new BusinessException((ErrorCode.INVALID_TYPE_VALUE)));

        LocalDate dailyDate = dailyAnalysis.getDailyDate();
        Optional<DailyAnalysis> nextDailyAnalysis = dailyAnalysisRepository.findByAfterDate(strategy, dailyDate);
        nextDailyAnalysis.ifPresent(analysis -> analysis.setProceed(Proceed.NO));

        dailyAnalysisRepository.deleteByStrategyAndDailyAnalysisId(strategy, analysisId);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<DailyAnalysisResponse> getMyDailyAnalysis(Long strategyId, Pageable pageable) {
        Page<DailyAnalysisResponse> myDailyAnalysis = dailyAnalysisRepository.findMyDailyAnalysis(strategyId, pageable);
        return new PageResponseDto<>(myDailyAnalysis);
    }

    private Strategy findStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STRATEGY_NOT_FOUND));
    }

    private void verifyUserPermission(Strategy strategy, Long userId) {
        if (!strategy.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
