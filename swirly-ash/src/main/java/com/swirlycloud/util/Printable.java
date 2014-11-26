/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

public interface Printable {
    // TODO: consider using Appendable.
    void print(StringBuilder sb, Object arg);
}
