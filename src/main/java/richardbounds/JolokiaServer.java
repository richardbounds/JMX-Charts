package richardbounds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jolokia.jvmagent.jdk6.JolokiaHttpHandler;
import org.jolokia.util.ConfigKey;
import org.json.simple.JSONArray;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


@SuppressWarnings("restriction")
public class JolokiaServer {

	private HttpServer server;
	
	private final int port;

	private final ChartProvider charts;

	public JolokiaServer(int port, ChartProvider charts) {
		this.port = port;
		this.charts = charts;
	}

	public void start() throws IOException {
		

		Map<ConfigKey,String> config = new HashMap<ConfigKey, String>();
        config.put(ConfigKey.AGENT_CONTEXT, "/jmx");
        config.put(ConfigKey.POLICY_LOCATION, getClass().getResource("/jolokia-restrictions.xml").toString());
		
		server = HttpServer.create(new InetSocketAddress(port), 0);
	    server.createContext("/jmx/", new JolokiaHttpHandler(config));
	    server.createContext("/charts.json", new ChartJsonHandler(charts));
	    server.createContext("/", new ClasspathFileHandler("/jolokia-web"));
	    server.setExecutor(null); 
	    server.start();
		
	}
	
	public void stop() {
		server.stop(0);
	}
	
	
	static class ChartJsonHandler implements HttpHandler {
		private final ChartProvider charts;

		public ChartJsonHandler(ChartProvider charts) {
			super();
			this.charts = charts;
		}

		public void handle(HttpExchange pExchange) throws IOException {
			
			JSONArray chartArray = new JSONArray();
			for (JMXChart chart : charts.getCharts()) {
				JSONArray arr = new JSONArray(); 
				arr.add(chart);
				chartArray.add(arr);
			}
			
			byte[] response = chartArray.toString().getBytes();
			
			pExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
			pExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
			
			pExchange.getResponseBody().write(response);
			pExchange.close();
		}
		
		
	}
		
	static class ClasspathFileHandler implements HttpHandler {

		private final String path;
		
		public ClasspathFileHandler(String path) {
			super();
			this.path = path;
		}



		public void handle(HttpExchange pExchange) throws IOException {
		       URI uri = pExchange.getRequestURI();
		       OutputStream os = null;

		       String reqPath = uri.getPath();
		       
		       if ("/".equals(reqPath)) {
		    	   reqPath = "/index.html";
		       }
		       
			   InputStream str = ClasspathFileHandler.class.getResourceAsStream(path + reqPath);
		       if (!reqPath.startsWith("/")) {
		    	   reqPath = "/" + reqPath;
		       }
			   
			   
		       if (str == null) { 
		    	   pExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,0);
		    	   pExchange.close();
		    	   return;
		       }

				if (reqPath.endsWith( ".js")){
					pExchange.getResponseHeaders().set("Content-Type", "text/javascript; charset=UTF-8");
				} else if(reqPath.endsWith(".css")){
					pExchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
				} else if (reqPath.endsWith(".html")) {
					pExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
				}		

				byte[] response = readBytes(str);
				
				pExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
				
				os = pExchange.getResponseBody();
				os.write(response);
				pExchange.close();
				
		}



		private byte[] readBytes(InputStream str) throws IOException {
			int thisLine;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				while ((thisLine = str.read()) != -1) {
				    bos.write(thisLine);
				}
				bos.flush();
	
				return bos.toByteArray();
			} finally {
				if (bos != null){
				    bos.close();
				}
			}
		}
		
		
		
	}
	
	
}
