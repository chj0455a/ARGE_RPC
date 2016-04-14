
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
//  import org.apache.xmlrpc.demo.webserver.proxy.impls.AdderImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.google.common.collect.Maps;

public class Repartiteur {
	private static Logger LOGGER = Logger.getLogger("Repartiteur");
	private static final int port = 8080;

	private static HashMap<Integer, XmlRpcClient> calculateurs;

	private static Integer calculateurCourant;

	private static XmlRpcClient redirectionCourante;

	public static void main(String[] args) throws Exception {
		if (args[0] != null) {
			calculateurs = Maps.newHashMap();

			WebServer webServer = new WebServer(Integer.parseInt(args[0]));

			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.load(Thread.currentThread().getContextClassLoader(), "XmlRpcServlet.properties");
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			webServer.start();
			System.out.println("Le repartiteur a démarré ...");
		}
		while (true) {
			System.out.print("Mise à jour ? > ");
			String input = new Scanner(System.in).nextLine();
			// update_repartiteur pascompris pascompris add 127.0.0.1 2012
			String[] params = input.split("\\s+");
			for (String string : params) {
				System.out.println(string);
			}
			if (params.length != 6) {
				System.out.println(
						"Usage : update_repartiteur <machine> <portRepartiteur> <add/delete> <machine à ajouter/supprimer> <nouveauPort>");
			} else {
				if (params[3].equals("add")) {
					if (calculateurs.containsKey(Integer.parseInt(params[5]))) {
						System.out.println("Impossible d'associer le nouveau WN : le port est déjà utilisé");
					} else {
						creerCalculateur(params[4], Integer.parseInt(params[5]));
					}
				} else if (params[3].equals("del")) {
					if (!calculateurs.containsKey(Integer.parseInt(params[5]))) {
						System.out.println("Impossible de supprimer le WN : l'association n'existe pas");
					} else {
						supprimerCalculateur(params[4], Integer.parseInt(params[5]));
					}
				}
			}
		}
	}

	private static void supprimerCalculateur(String machine, int port) {
		LOGGER.info("Suppression d'une association à un calculateur");
		System.out.println(calculateurs.size() + " calculateur(s)");
		calculateurs.remove(port);
		if (calculateurs.size() == 0) {
			redirectionCourante = null;
		} else {
			redirectionCourante = calculateurs.get(calculateurs.keySet().iterator().next());
		}
		System.out.println(calculateurs.size() + " calculateur(s)");
	}

	private static void creerCalculateur(String machine, int port) throws MalformedURLException, XmlRpcException {
		LOGGER.info("Ajout d'une association à un calculateur");
		System.out.println(calculateurs.size() + " calculateur(s)");
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://" + machine + ":" + port + "/calculateur"));
		config.setEnabledForExtensions(true);
		config.setConnectionTimeout(60 * 1000);
		config.setReplyTimeout(60 * 1000);

		XmlRpcClient client = new XmlRpcClient();

		// use Commons HttpClient as transport
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		// set configuration
		client.setConfig(config);

		redirectionCourante = client;

		calculateurs.put(port, client);
		System.out.println(calculateurs.size() + " calculateur(s)");
	}

	public int add(int i1, int i2) {
		int res = 0;
		try {
			res = this.transmettreLaRequete();
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private int transmettreLaRequete() throws XmlRpcException {
		LOGGER.info("Transmission de requête.");
		// make the a regular call
		Object[] params = new Object[] { new Integer(2), new Integer(3) };
		Integer result = (Integer) redirectionCourante.execute("Calculateur.add", params);
		LOGGER.info("RESULTAT : " + result);
		return result;
	}

	public int subtract(int i1, int i2) {
		return i1 - i2;
	}
}