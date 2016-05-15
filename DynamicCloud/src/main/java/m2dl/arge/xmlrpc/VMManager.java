package m2dl.arge.xmlrpc;


import com.google.common.collect.Maps;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
import org.openstack4j.openstack.OSFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class VMManager {
    private static Logger LOGGER = Logger.getLogger("VMManager");
    private static VMManager instance;
    private static InfoCalculateur calculateurCourant;
    private static HashMap<String, InfoCalculateur> calculateurs;
    private static PrintWriter writer;
    private static Repartiteur repartiteur;
    private static int nouveauPort = 2012;
    private String derniereTraceDeProcess = "";
    private static int nombreVM = 0;


    public static void main(String[] args) throws MissingImageException, FileNotFoundException,
            UnsupportedEncodingException, MalformedURLException, XmlRpcException {
        if(args.length != 2) {

            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            // config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
            config.setServerURL(new URL("http://" + args[0] + ":" + args[1] + "/xmlrpc"));
            config.setEnabledForExtensions(true);
            config.setConnectionTimeout(60 * 1000);
            config.setReplyTimeout(60 * 1000);

            XmlRpcClient client = new XmlRpcClient();

            // use Commons HttpClient as transport
            client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
            // set configuration
            client.setConfig(config);
            Object[] params = new Object[]{new String(args[2]), new String(args[3]), new String(args[4])};
            repartiteur = (Repartiteur) client.execute("Repartiteur.getRepartiteurInstance", params);


            writer = new PrintWriter(new PrintWriter("logVMManagerLog.txt", "UTF-8"), true);
            calculateurs = Maps.newHashMap();
            // Création d'un premier calculateur.
            creerCalculateur("127.0.0.1", nouveauPort);

//        Thread clean = new Thread(){
//            @Override
//            public void run() {
//                List<InfoCalculateur> calcs = new ArrayList<>(VMManager.this.calculateurs.values());
//                for (InfoCalculateur calc:
//                calcs){
//                    if(!calc.getAdresse().equals(VMManager.this.calculateurCourant.getAdresse()) && calc.getCharge_courante() < 10. / 100. * calc.getCharge_max()) {
//                        calc.setState(CalcState.WILL_BE_DELETED);
//                        try {
//                            Thread.sleep(10000);
//                            VMManager.this.deleteVM(calc);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        };
        }
    }

    private synchronized void deleteVM(InfoCalculateur calc) {
        OSClient os = this.cloudmipConnection();
        this.calculateurs.remove(calc.getAdresse());
        os.compute().servers().delete(calc.getId());
    }

    public static synchronized InfoCalculateur creerCalculateur(String machine, int port) throws MissingImageException {
        // Création dudit calculateur
        OSClient os = cloudmipConnection();
        // Récupérer l'image
        nombreVM++;
//        Image img = os.compute().images().get("jUb");
        List<? extends Image> imagesList = os.compute().images().list();
        Image imageForNewVM = null;
        for (Image image :
                imagesList) {
            if (image.getName().equals("trueJCWNimg")) {
                imageForNewVM = image;
            }
        }

        Server server;
        if (imageForNewVM != null) {
            // Create a Server Model Object
            ArrayList<String> networks = new ArrayList<>();
            networks.add("c1445469-4640-4c5a-ad86-9c0cb6650cca");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            ServerCreate sc = Builders.server().name("z_WN_" + this.nombreVM + "_" + df.format(new Date())).networks(networks).flavor("2").keypairName("jckey").image(imageForNewVM.getId()).build();

            server = os.compute().servers().boot(sc);
        } else {
            LOGGER.severe("L'image jUb n'a pas été trouvée");
            throw new MissingImageException("L'image jUb n'a pas été trouvée");
        }

        boolean wait = true;
        while(wait) {
            this.writer.println("5 : " + os.compute().servers().get(server.getId()).getStatus().value());
            this.writer.println("6 : " + os.compute().servers().get(server.getId()).getImage().getName());
            if(os.compute().servers().get(server.getId()).getStatus().equals(Server.Status.ACTIVE)) {
                wait = false;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

server = os.compute().servers().get(server.getId());
        for (List<? extends Address> adresse:
        server.getAddresses().getAddresses().values()) {
            for (Address addr:
                 adresse) {
                this.writer.println("1 : " + addr.getAddr());

            }
        }

       String adresse = server.getAddresses().getAddresses().get("private").get(0).getAddr().toString();
        String id = server.getId();
        System.out.println(id + " " + adresse);

        // Ajout de la référence vers le nouveau calculateur
        LOGGER.info("Ajout d'une association � un calculateur");
        System.out.println(calculateurs.size() + " calculateur(s)");
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
//            config.setServerURL(new URL("http://" + machine + ":" + port + "/calculateur"));
            config.setServerURL(new URL("http://" + adresse + ":2012/calculateur"));
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

        InfoCalculateur nouveau_calc = new InfoCalculateur(client, 0, 500, 2012, adresse, id, CalcState.OK);
//        if (calculateurCourant == null) {
        calculateurCourant = nouveau_calc;
//        }

        calculateurs.put(adresse, nouveau_calc);
        System.out.println(calculateurs.size() + " calculateur(s)");
        LOGGER.info("Calculateur courant : " + calculateurCourant.getPort());
        this.nouveauPort++;
        return nouveau_calc;
    }

    private static OSClient cloudmipConnection() {
        LOGGER.info("Try to connect to cloudmip vm");
        OSClient os = OSFactory.builder()
                .endpoint("http://195.220.53.61:5000/v2.0")
                .credentials("ens27", "BTAAMU").tenantName("service")
                .authenticate();
        LOGGER.info("Connection succeed");
        System.out.println(os);
//        System.out.println(os.images().list());

        return os;
    }

    public synchronized void augmenterLaCharge() {
        LOGGER.info("+ Charge du noeud " + calculateurs.get(calculateurCourant.getAdresse()) + " : " + calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante());
        LOGGER.info("+ Charge du noeud : " + calculateurCourant.getCharge_courante());
        calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() + 1);
        LOGGER.info("+ NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getAdresse()) + " : " + calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante());
        LOGGER.info("+ NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
    }

    public synchronized void diminuerLaCharge() {
        LOGGER.info("- Charge du noeud " + calculateurs.get(calculateurCourant.getAdresse()) + " : " + calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante());
        LOGGER.info("- Charge du noeud : " + calculateurCourant.getCharge_courante());
        if (calculateurCourant.getCharge_courante() > 0) {
            calculateurCourant.setCharge_courante(calculateurCourant.getCharge_courante() - 1);
        }
        LOGGER.info("- NOUVELLE charge du noeud " + calculateurs.get(calculateurCourant.getAdresse()) + " : " + calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante());
        LOGGER.info("- NOUVELLE charge du noeud : " + calculateurCourant.getCharge_courante());
    }

    /**
     * Permet au gestionnaire de définir quel calculateur doit être utilisé, il doit être utilisé depuis le calculateur courant du gestionnaire.
     *
     * @throws CalculatorsManagementException
     */
    public synchronized void choisirLeCalculateur() throws CalculatorsManagementException, MissingImageException {
        if (calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante() >= 80. / 100.
                * calculateurs.get(calculateurCourant.getAdresse()).getCharge_max()) {
            changerLaRepartition();
        }

    }

    /**
     * Cherche un nouveau calculateur peu actif ou en créer un nouveau. Le nouveau calculateur choisit est disponible au travers du calculateur courant du gestionnaire.
     *
     * @throws CalculatorsManagementException
     */
    private synchronized void changerLaRepartition() throws CalculatorsManagementException, MissingImageException {
        Iterator<String> iterator = calculateurs.keySet().iterator();
        Boolean trouve = false;
        String next = null;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (!(next.equals(calculateurCourant.getAdresse())) && calculateurs.get(next)
                    .getCharge_courante() < 80 / 100 * calculateurs.get(calculateurCourant.getAdresse()).getCharge_max() && this.calculateurs.get(next).getState().equals(CalcState.OK)) {
                calculateurCourant = calculateurs.get(next);
                trouve = true;
            }
        }
        if (!trouve) {
//            throw new CalculatorsManagementException(
//                    "Pas assez de calculateur ou charge trop importante sur les calculateurs actifs");
            // Création d'un nouveau calculateur
            calculateurCourant = this.creerCalculateur("127.0.0.1", this.nouveauPort);
        }
    }

    public static InfoCalculateur getCalculateurCourant() {
        return calculateurCourant;
    }

    public static void setCalculateurCourant(InfoCalculateur calculateurCourant) {
        VMManager.calculateurCourant = calculateurCourant;
    }

    public synchronized static HashMap<String, InfoCalculateur> getCalculateurs() {
        return calculateurs;
    }

    public synchronized static void setCalculateurs(HashMap<String, InfoCalculateur> calculateurs) {
        VMManager.calculateurs = calculateurs;
    }

    public int getNouveauPort() {
        return nouveauPort;
    }

    public String getDerniereTraceDeProcess() {
        return derniereTraceDeProcess;
    }
}
