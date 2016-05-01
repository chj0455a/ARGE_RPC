package m2dl.arge.xmlrpc;



import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import com.google.common.collect.Maps;

public class GestionnaireRessource {
	private static Logger LOGGER = Logger.getLogger("GestionnaireRessource");
	private static GestionnaireRessource instance;
	private static InfoCalculateur calculateurCourant;
	private static HashMap<Integer, InfoCalculateur> calculateurs;
	
	private GestionnaireRessource ()
	{
		calculateurs = Maps.newHashMap();
	}
	
	public static GestionnaireRessource getGestionnaireRessource() 
	{
		if(instance == null)
		{
			instance = new GestionnaireRessource();
		}
		return instance;
	}
	
	public void creerCalculateur(String machine, int port) throws MalformedURLException, XmlRpcException {
		LOGGER.info("Ajout d'une association � un calculateur");
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
	public void augmenterLaCharge() {
		LOGGER.info("+ Charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("+ Charge du noeud : " + calculateurCourant.getCharge_courante());
		calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() + 1);
		LOGGER.info("+ NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("+ NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
	}
	
	public void diminuerLaCharge() {
		LOGGER.info("- Charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("- Charge du noeud : " + calculateurCourant.getCharge_courante());
		calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() - 1);
		LOGGER.info("- NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getPort()) + " : " + calculateurs.get(calculateurCourant.getPort()).getCharge_courante());
		LOGGER.info("- NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
	}


	public void choisirLeCalculateur() throws NotEnoughtResourcesException {
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

	public static InfoCalculateur getCalculateurCourant() {
		return calculateurCourant;
	}

	public static void setCalculateurCourant(InfoCalculateur calculateurCourant) {
		GestionnaireRessource.calculateurCourant = calculateurCourant;
	}

	public static HashMap<Integer, InfoCalculateur> getCalculateurs() {
		return calculateurs;
	}

	public static void setCalculateurs(HashMap<Integer, InfoCalculateur> calculateurs) {
		GestionnaireRessource.calculateurs = calculateurs;
	}
	
	
}
