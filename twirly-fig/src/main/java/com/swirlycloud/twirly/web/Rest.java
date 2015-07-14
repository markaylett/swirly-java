package com.swirlycloud.twirly.web;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.util.Params;

public @NonNullByDefault interface Rest {

    void getRec(boolean withTraders, Params params, long now, Appendable out) throws IOException;

    void getRec(RecType recType, Params params, long now, Appendable out) throws IOException;

    void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getView(Params params, long now, Appendable out) throws IOException;

    void getView(String marketMnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getSess(String email, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getOrder(String email, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getOrder(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getOrder(String email, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getTrade(String email, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getTrade(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getTrade(String email, String market, long id, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getPosn(String email, Params params, long now, Appendable out) throws NotFoundException,
            IOException;

    void getPosn(String email, String contr, Params params, long now, Appendable out)
            throws NotFoundException, IOException;

    void getPosn(String email, String contr, int settlDate, Params params, long now, Appendable out)
            throws NotFoundException, IOException;
}
