/**  Copyright (C) 2021 Koninklijke Philips N.V, Inc. All rights reserved 
 *   @Owner Koninklijke Philips N.V. 
 *   Unauthorized copying of this file, via any medium is strictly prohibited 
 *   Proprietary and confidential 
 */
package com.philips.poc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class RuleRequest extends EditRuleRequest{
     
    @NotBlank
    @Size(min = 2, max = 20)
    private String business;
    @NotBlank
    @Size(min = 3, max = 50)
    private String productType;
    @NotBlank
    @Size(min = 1, max = 100)
    private String ruleName;
    private int size;
}
