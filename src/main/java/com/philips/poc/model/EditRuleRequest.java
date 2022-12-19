/**  Copyright (C) 2019 Koninklijke Philips N.V, Inc. All rights reserved 
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
public class EditRuleRequest {
    
    @NotBlank
    @Size(min = 1, max = 10000)
    private String ruleExpression;
    @NotBlank
    @Size(min = 1,max = 255)
    private String ruleDescription;
    private boolean ualTemplate;
}
