/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.jdkhttp;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.glassfish.jersey.internal.ProcessingException;
import org.glassfish.jersey.server.Application;
import org.glassfish.jersey.server.ContainerFactory;

/**
 * Factory for creating {@link HttpServer JDK HttpServer} instances adapted to
 * the {@link Application Jersey Application}.
 *
 * @author Miroslav Fuksa (miroslav.fuksa at oracle.com)
 */
public class JdkHttpServerFactory {

    /**
     * Creates and starts the {@link HttpServer JDK HttpServer} with the {@link Application Jersey Application}
     * deployed on the given {@link URI}.
     *
     * <p>The returned {@link HttpServer JDK HttpServer} is started.</p>
     *
     * @param uri The {@link URI uri} on which the {@link Application Jersey Application} will be deployed.
     * @param application The {@link Application Jersey Application} to be
     * deployed.
     * @return Newly created {@link HttpServer}.
     * @throws ProcessingException Thrown when problems during server creation
     * occurs.
     */
    public static HttpServer createHttpServer(final URI uri, final Application application) throws ProcessingException {
        final HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, application);

        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        final String scheme = uri.getScheme();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + uri
                    + ", must be equal (ignoring case) to 'http' or 'https'");
        }

        final String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ", must be non-null");
        } else if (path.length() == 0) {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ", must be present");
        } else if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ". must start with a '/'");
        }

        final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
        HttpServer server;
        try {
            server = (scheme.equalsIgnoreCase("http"))
                    ? HttpServer.create(new InetSocketAddress(port), 0)
                    : HttpsServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ioe) {
            throw new ProcessingException("IOException thrown when creating the JDK HttpServer.", ioe);
        }

        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext(path, handler);
        server.start();

        return server;
    }

    /**
     * Prevents instantiation.
     */
    private JdkHttpServerFactory() {
    }
}