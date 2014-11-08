/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.doobry.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Context implements ServletContextListener {
    private static final class Holder {
        private static final Rest rest = new Rest();

        private static void init() {
            // Force static initialisation.
        }
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        Holder.init();
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
    }

    public static Rest getRest() {
        return Holder.rest;
    }
}
