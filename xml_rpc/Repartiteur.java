
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
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

	private static HashMap<Integer, InfoCalculateur> calculateurs;

	private static InfoCalculateur calculateurCourant;

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
			calculateurCourant = null;
		} else {
			calculateurCourant = calculateurs.get(calculateurs.keySet().iterator().next());
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

		InfoCalculateur nouveau_calc = new InfoCalculateur(client, 0, 500, port);
		if (calculateurCourant == null) {
			calculateurCourant = nouveau_calc;
		}

		calculateurs.put(port, nouveau_calc);
		System.out.println(calculateurs.size() + " calculateur(s)");
		LOGGER.info("Calculateur courant : " + calculateurCourant.getPort());
	}

//	public int add(int i1, int i2) throws NotEnoughtResourcesException {
	public String add(int i1, int i2) throws NotEnoughtResourcesException {
		int res = 0;
		try {
			res = this.transmettreLaRequete();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (NotEnoughtResourcesException e) {
			e.printStackTrace();
		}
//		return res;
		return "Calc courant : " + calculateurCourant.getPort() + ". Sa charge : " + calculateurCourant.getCharge_courante() + "/" + calculateurCourant.getCharge_max();
	}

	private int transmettreLaRequete() throws XmlRpcException, NotEnoughtResourcesException {
		LOGGER.info("Transmission de requête.");
		try {
			choisirLeCalculateur();
			// make the a regular call
			Object[] params = new Object[] { new Integer(2), new Integer(3) };
			augmenterLaCharge();
			Integer result = (Integer) calculateurCourant.getClient().execute("Calculateur.add", params);
			LOGGER.info("RESULTAT : " + result);
			diminuerLaCharge();
			return result;
		} catch (NotEnoughtResourcesException e) {
			throw e;
		}
	}

	private void augmenterLaCharge() {
		LOGGER.info("+ Charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("+ Charge du noeud : " + calculateurCourant.getCharge_courante());
		calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() + 1);
		LOGGER.info("+ NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("+ NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
	}
	
	private void diminuerLaCharge() {
		LOGGER.info("- Charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("- Charge du noeud : " + calculateurCourant.getCharge_courante());
		calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() - 1);
		LOGGER.info("- NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("- NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
	}


	private void choisirLeCalculateur() throws NotEnoughtResourcesException {
		if (calculateurs.get(calculateurCourant.getPort()).getCharge_courante() >= 80. / 100.
				* calculateurs.get(calculateurCourant.getPort()).getCharge_max()) {
			changerLaRepartition();
		}

	}

	private void changerLaRepartition() throws NotEnoughtResourcesException {
		Iterator<Integer> iterator = calculateurs.keySet().iterator();
		Boolean trouve = false;
		Integer next = null;
		while (iterator.hasNext()) {
			next = iterator.next();
			if (!(next.intValue() == calculateurCourant.getPort()) && calculateurs.get(next)
					.getCharge_courante() < 80 / 100 * calculateurs.get(calculateurCourant.getPort()).getCharge_max()) {
				calculateurCourant = calculateurs.get(next);
				trouve = true;
			}
		}
		if (!trouve) {
			throw new NotEnoughtResourcesException(
					"Pas assez de calculateur ou charge trop importante sur les calculateurs actifs");
		}
	}

	public int subtract(int i1, int i2) {
		return i1 - i2;
	}
}