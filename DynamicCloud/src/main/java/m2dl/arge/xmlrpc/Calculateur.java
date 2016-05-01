package m2dl.arge.xmlrpc;



import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

public class Calculateur {
	public int add(int i1, int i2) throws InterruptedException {
//		int nbDivisibles = 0;
//		for (int i = 2; i < i1; i++) {
//			if(i1 % i == 0)
//			{
//				nbDivisibles ++;
//			}
//		}
		Thread.sleep(500);
		return i1 + i2;
	}

	public int subtract(int i1, int i2) {
		return i1 - i2;
	}

	public static void main(String[] args) throws Exception {
		if (args[0] != null) {
			WebServer webServer = new WebServer(Integer.parseInt(args[0]));

			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			/*
			 * Load handler definitions from a property file. The property file
			 * might look like: Calculator=org.apache.xmlrpc.demo.Calculator
			 * org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.
			 * AdderImpl
			 */
			phm.load(Thread.currentThread().getContextClassLoader(), "XmlRpcCalculator.properties");

			/*
			 * You may also provide the handler classes directly, like this:
			 * phm.addHandler("Calculator",
			 * org.apache.xmlrpc.demo.Calculator.class);
			 * phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.getName()
			 * , org.apache.xmlrpc.demo.proxy.AdderImpl.class);
			 */
			xmlRpcServer.setHandlerMapping(phm);

			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			webServer.start();
			System.out.println("Le Worker Node web a demarre ...");
		}
	}
}
