package m2dl.arge.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.hyperic.sigar.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

public class Calculateur {
    private static int monPort = 0;
    private static Sigar sigar;
    private static PrintWriter writer;

    public int add(int i1, int i2) throws InterruptedException {
        if(this.writer != null) {
            writer.println("Requete " + i2 + " recue;");
        }
        int nbDivisibles = 0;
        for (int i = 2; i < i1; i++) {
            if (i1 % i == 0) {
                nbDivisibles++;
            }
        }
        Thread.sleep(5000);
        return monPort;
    }

    public double getCPUCharge() {
        CpuPerc cpuperc = null;
        try {
            cpuperc = sigar.getCpuPerc();
        } catch (SigarException se) {
            se.printStackTrace();
        }
        writer.print((cpuperc.getCombined() * 100) + "\t");
        return cpuperc.getCombined() * 100;
    }

    public int subtract(int i1, int i2) {
        return i1 - i2;
    }

    public static void main(String[] args) throws IOException, XmlRpcException {
        writer = new PrintWriter(new PrintWriter("logCalculateur2.txt", "UTF-8"), true);
        sigar = new Sigar();
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
                writer.println("Le Worker Node web a demarre ...");
                System.out.println("Le Worker Node web a demarre ...");




            } catch (NumberFormatException e) {
                e.printStackTrace();
                writer.println("Mauvais argument, usage : \n./Calculateur <nombre entier>");

            }
        }
    }
}
