/*
 * Copyright 2007-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Main class which simplifies starting a Jetty server instance, under http, https or both at the same time.
 * @author Phil Zoio
 */
public class RunJetty {
	public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Insufficient arguments supplied");
            usage();
            System.exit(1);
        }

        try {

            int port = getPort(args[0]);
            File configLocation = getContextLocation(args[1]);
            String contextPath = getContextPath(args[2]);
            String scheme = args[3];
            String resourceBase = args(args, 4, "html");

            Server server = new Server();
            
            ServerConnector sslConnector = null;
            boolean https = scheme.equals("https");
            boolean http = scheme.equals("http");
            boolean both = scheme.equals("both");
            int sslPort = -1;
            if (https || both) {
                
            	HttpConfiguration httpConfig = new HttpConfiguration();
        		httpConfig.setSendServerVersion( false );
        		httpConfig.addCustomizer(new SecureRequestCustomizer());
        		HttpConnectionFactory httpFactory = new HttpConnectionFactory( httpConfig );
        		
        	    SslContextFactory sslContextFactory = new SslContextFactory();
        	    sslContextFactory.setKeyStorePath(System.getProperty("jetty.ssl.keystore"));
        	    sslContextFactory.setKeyStorePassword(System.getProperty("jetty.ssl.keystore.password"));
        	    sslContextFactory.setKeyManagerPassword(System.getProperty("jetty.ssl.key.password"));
        	    sslContextFactory.setTrustStorePath(System.getProperty("jetty.ssl.truststore"));
        	    sslContextFactory.setTrustStorePassword(System.getProperty("jetty.ssl.truststore.password"));
        	    
				SslConnectionFactory sslFactory =  new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

				sslConnector = new ServerConnector(server,sslFactory,httpFactory);
				
				sslPort = https ? port : Integer.parseInt(System.getProperty("ssl.port"));
				sslConnector.setPort(sslPort);
        	    
            }

            ServerConnector connector = null;
            
            if (http || both) {
            
            	HttpConfiguration httpConfig = new HttpConfiguration();
        		httpConfig.setSendServerVersion( false );
        		if (both) {
            		httpConfig.setSecurePort(sslPort);
                }
        		HttpConnectionFactory httpFactory = new HttpConnectionFactory( httpConfig );
        		
        		connector = new ServerConnector( server,httpFactory );
                connector.setPort(port);
            }
            
            Connector[] connectors = null;
            
            if (both) {
                connectors = new Connector[] { connector, sslConnector };
            } else if (http) {
                connectors = new Connector[] { connector };
            } else if (https) {
                connectors = new Connector[] { sslConnector };
            } else {
                throw new IllegalArgumentException("Invalid scheme. Must be 'http', 'https' or 'both'");
            }
            
            server.setConnectors(connectors);

            HandlerCollection handlers = new HandlerCollection();
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setResourceBase(resourceBase);
            
            ContextHandlerCollection contexts = new ContextHandlerCollection();

            WebAppContext webAppContext = new WebAppContext(configLocation.getAbsolutePath(), contextPath);
            
            contexts.addHandler(webAppContext);

            RequestLogHandler requestLogHandler = new RequestLogHandler();
            handlers.setHandlers(new Handler[] { contexts, resourceHandler, new DefaultHandler(), requestLogHandler });
            server.setHandler(handlers);

            server.setStopAtShutdown(true);

            try {
                server.start();
                server.join();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            usage();
            System.exit(-1);
        }
    }

    static String args(String[] args, int index, String defaultValue) {
        if (args.length < index+1) {
            return defaultValue;
        }
        return args[index];
    }

    static String getContextPath(String arg) {
        if (!arg.startsWith("/")) {
            arg = "/" + arg;
        }
        return arg;
    }

    static File getContextLocation(String arg) {
        File file = new File(arg);
        if (!file.exists()) {
            throw new IllegalArgumentException("Invalid context directory: " + file.getAbsolutePath());
        }
        return file;
    }

    static int getPort(String arg) {
        int port = 8080;
        try {
            port = Integer.parseInt(arg);
        }
        catch (NumberFormatException e1) {
            throw new IllegalArgumentException("Invalid port: " + arg);
        }
        return port;
    }
    

    private static void usage() {
        System.out.println(RunJetty.class.getName() + " [port] [context directory] [path] [http|https|both]");
    }
}
