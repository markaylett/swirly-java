package com.swirlycloud.twirly.rest;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.util.Params;

public @NonNullByDefault interface Rest {

    @Nullable
    String findTraderByEmail(String email) throws ServiceUnavailableException, IOException;

    void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException;

    void getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException;

    void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException;

    void getView(Params params, long now, Appendable out) throws IOException;

    void getView(String market, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getSess(String mnem, Params params, long now, Appendable out) throws NotFoundException,
            ServiceUnavailableException, IOException;

    void getOrder(String mnem, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getOrder(String mnem, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getOrder(String mnem, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getTrade(String mnem, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getTrade(String mnem, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getTrade(String mnem, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getPosn(String mnem, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getPosn(String mnem, String contr, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getPosn(String mnem, String contr, int settlDate, Params params, long now, Appendable out)
            throws NotFoundException, IOException;
}
