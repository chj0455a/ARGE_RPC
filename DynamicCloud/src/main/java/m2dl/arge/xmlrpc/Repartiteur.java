package m2dl.arge.xmlrpc;




import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
//  import org.apache.xmlrpc.demo.webserver.proxy.impls.AdderImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

public class Repartiteur {
	private static Logger LOGGER = Logger.getLogger("Repartiteur");
	private static final int port = 8080;
	
	private static VMManager vMManager;
	private static HashMap<String, InfoCalculateur> calculateursForLoadBalancing;

	public static void main(String[] args) throws Exception {
		System.out.println("Repartiteur nouvelle version2");
		if (args[0] != null) {
			vMManager = VMManager.getGestionnaireRessource();

			WebServer webServer = new WebServer(Integer.parseInt(args[0]));

			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.load(Thread.currentThread().getContextClassLoader(), "XmlRpcServlet.properties");
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			webServer.start();
			System.out.println("Le repartiteur a d�marr� ...");
		}
		while (true) {
			System.out.print("Mise � jour ? > ");
			String input = new Scanner(System.in).nextLine();
			// update_repartiteur pascompris pascompris add 127.0.0.1 2012
			String[] params = input.split("\\s+");
			for (String string : params) {
				System.out.println(string);
			}
			if (params.length != 6) {
				System.out.println(
						"Usage : update_repartiteur <machine> <portRepartiteur> <add/delete> <machine � ajouter/supprimer> <nouveauPort>");
			} else {
				if (params[3].equals("add")) {
					if (vMManager.getCalculateurs().containsKey(Integer.parseInt(params[5]))) {
						System.out.println("Impossible d'associer le nouveau WN : le port est d�j� utilis�");
					} else {
						vMManager.creerCalculateur(params[4], Integer.parseInt(params[5]));
					}
				} else if (params[3].equals("del")) {
					if (!vMManager.getCalculateurs().containsKey(Integer.parseInt(params[5]))) {
						System.out.println("Impossible de supprimer le WN : l'association n'existe pas");
					} else {
						supprimerCalculateur(params[4], Integer.parseInt(params[5]));
					}
				}
			}
		}
	}

	private synchronized static void supprimerCalculateur(String machine, int port) {
		LOGGER.info("Suppression d'une association � un calculateur");
		System.out.println(vMManager.getCalculateurs().size() + " calculateur(s)");
		vMManager.getCalculateurs().remove(port);
		if (vMManager.getCalculateurs().size() == 0) {
			vMManager.setCalculateurCourant(null);
		} else {
			vMManager.setCalculateurCourant(vMManager.getCalculateurs().get(vMManager.getCalculateurs().keySet().iterator().next()));
		}
		System.out.println(vMManager.getCalculateurs().size() + " calculateur(s)");
	}

	

//	public int add(int i1, int i2) throws NotEnoughtResourcesException {
	public String add(int i1, int i2) throws NotEnoughtResourcesException, MissingImageException {
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
		LOGGER.info("/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1==i2) + "Calc courant : " + vMManager.getCalculateurCourant().getPort() + ". Sa charge : " + vMManager.getCalculateurCourant().getCharge_courante() + "/" + vMManager.getCalculateurCourant().getCharge_max() + " RES : " + res);
		return "/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1==i2) + "Calc courant : " + vMManager.getCalculateurCourant().getPort() + ". Sa charge : " + vMManager.getCalculateurCourant().getCharge_courante() + "/" + vMManager.getCalculateurCourant().getCharge_max() + " RES : " + res;
	}

	private synchronized int transmettreLaRequete(int i) throws XmlRpcException, NotEnoughtResourcesException, MissingImageException {
		LOGGER.info("Transmission de requ�te.");
		try {
			vMManager.choisirLeCalculateur();
			// make the a regular call
			Object[] params = new Object[] { new Integer(i), new Integer(3) };
			vMManager.augmenterLaCharge();
//			Integer result = (Integer) calculateurCourant.getClient().execute("Calculateur.add", params);
			Integer result = (Integer) vMManager.getCalculateurCourant().getClient().execute("Calculateur.add", params);
			LOGGER.info("RESULTAT : " + result);
			vMManager.diminuerLaCharge();
			return result;
		} catch (NotEnoughtResourcesException e) {
			throw e;
		}
	}
	

	public int subtract(int i1, int i2) {
		return i1 - i2;
	}
}