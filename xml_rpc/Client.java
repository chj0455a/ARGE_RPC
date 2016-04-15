
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import com.google.common.collect.Lists;
//  import org.apache.xmlrpc.demo.proxy.Adder;

public class Client {
	private static Logger LOGGER = Logger.getLogger("Client");
	private static String commande = null;
	private static String nb_requete = null;
	private static String machine = null;
	private static String port = null;
	private static Boolean argOK = false;

	public static void main(String[] args) throws Exception {
		parseArgs(args);
		if (argOK && commande.equals("client")) {
			machine = (machine.equals("localhost")) ? "127.0.0.1" : machine;
			// create configuration
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			// config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
			config.setServerURL(new URL("http://" + machine + ":" + port + "/xmlrpc"));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(60 * 1000);
			config.setReplyTimeout(60 * 1000);

			XmlRpcClient client = new XmlRpcClient();

			// use Commons HttpClient as transport
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			// set configuration
			client.setConfig(config);

			while (true) {
				// make the a regular call
				Object[] params = new Object[] { new Integer(2), new Integer(3) };
				int nb = Integer.parseInt(nb_requete);
				for (int i = 0; i < nb; i++) {
					LOGGER.info("Envoi de la requête numéro : " + i);
					Integer result = 0;
					String result2 = "";
					try {
//						result = (Integer) client.execute("Calculateur.add", params);
						
						result2 = (String) client.execute("Calculateur.add", params);
					} catch (XmlRpcException e) {
						LOGGER.severe(
								"Un probleme est survenu, le calculateur doit être créé et ajouté au répartiteur. Le calculateur actif dans le répartiteur doit exister. Le répartiteur doit être créé. Vérifier la concordance des ports.");
						e.printStackTrace();
					}
//					System.out.println("2 + 3 = " + result);
					System.out.println("2 + 3 = " + result2);

				}
				Thread.sleep(1000);

			}
		}

	}

	private static void parseArgs(String[] args) {
		List<String> arguments = Lists.newArrayList();
		for (String string : args) {
			arguments.add(string);
		}

		if (arguments.size() == 4) {
			argOK = true;
			commande = arguments.get(0);
			nb_requete = arguments.get(1);
			machine = arguments.get(2);
			port = arguments.get(3);
		}
	}
}