package m2dl.arge.xmlrpc;


import com.google.common.collect.Maps;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.compute.ext.DomainEntry;
import org.openstack4j.openstack.OSFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final PrintWriter writer;
    private int nouveauPort = 2012;
    private String derniereTraceDeProcess = "";
    private int nombreVM = 0;


    private VMManager() throws MissingImageException, FileNotFoundException, UnsupportedEncodingException {
        this.writer = new PrintWriter(new PrintWriter("VMManagerLog.txt", "UTF-8"), true);
        calculateurs = Maps.newHashMap();
        // Création d'un premier calculateur.
        creerCalculateur("127.0.0.1", this.nouveauPort);
        Thread clean = new Thread(){
            @Override
            public void run() {
                List<InfoCalculateur> calcs = new ArrayList<>(VMManager.this.calculateurs.values());
                for (InfoCalculateur calc:
                calcs){
                    if(!calc.getAdresse().equals(VMManager.this.calculateurCourant.getAdresse()) && calc.getCharge_courante() < 10. / 100. * calc.getCharge_max()) {
                        calc.setState(CalcState.WILL_BE_DELETED);
                        try {
                            Thread.sleep(10000);
                            VMManager.this.deleteVM(calc);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    private synchronized void deleteVM(InfoCalculateur calc) {
        OSClient os = this.cloudmipConnection();
        this.calculateurs.remove(calc.getAdresse());
        os.compute().servers().delete(calc.getId());
    }

    public static VMManager getGestionnaireRessource() throws MissingImageException {
        if (instance == null) {
            try {
                instance = new VMManager();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public synchronized InfoCalculateur creerCalculateur(String machine, int port) throws MissingImageException {
        // Création dudit calculateur
        OSClient os = this.cloudmipConnection();
        // Récupérer l'image
        this.nombreVM++;
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            ServerCreate sc = Builders.server().name("z_WN_" + this.nombreVM + "_" + new Date().toString()).networks(networks).flavor("2").keypairName("jckey").image(imageForNewVM.getId()).build();

            // Boot the Server
            server = os.compute().servers().bootAndWaitActive(sc, 120);

            // Créer une addresse ip privée
            // Créer la vm

            // Associer vm et ip


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

//        try {
//        	LOGGER.severe(System.getProperties().get("user.dir").toString());
//String path = System.getProperties().get("user.dir").toString().replace("\\", "/") + "/";
//path = (path.contains("/target/appassembler/bin/")) ? path : path + "/target/appassembler/bin/";
//            BufferedReader br = new BufferedReader(new FileReader(path+"Calculateur"));
//                String line = null;
//                while ((line = br.readLine()) != null) {
//                    this.content += line;
//                }

//Process processRes = Runtime.getRuntime().exec("bash " + path + "Calculateur " + port);
//            BufferedReader bri = new BufferedReader
//                    (new InputStreamReader(processRes.getInputStream()));
//            BufferedReader bre = new BufferedReader
//                    (new InputStreamReader(processRes.getErrorStream()));
//            PrintWriter pR = new PrintWriter(new PrintWriter("Calculateur" + this.nouveauPort + "Log.txt", "UTF-8"), true);
//            String lineR;
//            while ((lineR = bri.readLine()) != null) {
//                pR.println(lineR);
//                System.out.println(lineR);
//                this.derniereTraceDeProcess += lineR;
//                LOGGER.info("LL : " +lineR);
//                break;
//            }
//            bri.close();
//            while ((lineR = bre.readLine()) != null) {
//                pR.println(lineR);
//                System.out.println(lineR);
//                this.derniereTraceDeProcess += lineR;
//                LOGGER.info("LL : " +lineR);
//                break;
//            }
//            bre.close();
//            processRes.destroy();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        for (List<? extends Address> adresse:
        server.getAddresses().getAddresses().values()) {
            for (Address addr:
                 adresse) {
                this.writer.println("1 : " + addr.getAddr());

            }
        }

        Iterator<? extends Address> it = server.getAddresses().getAddresses().get("private").iterator();
        while (it.hasNext())
        {
            this.writer.println("2 : " + it.next().getAddr());
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

        InfoCalculateur nouveau_calc = new InfoCalculateur(client, 0, 500, 2012, adresse, id);
//        if (calculateurCourant == null) {
        calculateurCourant = nouveau_calc;
//        }

        calculateurs.put(adresse, nouveau_calc);
        System.out.println(calculateurs.size() + " calculateur(s)");
        LOGGER.info("Calculateur courant : " + calculateurCourant.getPort());
        this.nouveauPort++;
        return nouveau_calc;
    }

    private OSClient cloudmipConnection() {
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
     * @throws NotEnoughtResourcesException
     */
    public synchronized void choisirLeCalculateur() throws NotEnoughtResourcesException, MissingImageException {
        if (calculateurs.get(calculateurCourant.getAdresse()).getCharge_courante() >= 80. / 100.
                * calculateurs.get(calculateurCourant.getAdresse()).getCharge_max()) {
            changerLaRepartition();
        }

    }

    /**
     * Cherche un nouveau calculateur peu actif ou en créer un nouveau. Le nouveau calculateur choisit est disponible au travers du calculateur courant du gestionnaire.
     *
     * @throws NotEnoughtResourcesException
     */
    private synchronized void changerLaRepartition() throws NotEnoughtResourcesException, MissingImageException {
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
//            throw new NotEnoughtResourcesException(
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
