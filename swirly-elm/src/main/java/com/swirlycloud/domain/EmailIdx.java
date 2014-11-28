/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

public final class EmailIdx {

    private final User[] buckets;

    private static boolean equals(User user, String email) {
        assert email != null;
        return user.getEmail().equals(email);
    }

    private static int indexFor(int hash, int length) {
        return (hash & 0x7fffffff) % length;
    }

    public EmailIdx(int nBuckets) {
        assert nBuckets > 0;
        this.buckets = new User[nBuckets];
    }

    public final void insert(User user) {
        assert user != null;
        if (user.getEmail() != null) {
            final int bucket = indexFor(user.getEmail().hashCode(),
                    buckets.length);
            user.emailNext = buckets[bucket];
            buckets[bucket] = user;
        }
    }

    public final User find(String email) {
        assert email != null;
        final int bucket = indexFor(email.hashCode(), buckets.length);
        for (User it = buckets[bucket]; it != null; it = it.emailNext) {
            if (equals(it, email)) {
                return it;
            }
        }
        return null;
    }
}
