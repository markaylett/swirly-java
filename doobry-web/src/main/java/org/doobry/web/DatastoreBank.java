package org.doobry.web;

import org.doobry.domain.Kind;
import org.doobry.engine.Bank;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyRange;

public final class DatastoreBank implements Bank {
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    @Override
    public final long allocIds(Kind kind, long num) {
        final KeyRange range = datastore.allocateIds(kind.name().toLowerCase(), num);
        return range.getStart().getId();
    }
}
