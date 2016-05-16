package m2dl.arge.xmlrpc;


import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.xmlrpc.XmlRpcException;
//  import org.apache.xmlrpc.demo.webserver.proxy.impls.AdderImpl;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

public class Repartiteur {
    private static final double LIMIT = 400.00;
    private static Logger LOGGER = Logger.getLogger("Repartiteur");
    private static final int port = 8080;

    private static VMManager vMManager;
    public static List<InfoCalculateur> calculateursLoadBalancing;
    private static PrintWriter writer;
    public static int calcIndexLoadBalance = 0;
    public static int toto = 0;


    public static void main(String[] args) throws Exception {
        System.out.println("Repartiteur nouvelle version2");
        writer = new PrintWriter(new PrintWriter("logRepartiteur.txt", "UTF-8"), true);
        if (args[0] != null) {
            calculateursLoadBalancing = new ArrayList<>();
            // Le Repartiteur ne doit pas être intelligent : c'est le VMManager qui va gérer cet attribut

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
            while (true) {
                int i = 1;
            }
        }
    }

    public void creerCalculateur(String adresse, int port) throws MalformedURLException {
        System.out.println(this.calculateursLoadBalancing.size() + " calculateur(s)");
        // Ajout de la référence vers le nouveau calculateur
        LOGGER.info("Ajout d'une association � un calculateur");
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
//            config.setServerURL(new URL("http://" + machine + ":" + port + "/calculateur"));
        config.setServerURL(new URL("http://" + adresse + ":" + port + "/calculateur"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        CustomXmlRpcClient client = new CustomXmlRpcClient();

        // use Commons HttpClient as transport
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        // set configuration
        client.setConfig(config);

        InfoCalculateur nouveau_calc = new InfoCalculateur(client, 0, 500, port, adresse, null, CalcState.OK);
        this.calculateursLoadBalancing.add(nouveau_calc);
        System.out.println(this.calculateursLoadBalancing.size() + " calculateur(s)");
    }

    public synchronized void supprimerCalculateur(String machine, int port) {
        LOGGER.info("Suppression d'une association � un calculateur");
        System.out.println(calculateursLoadBalancing.size() + " calculateur(s)");

        InfoCalculateur calcToRemove = null;
        for (InfoCalculateur calculateur :
                calculateursLoadBalancing) {
            if (calculateur.getAdresse().equals(machine) && calculateur.getPort() == port) {
                calcToRemove = calculateur;
            }
        }

        calculateursLoadBalancing.remove(calcToRemove);
        System.out.println(calculateursLoadBalancing.size() + " calculateur(s)");
    }


    //	public int add(int i1, int i2) throws CalculatorsManagementException {
    public String add(int i1, int i2) throws CalculatorsManagementException, MissingImageException,
            NotEnoughtResourceException {
        LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!" + i1 + " " + i2);
        int res = 0;
        try {
            res = transmettreLaRequete(i1);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        } catch (CalculatorsManagementException e) {
            e.printStackTrace();
        }
//		return res;
        LOGGER.info("/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1 == i2) + "Calc courant : " +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getPort() + ". Sa charge : " +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getCharge_courante() + "/" +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getCharge_max() + " RES : " + res);
        return "/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\" + (i1 == i2) + "Calc courant : " +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getPort() + ". Sa charge : " +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getCharge_courante() + "/" +
                calculateursLoadBalancing.get(calcIndexLoadBalance).getCharge_max() + " RES : " + res;
    }

    public synchronized int transmettreLaRequete(int i) throws XmlRpcException, CalculatorsManagementException,
            MissingImageException, NotEnoughtResourceException {
        LOGGER.info("Transmission de requ�te.");
        // TODO : PLUS BESOIN
//			vMManager.choisirLeCalculateur();
        // make the a regular call
        // Choisir le calculateur
        Integer result = null;
        if (calcIndexLoadBalance <= calculateursLoadBalancing.size()) {
            InfoCalculateur calculateur = choisirCalculateur();

            calculateur.setCharge_courante(calculateur.getCharge_courante() + 1.);
            Object[] params = new Object[]{new Integer(i), new Integer(3)};
//			Integer result = (Integer) calculateurCourant.getClient().execute("Calculateur.add", params);


            // TODO CHANGEMENT EN LOAD BALANCE
            System.out.println
                    ("------------------------------------------------------------------------------------------------------------------------------------------------- Transmission au calculateur " + calculateur.getAdresse() + ":" + calculateur.getPort() + " de charge " + calculateur.getCharge_courante());
            writer.println
                    ("------------------------------------------------------------------------------------------------------------------------------------------------- Transmission au calculateur " + calculateur.getAdresse() + ":" + calculateur.getPort() + " de charge " + calculateur.getCharge_courante());
            result = (Integer) calculateur.getClient().execute("Calculateur.add", params);

            calcIndexLoadBalance = (calcIndexLoadBalance + 1) % calculateursLoadBalancing.size();

            LOGGER.info("RESULTAT : " + result);
            if (calculateur.getCharge_courante() >= 0) {
                calculateur.setCharge_courante(calculateur.getCharge_courante() - 1.);
            }
        }
        if (result == null) {
            throw new CalculatorsManagementException("Mauvaise gestion des calculateurs");
        }
        return result;
    }

    /**
     * Le Répartiteur a un brin d'intelligence : il est censé redistribué la charge aux calculateurs moins encombrés
     * si certain sont trop chargés au lieu de faire du roundrobin pur et dur
     * @return InfoCalculateur
     * @throws NotEnoughtResourceException
     */
    public InfoCalculateur choisirCalculateur() throws NotEnoughtResourceException {
        LOGGER.info("CHOISIR le calculateur");
        InfoCalculateur choosenCalc = null;

        System.out.println("taille calc : " + calculateursLoadBalancing.size());
        writer.println("taille calc : " + calculateursLoadBalancing.size());
        // DEBUG
        for (InfoCalculateur calc :
                calculateursLoadBalancing) {
            System.out.println(calc.getAdresse());
            writer.println(calc.getAdresse());
            System.out.println(calc.getPort());
            writer.println(calc.getPort());
            System.out.println(calc.getCharge_courante());
            writer.println(calc.getCharge_courante());
            System.out.println(calc.getCharge_max());
            writer.println(calc.getCharge_max());
            System.out.println(calc.getState());
            writer.println(calc.getState());
        }
        //

        for (int i = 0; i < calculateursLoadBalancing.size(); i++) {
            InfoCalculateur infoCalculateur = calculateursLoadBalancing.get((calcIndexLoadBalance + i) %
                    calculateursLoadBalancing.size());
            if (infoCalculateur.getCharge_courante() < LIMIT && infoCalculateur.getState().equals(CalcState.OK)) {
                choosenCalc = infoCalculateur;
                break;
            }
        }
        if (choosenCalc == null) {
            // Le Repartiteur est bête, si pas assez de ressource, ce n'est pas à lui de déclencher la création d'un
            // calculateur, même via le VMManager
            throw new NotEnoughtResourceException("Tout les calculateurs sont saturés");
        }
        return choosenCalc;
    }

    public List<InfoCalculateur> getCalculateursLoadBalancing() {
        return calculateursLoadBalancing;
    }

    public int getCalcIndexLoadBalance() {
        return calcIndexLoadBalance;
    }

    public void setCalculateursLoadBalancing(List<InfoCalculateur> calculateursLoadBalancing) {
        this.calculateursLoadBalancing = calculateursLoadBalancing;
    }

    public void setCalcIndexLoadBalance(int calcIndexLoadBalance) {
        this.calcIndexLoadBalance = calcIndexLoadBalance;
    }

    public int linkRequest(String requete, String adresseCalc, String portCalc) throws MalformedURLException {

        System.out.print("Mise � jour ? > ");
        // update_repartiteur pascompris pascompris add 127.0.0.1 2012
        toto = Integer.parseInt(portCalc);
        List<String> adresses = getUsedAddresses();
        if (requete.equals("add")) {
            if (adresses.contains(Integer.parseInt(portCalc))) {
                System.out.println("Impossible d'associer le nouveau WN : le port est d�j� utilis�");
                return 1;
            } else {
                creerCalculateur(adresseCalc, Integer.parseInt(portCalc));
                return 2;
            }
        } else if (requete.equals("del")) {
            if (!adresses.contains(Integer.parseInt(portCalc))) {
                System.out.println("Impossible de supprimer le WN : l'association n'existe pas");
                return 3;
            } else {
                supprimerCalculateur(adresseCalc, Integer.parseInt(portCalc));
                return 4;
            }
        }
        return 1;
    }

    public List<String> getUsedAddresses() {
        return (List<String>) CollectionUtils.collect(calculateursLoadBalancing,
                new BeanToPropertyValueTransformer("port"));
    }

    public static void setWriter(PrintWriter writer) {
        Repartiteur.writer = writer;
    }

    public Repartiteur getRepartiteurInstance() {
        LOGGER.info("getRepartiteurInstance");
        return this;
    }

    public boolean addCalculateur(InfoCalculateur infoCalculateur) {
        LOGGER.info("Recetion d'une requête d'ajout de calculalteur : " + infoCalculateur.toString());
        this.calculateursLoadBalancing.add(infoCalculateur);
        return true;
    }

    public boolean removeCalculateur(InfoCalculateur infoCalculateur) {
        return this.calculateursLoadBalancing.remove(infoCalculateur);
    }
}