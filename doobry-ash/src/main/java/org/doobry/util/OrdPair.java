/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class OrdPair<T extends Comparable<? super T>, U extends Comparable<? super U>>
        extends EqPair<T, U> implements Comparable<OrdPair<T, U>> {

    public OrdPair(T first, U second) {
        super(first, second);
    }

    @Override
    public final int compareTo(OrdPair<T, U> rhs) {
        int n = first.compareTo(rhs.first);
        if (0 == n) {
            n = second.compareTo(rhs.second);
        }
        return n;
    }
}
