/**
 * 
 */
package org.rifidi.edge.adapter.generic;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

/**
 * @author matt
 *
 */
public class GenericRestServer {
	/** Logger */
	private final Log logger = LogFactory.getLog(getClass());

	//Server restServer = null;
	
	Component restServer = null;
	Component sslRestServer = null;

	/**
	 * 
	 * @param port
	 */
	public GenericRestServer(int port, int sslPort, Application app) {
		// If you want to see json output
		// GenericTagDTO tag1 = new GenericTagDTO();
		// GenericTagDTO tag2 = new GenericTagDTO();
		// tag1.setAntenna(1);
		// tag1.setExtrainformation("Direction:1|Speed:50");
		// tag1.setEpc("123456781234567812345678");
		// tag1.setReader("Alien_1");
		// tag1.setRssi("123");
		// tag1.setTimestamp(System.currentTimeMillis());
		// tag2.setAntenna(1);
		// tag2.setExtrainformation("Direction:1|Speed:50");
		// tag2.setEpc("123456781234567800000000");
		// tag2.setReader("Alien_1");
		// tag2.setRssi("123");
		// tag2.setTimestamp(System.currentTimeMillis());
		// List<GenericTagDTO> taglist = new ArrayList<GenericTagDTO>();
		// taglist.add(tag1);
		// taglist.add(tag2);
		// Gson gson = new Gson();
		// String jsontest = gson.toJson(taglist);
		// System.out.println(jsontest);
		
		try {
			logger.info("RestletServer called");
			boolean restletEnabled = port>0;
			if (restletEnabled) {
				logger.info("Starting restlet server on port: " + port);
				restServer = new Component();
				Server jettyServer = new Server(restServer.getContext().createChildContext(), Protocol.HTTP, port);
				Series<Parameter> parameters = jettyServer.getContext().getParameters();

				// jetty parameters
				addArguments(parameters);
				restServer.getServers().add(jettyServer);
				restServer.getClients().add(Protocol.FILE);

				restServer.getDefaultHost().attach(app);
				restServer.start();
			}

			// Check if ssl is enabled
			boolean sslEnabled = sslPort>0;

			if (sslEnabled) {
				logger.info("Restlet SSL Server enabled");

				// Get required jvm properties

				String keystorepath = System.getProperty("org.rifidi.home") + File.separator
						+ System.getProperty("org.rifidi.restlet.ssl.keystorepath");
				String keystorepassword = System.getProperty("org.rifidi.restlet.ssl.keystorepassword");
				String keypassword = System.getProperty("org.rifidi.restlet.ssl.keypassword");
				String keystoretype = System.getProperty("org.rifidi.restlet.ssl.keystoretype");

				logger.info("Starting ssl restlet server on port: " + sslPort);

				sslRestServer = new Component();
				Server sjettyServer = new Server(sslRestServer.getContext().createChildContext(), Protocol.HTTPS, sslPort);

				sjettyServer.getProtocols().add(Protocol.FILE);

				Series<Parameter> parameters = sjettyServer.getContext().getParameters();

				parameters.add("sslContextFactory", "org.restlet.ext.jsslutils.PkixSslContextFactory");
				parameters.add("keystorePath", keystorepath);
				parameters.add("keystorePassword", keystorepassword);
				parameters.add("keyPassword", keypassword);
				parameters.add("keystoreType", keystoretype);
				// jetty parameters
				addArguments(parameters);

				sslRestServer.getServers().add(sjettyServer);

				sslRestServer.getDefaultHost().attach(app);
				sslRestServer.start();
			}

		} catch (Exception e) {
			// TODO Handle this
			e.printStackTrace();
		}
	}

	public static void addArguments(Series<Parameter> parameters) {
		String[] args = new String[] { "threadPool.minThreads", "threadPool.maxThreads", "threadPool.threadsPriority",
				"threadPool.idleTimeout", "threadPool.stopTimeout", "connector.acceptors", "connector.selectors",
				"connector.acceptQueueSize", "connector.idleTimeout", "connector.soLingerTime", "connector.stopTimeout",
				"http.headerCacheSize", "http.requestHeaderSize", "http.responseHeaderSize", "http.outputBufferSize",
				"lowResource.period", "lowResource.threads", "lowResource.maxMemory", "lowResource.maxConnections",
				"lowResource.idleTimeout", "lowResource.stopTimeout", "spdy.version", "spdy.pushStrategy" };
		for (String arg : args) {
			String prop = System.getProperty("org.rifidi.edgemobile." + arg);
			if (prop != null) {
				parameters.add(arg, prop);
			}
		}
	}
	
	public void stopServer() {
		try {
			restServer.stop();
			sslRestServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
