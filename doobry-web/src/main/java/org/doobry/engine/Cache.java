/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Asset;
import org.doobry.domain.Contr;
import org.doobry.domain.Party;
import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.util.SlNode;

public final class Cache {
    private static int ID = 0;
    private static int MNEM = 1;
    private static int COLS = 2;
    private final int nBuckets;
    private Asset firstAsset;
    private Contr firstContr;
    private Party firstParty;
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

    private final void insertId(Rec rec) {
        final int bucket = hashCode(rec.getType().intValue(), rec.getId()) % nBuckets;
        final Rec first = buckets[bucket][ID];
        rec.setNext(first);
        buckets[bucket][ID] = rec;
    }

    private final void insertMnem(Rec rec) {
        final int bucket = hashCode(rec.getType().intValue(), rec.getMnem()) % nBuckets;
        final Rec first = buckets[bucket][MNEM];
        rec.setNext(first);
        buckets[bucket][MNEM] = rec;
    }

    private final void updateIndex(Rec first) {
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

    public final void insertList(Rec first) {
        switch (first.getType()) {
        case ASSET:
            assert firstAsset == null;
            firstAsset = (Asset) first;
            break;
        case CONTR:
            assert firstContr == null;
            firstContr = (Contr) first;
            break;
        case PARTY:
            assert firstParty == null;
            firstParty = (Party) first;
            break;
        }
        updateIndex(first);
    }

    public final Rec findId(RecType type, long id) {
        final int bucket = hashCode(type.intValue(), id) % nBuckets;
        for (SlNode node = buckets[bucket][ID]; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (rec.getType() == type && rec.getId() == id)
                return rec;
        }
        return null;
    }

    public final Rec findMnem(RecType type, String mnem) {
        final int bucket = hashCode(type.intValue(), mnem) % nBuckets;
        for (SlNode node = buckets[bucket][MNEM]; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (rec.getType() == type && rec.getMnem().equals(mnem))
                return rec;
        }
        return null;
    }

    public final Rec getFirst(RecType type) {
        Rec first = null;
        switch (type) {
        case ASSET:
            first = firstAsset;
            break;
        case CONTR:
            first = firstContr;
            break;
        case PARTY:
            first = firstParty;
            break;
        }
        return first;
    }

    public final boolean isEmpty(RecType type) {
        return getFirst(type) == null;
    }
}
