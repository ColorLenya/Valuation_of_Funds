package com.valuation.funds.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 基金估值查询请求体，前端传入 secu_codes。
 */
public class FundValuationRequest {

    @JsonProperty("secu_codes")
    private String secu_codes;

    public String getSecu_codes() {
        return secu_codes;
    }

    public void setSecu_codes(String secu_codes) {
        this.secu_codes = secu_codes;
    }
}
