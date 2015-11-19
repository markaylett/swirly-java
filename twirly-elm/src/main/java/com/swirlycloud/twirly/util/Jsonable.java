/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.util.Params;

public interface Jsonable {
    void toJson(@Nullable Params params, @NonNull Appendable out) throws IOException;
}
