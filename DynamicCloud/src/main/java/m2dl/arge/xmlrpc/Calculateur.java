package m2dl.arge.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;
import java.net.BindException;

public class Calculateur {
    private static int monPort = 0;

    public int add(int i1, int i2) throws InterruptedException {
        int nbDivisibles = 0;
        for (int i = 2; i < i1; i++) {
            if (i1 % i == 0) {
                nbDivisibles++;
            }
        }
        Thread.sleep(5000);
        return monPort;
    }

    public int subtract(int i1, int i2) {
        return i1 - i2;
    }

    public static void main(String[] args) throws IOException, XmlRpcException {
        if (args[0] != null) {
            try {
                monPort = Integer.parseInt(args[0]);
//				try {
                // Test
                Integer.parseInt(args[0]);

                WebServer webServer = new WebServer(Integer.parseInt(args[0]));

                XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

                PropertyHandlerMapping phm = new PropertyHandlerMapping();
                    /*
					 * Load handler definitions from a property file. The
					 * property file might look like:
					 * Calculator=org.apache.xmlrpc.demo.Calculator
					 * org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo
					 * .proxy. AdderImpl
					 */
                phm.load(Thread.currentThread().getContextClassLoader(), "XmlRpcCalculator.properties");

					/*
					 * You may also provide the handler classes directly, like
					 * this: phm.addHandler("Calculator",
					 * org.apache.xmlrpc.demo.Calculator.class);
					 * phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.
					 * getName() ,
					 * org.apache.xmlrpc.demo.proxy.AdderImpl.class);
					 */
                xmlRpcServer.setHandlerMapping(phm);

                XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
                serverConfig.setEnabledForExtensions(true);
                serverConfig.setContentLengthOptional(false);

                webServer.start();
                System.out.println("Le Worker Node web a demarre ...");
//				} catch (BindException e) {
//					String[] argsTemp = new String[1];
//					argsTemp[0] = (Integer.parseInt(args[0]) + 1) + "" ;
//					Calculateur.main(argsTemp);
//				}
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Mauvais argument, usage : \n./Calculateur <nombre entier>");

            }
        }
    }
}
