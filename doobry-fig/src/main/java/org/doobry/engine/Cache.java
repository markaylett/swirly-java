/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.util.SlNode;

public final class Cache {
    private static int ID = 0;
    private static int MNEM = 1;
    private static int COLS = 2;
    private final int nBuckets;
    private SlNode firstAsset;
    private SlNode firstContr;
    private SlNode firstUser;
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
        final int bucket = indexFor(hashCode(rec.getType().intValue(), rec.getId()), nBuckets);
        final Rec first = buckets[bucket][ID];
        rec.setIdNext(first);
        buckets[bucket][ID] = rec;
    }

    private final void insertMnem(Rec rec) {
        final int bucket = indexFor(hashCode(rec.getType().intValue(), rec.getMnem()), nBuckets);
        final Rec first = buckets[bucket][MNEM];
        rec.setMnemNext(first);
        buckets[bucket][MNEM] = rec;
    }

    private final void updateIndex(SlNode first) {
        for (SlNode node = first; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            insertId(rec);
            insertMnem(rec);
        }
    }

    public Cache(int nBuckets) {
        assert nBuckets > 0;
        this.nBuckets = nBuckets;
        buckets = new Rec[nBuckets][COLS];
    }

    public final void insertList(RecType type, SlNode first) {
        switch (type) {
        case ASSET:
            assert firstAsset == null;
            firstAsset = first;
            break;
        case CONTR:
            assert firstContr == null;
            firstContr = first;
            break;
        case USER:
            assert firstUser == null;
            firstUser = first;
            break;
        }
        updateIndex(first);
    }

    public final Rec findRec(RecType type, long id) {
        final int bucket = indexFor(hashCode(type.intValue(), id), nBuckets);
        for (Rec rec = buckets[bucket][ID]; rec != null; rec = rec.idNext()) {
            if (rec.getType() == type && rec.getId() == id)
                return rec;
        }
        return null;
    }

    public final Rec findRec(RecType type, String mnem) {
        final int bucket = indexFor(hashCode(type.intValue(), mnem), nBuckets);
        for (Rec rec = buckets[bucket][MNEM]; rec != null; rec = rec.mnemNext()) {
            if (rec.getType() == type && rec.getMnem().equals(mnem))
                return rec;
        }
        return null;
    }

    public final SlNode getFirstRec(RecType type) {
        SlNode first = null;
        switch (type) {
        case ASSET:
            first = firstAsset;
            break;
        case CONTR:
            first = firstContr;
            break;
        case USER:
            first = firstUser;
            break;
        }
        return first;
    }

    public final boolean isEmptyRec(RecType type) {
        return getFirstRec(type) == null;
    }
}
