package com.valuation.funds.controller;

import com.valuation.funds.entity.FundValuationItem;
import com.valuation.funds.service.FundValuationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基金估值查询接口：接收前端传入的 secu_codes，调用恒生接口并返回解析后的实体列表。
 */
@RestController
@RequestMapping("/api")
public class FundValuationController {

    private final FundValuationService fundValuationService;

    public FundValuationController(FundValuationService fundValuationService) {
        this.fundValuationService = fundValuationService;
    }

    /**
     * POST /api/fund-valuation
     * 请求体为 JSON：{"secu_codes":"020156"}，或 form：secu_codes=020156。
     * 返回 List&lt;FundValuationItem&gt;，字段：target_time、estimated_nav、estimated_change、fundcode。
     */
    @PostMapping(value = "/fund-valuation", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> fundValuation(@RequestParam(required = false) String secu_codes,
                                          @RequestBody(required = false) FundValuationRequest jsonRequest) {
        String secuCodes = secu_codes;
        if (secuCodes == null && jsonRequest != null && jsonRequest.getSecu_codes() != null) {
            secuCodes = jsonRequest.getSecu_codes();
        }
        if (secuCodes == null) {
            secuCodes = "";
        }
        try {
            List<FundValuationItem> list = fundValuationService.getFundValuationLastPoint(secuCodes);
            return ResponseEntity.ok(list);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("调用恒生接口失败: " + e.getMessage());
        }
    }
}
