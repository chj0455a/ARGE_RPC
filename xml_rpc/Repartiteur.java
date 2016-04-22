
import java.awt.GradientPaint;
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
	
	private static GestionnaireRessource gestionnaireRessource = GestionnaireRessource.getGestionnaireRessource();

	public static void main(String[] args) throws Exception {
		if (args[0] != null) {
			

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
					if (gestionnaireRessource.getCalculateurs().containsKey(Integer.parseInt(params[5]))) {
						System.out.println("Impossible d'associer le nouveau WN : le port est déjà utilisé");
					} else {
						gestionnaireRessource.creerCalculateur(params[4], Integer.parseInt(params[5]));
					}
				} else if (params[3].equals("del")) {
					if (!gestionnaireRessource.getCalculateurs().containsKey(Integer.parseInt(params[5]))) {
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
		System.out.println(gestionnaireRessource.getCalculateurs().size() + " calculateur(s)");
		gestionnaireRessource.getCalculateurs().remove(port);
		if (gestionnaireRessource.getCalculateurs().size() == 0) {
			gestionnaireRessource.setCalculateurCourant(null);
		} else {
			gestionnaireRessource.setCalculateurCourant(gestionnaireRessource.getCalculateurs().get(gestionnaireRessource.getCalculateurs().keySet().iterator().next()));
		}
		System.out.println(gestionnaireRessource.getCalculateurs().size() + " calculateur(s)");
	}

	

//	public int add(int i1, int i2) throws NotEnoughtResourcesException {
	public String add(int i1, int i2) throws NotEnoughtResourcesException {
		LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!" + i1 + " " + i2);
		int res = 0;
		try {
			res = this.transmettreLaRequete(i1);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (NotEnoughtResourcesException e) {
			e.printStackTrace();
		}
//		return res;
		LOGGER.info("/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1==i2) + "Calc courant : " + gestionnaireRessource.getCalculateurCourant().getPort() + ". Sa charge : " + gestionnaireRessource.getCalculateurCourant().getCharge_courante() + "/" + gestionnaireRessource.getCalculateurCourant().getCharge_max() + " RES : " + res);
		return "/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1==i2) + "Calc courant : " + gestionnaireRessource.getCalculateurCourant().getPort() + ". Sa charge : " + gestionnaireRessource.getCalculateurCourant().getCharge_courante() + "/" + gestionnaireRessource.getCalculateurCourant().getCharge_max() + " RES : " + res;
	}

	private int transmettreLaRequete(int i) throws XmlRpcException, NotEnoughtResourcesException {
		LOGGER.info("Transmission de requête.");
		try {
			gestionnaireRessource.choisirLeCalculateur();
			// make the a regular call
			Object[] params = new Object[] { new Integer(i), new Integer(3) };
			gestionnaireRessource.augmenterLaCharge();
//			Integer result = (Integer) calculateurCourant.getClient().execute("Calculateur.add", params);
			Integer result = (Integer) gestionnaireRessource.getCalculateurCourant().getClient().execute("Calculateur.add", params);
			LOGGER.info("RESULTAT : " + result);
			gestionnaireRessource.diminuerLaCharge();
			return result;
		} catch (NotEnoughtResourcesException e) {
			throw e;
		}
	}
	

	public int subtract(int i1, int i2) {
		return i1 - i2;
	}
}