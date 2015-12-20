/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.fix;

public final class BusinessRejectReason {
    private BusinessRejectReason() {
    }

    public static final int OTHER = 0;
    public static final int UNKNOWN_ID = 1;
    public static final int UNKNOWN_SECURITY = 2;
    public static final int UNSUPPORTED_MESSAGE_TYPE = 3;
    public static final int APPLICATION_NOT_AVAILABLE = 4;
    public static final int CONDITIONALLY_REQUIRED_FIELD_MISSING = 5;
    public static final int NOT_AUTHORIZED = 6;
    public static final int DELIVERTO_FIRM_NOT_AVAILABLE_AT_THIS_TIME = 7;
    public static final int INVALID_PRICE_INCREMENT = 18;
}
