package m2dl.arge.xmlrpc;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import com.google.common.collect.Maps;

public class GestionnaireRessource {
    private static Logger LOGGER = Logger.getLogger("GestionnaireRessource");
    private static GestionnaireRessource instance;
    private static InfoCalculateur calculateurCourant;
    private static HashMap<Integer, InfoCalculateur> calculateurs;
    private int nouveauPort = 2012;
    private String derniereTraceDeProcess = "";
    private String content = "";

    private GestionnaireRessource() {
        calculateurs = Maps.newHashMap();
        // Création d'un premier calculateur.
        creerCalculateur("127.0.0.1", this.nouveauPort);
    }

    public static GestionnaireRessource getGestionnaireRessource() {
        if (instance == null) {
            instance = new GestionnaireRessource();
        }
        return instance;
    }

    public void creerCalculateur(String machine, int port) {
        // Création dudit calculateur
        try {
            String path = System.getProperties().get("user.dir").toString().replace("\\", "/") + "/target/appassembler/bin/";
            BufferedReader br = new BufferedReader(new FileReader(path+"Calculateur"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    this.content += line;
                }

            Process processRes = Runtime.getRuntime().exec(path + "Calculateur.bat 2012");
            BufferedReader bri = new BufferedReader
                    (new InputStreamReader(processRes.getInputStream()));
            BufferedReader bre = new BufferedReader
                    (new InputStreamReader(processRes.getErrorStream()));
            PrintWriter pR = new PrintWriter(new PrintWriter("Calculateur" + this.nouveauPort + "Log.txt", "UTF-8"), true);
            String lineR;
            while ((lineR = bri.readLine()) != null) {
                pR.println(lineR);
                System.out.println(lineR);
                this.derniereTraceDeProcess += lineR;
                LOGGER.info("LL : " +lineR);
                break;
            }
            bri.close();
//            while ((lineR = bre.readLine()) != null) {
//                pR.println(lineR);
//                System.out.println(lineR);
//                this.derniereTraceDeProcess += lineR;
//                LOGGER.info("LL : " +lineR);
//                break;
//            }
            bre.close();
//            processRes.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }


        // Ajout de la référence vers le nouveau calculateur
        LOGGER.info("Ajout d'une association � un calculateur");
        System.out.println(calculateurs.size() + " calculateur(s)");
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://" + machine + ":" + port + "/calculateur"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
        this.nouveauPort++;
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

    /**
     * Permet au gestionnaire de définir quel calculateur doit être utilisé, il doit être utilisé depuis le calculateur courant du gestionnaire.
     * @throws NotEnoughtResourcesException
     */
    public void choisirLeCalculateur() throws NotEnoughtResourcesException {
        if (calculateurs.get(calculateurCourant.getPort()).getCharge_courante() >= 80. / 100.
                * calculateurs.get(calculateurCourant.getPort()).getCharge_max()) {
            changerLaRepartition();
        }

    }

    /**
     * Cherche un nouveau calculateur peu actif ou en créer un nouveau. Le nouveau calculateur choisit est disponible au travers du calculateur courant du gestionnaire.
     *
     * @throws NotEnoughtResourcesException
     */
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
//            throw new NotEnoughtResourcesException(
//                    "Pas assez de calculateur ou charge trop importante sur les calculateurs actifs");
            // Création d'un nouveau calculateur
            this.creerCalculateur("127.0.0.1", this.nouveauPort);
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

    public int getNouveauPort() {
        return nouveauPort;
    }

    public String getDerniereTraceDeProcess() {
        return derniereTraceDeProcess;
    }
}
