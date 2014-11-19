/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public final class User extends Rec {
    private final String email;

    public User(long id, String mnem, String display, String email) {
        super(Kind.USER, id, mnem, display);
        this.email = email;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        print(sb, null);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb, Object arg) {
        sb.append("{\"mnem\":\"").append(mnem);
        sb.append("\",\"display\":\"").append(display);
        sb.append("\",\"email\":\"").append(email);
        sb.append("\"}");
    }

    public final String getEmail() {
        return email;
    }
}
