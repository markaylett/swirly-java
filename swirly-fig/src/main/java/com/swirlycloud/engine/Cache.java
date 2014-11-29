/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.util.Queue;
import com.swirlycloud.util.SlNode;

public final class Cache {
    private static int ID = 0;
    private static int MNEM = 1;
    private static int COLS = 2;
    private final int nBuckets;
    private Queue assets;
    private Queue contrs;
    private Queue users;
    private final Rec[][] buckets;

    private static int hashCode(long id) {
        return (int) (id ^ id >>> 32);
    }

    private static int hashCode(int type, long id) {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        result = prime * result + hashCode(id);
        return result;
    }

    private static int hashCode(int type, String mnem) {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        result = prime * result + mnem.hashCode();
        return result;
    }

    private static int indexFor(int hash, int length) {
        return (hash & 0x7fffffff) % length;
    }

    private final void insertId(Rec rec) {
        final int bucket = indexFor(hashCode(rec.getKind().intValue(), rec.getId()), nBuckets);
        final Rec first = buckets[bucket][ID];
        rec.setIdNext(first);
        buckets[bucket][ID] = rec;
    }

    private final void insertMnem(Rec rec) {
        final int bucket = indexFor(hashCode(rec.getKind().intValue(), rec.getMnem()), nBuckets);
        final Rec first = buckets[bucket][MNEM];
        rec.setMnemNext(first);
        buckets[bucket][MNEM] = rec;
    }

    public Cache(int nBuckets) {
        assert nBuckets > 0;
        this.nBuckets = nBuckets;
        assets = new Queue();
        contrs = new Queue();
        users = new Queue();
        buckets = new Rec[nBuckets][COLS];
    }

    public final void insertRec(Rec rec) {
        switch (rec.getKind()) {
        case ASSET:
            assets.insertBack(rec);
            break;
        case CONTR:
            contrs.insertBack(rec);
            break;
        case USER:
            users.insertBack(rec);
            break;
        default:
            throw new IllegalArgumentException("invalid record-type");
        }
        insertId(rec);
        insertMnem(rec);
    }

    public final Rec findRec(Kind kind, long id) {
        final int bucket = indexFor(hashCode(kind.intValue(), id), nBuckets);
        for (Rec rec = buckets[bucket][ID]; rec != null; rec = rec.idNext()) {
            if (rec.getKind() == kind && rec.getId() == id)
                return rec;
        }
        return null;
    }

    public final Rec findRec(Kind kind, String mnem) {
        final int bucket = indexFor(hashCode(kind.intValue(), mnem), nBuckets);
        for (Rec rec = buckets[bucket][MNEM]; rec != null; rec = rec.mnemNext()) {
            if (rec.getKind() == kind && rec.getMnem().equals(mnem))
                return rec;
        }
        return null;
    }

    public final SlNode getFirstRec(Kind kind) {
        SlNode first = null;
        switch (kind) {
        case ASSET:
            first = assets.getFirst();
            break;
        case CONTR:
            first = contrs.getFirst();
            break;
        case USER:
            first = users.getFirst();
            break;
        default:
            throw new IllegalArgumentException("invalid record-type");
        }
        return first;
    }

    public final boolean isEmptyRec(Kind kind) {
        return getFirstRec(kind) == null;
    }
}
