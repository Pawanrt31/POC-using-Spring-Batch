/**
* Copyright (C) 2019 Koninklijke Philips N.V, Inc. All rights reserved
* @Owner Koninklijke Philips N.V.
* Unauthorized copying of this file, via any medium is strictly prohibited
* Proprietary and confidential
*/
package com.philips.poc.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.philips.poc.error.ValidationMessage;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String severity;
    private int code;
    private String message;
    private String description;
    private String cause;
    @JsonInclude(Include.NON_NULL)
    private List<ValidationMessage> validation = null;

}