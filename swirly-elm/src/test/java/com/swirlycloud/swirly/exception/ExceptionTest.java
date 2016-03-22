/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.swirly.fix.BusinessRejectReason;
import com.swirlycloud.swirly.fix.CancelRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;

public final class ExceptionTest {

    @Test
    public final void testBadRequestException() {
        final ServException e = new BadRequestException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testAlreadyExistsException() {
        final ServException e = new AlreadyExistsException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testRefAlreadyExistsException() {
        final ServException e = new RefAlreadyExistsException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.DUPLICATE_CLORDID_RECEIVED, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.DUPLICATE_ORDER, e.getOrderRejectReason());
    }

    @Test
    public final void testInvalidException() {
        final ServException e = new InvalidException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testInvalidLotsException() {
        final ServException e = new InvalidLotsException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.INCORRECT_QUANTITY, e.getOrderRejectReason());
    }

    @Test
    public final void testTooLateException() {
        final ServException e = new TooLateException("");
        assertEquals(400, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.TOO_LATE_TO_CANCEL, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.TOO_LATE_TO_ENTER, e.getOrderRejectReason());
    }

    @Test
    public final void testForbiddenException() {
        final ServException e = new ForbiddenException("");
        assertEquals(403, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testMethodNotAllowedException() {
        final ServException e = new MethodNotAllowedException("");
        assertEquals(405, e.getHttpStatus());
        assertEquals(BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testNotFoundException() {
        final ServException e = new NotFoundException("");
        assertEquals(404, e.getHttpStatus());
        assertEquals(BusinessRejectReason.UNKNOWN_ID, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testMarketClosedException() {
        final ServException e = new MarketClosedException("");
        assertEquals(404, e.getHttpStatus());
        assertEquals(BusinessRejectReason.OTHER, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.EXCHANGE_CLOSED, e.getOrderRejectReason());
    }

    @Test
    public final void testMarketNotFoundException() {
        final ServException e = new MarketNotFoundException("");
        assertEquals(404, e.getHttpStatus());
        assertEquals(BusinessRejectReason.UNKNOWN_SECURITY, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.UNKNOWN_SYMBOL, e.getOrderRejectReason());
    }

    @Test
    public final void testOrderNotFoundException() {
        final ServException e = new OrderNotFoundException("");
        assertEquals(404, e.getHttpStatus());
        assertEquals(BusinessRejectReason.UNKNOWN_ID, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.UNKNOWN_ORDER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.UNKNOWN_ORDER, e.getOrderRejectReason());
    }

    @Test
    public final void testTraderNotFoundException() {
        final ServException e = new TraderNotFoundException("");
        assertEquals(404, e.getHttpStatus());
        assertEquals(BusinessRejectReason.UNKNOWN_ID, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.UNKNOWN_ACCOUNT, e.getOrderRejectReason());
    }

    @Test
    public final void testServiceUnavailableException() {
        final ServException e = new ServiceUnavailableException("");
        assertEquals(503, e.getHttpStatus());
        assertEquals(BusinessRejectReason.APPLICATION_NOT_AVAILABLE, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }

    @Test
    public final void testUnauthorizedException() {
        final ServException e = new UnauthorizedException("");
        assertEquals(401, e.getHttpStatus());
        assertEquals(BusinessRejectReason.NOT_AUTHORIZED, e.getBusinessRejectReason());
        assertEquals(CancelRejectReason.OTHER, e.getCancelRejectReason());
        assertEquals(OrderRejectReason.OTHER, e.getOrderRejectReason());
    }
}
