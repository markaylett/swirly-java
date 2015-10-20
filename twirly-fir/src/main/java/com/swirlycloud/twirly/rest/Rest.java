package com.swirlycloud.twirly.rest;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.util.Params;

public @NonNullByDefault interface Rest {

    @Nullable
    String findTraderByEmail(String email) throws ServiceUnavailableException, IOException;

    long getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException;

    long getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException;

    long getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getView(Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException;

    long getView(String market, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getSess(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getOrder(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getOrder(String trader, String market, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getOrder(String trader, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getTrade(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getTrade(String trader, String market, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getTrade(String trader, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getPosn(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    long getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException;
}
