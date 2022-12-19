/** * Copyright (C) 2019 Koninklijke Philips N.V, Inc. All rights reserved 
* @Owner Koninklijke Philips N.V.
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential */

package com.philips.poc.error;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 320035663
 *
 */
@Getter
@Setter
public class ValidationMessage {
    private String field;
    private String errorMessage;

}
