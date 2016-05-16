package m2dl.arge.xmlrpc;


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
    private static PrintWriter writer;
    private static Object repartiteur;
    private static int nouveauPort = 2012;
    private static List<InfoCalculateur> calculateurs;
    private String derniereTraceDeProcess = "";
    private static int nombreVM = 0;
    private static XmlRpcClient repartiteurClient;


    public static void main(String[] args) throws MissingImageException, FileNotFoundException,
            UnsupportedEncodingException, MalformedURLException, XmlRpcException {
        System.out.println("Bonjour ?");
        if (args.length == 2) {
            args[0] = (args[0].equals("localhost")) ? "127.0.0.1" : args[0];
            calculateurs = new ArrayList<>();

            /************** CONNEXION AU REPARTITEUR **************/
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            // config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
            LOGGER.info("http://" + args[0] + ":" + args[1] + "/xmlrpc");
            config.setServerURL(new URL("http://" + args[0] + ":" + args[1] + "/xmlrpc"));
            config.setEnabledForExtensions(true);
            config.setConnectionTimeout(60 * 1000);
            config.setReplyTimeout(60 * 1000);

            repartiteurClient = new CustomXmlRpcClient();

            // use Commons HttpClient as transport
            repartiteurClient.setTransportFactory(new XmlRpcCommonsTransportFactory(repartiteurClient));
            // set configuration
            repartiteurClient.setConfig(config);


            Object[] params = new Object[]{};
                System.out.println("LeVMManager est à l'écoute du répartiteur.");

            writer = new PrintWriter(new PrintWriter("logVMManagerLog.txt", "UTF-8"), true);
            Object[] calculateursResponse = (Object[]) repartiteurClient.execute("Repartiteur" +
                    ".getCalculateursLoadBalancing", params);
            List<InfoCalculateur> az = new ArrayList<>();
            for(Object calcObject : calculateursResponse)
            // Création d'un premier calculateur.
            {
                InfoCalculateur infoCalculateur = (InfoCalculateur) calcObject;
                calculateurs.add(infoCalculateur);
            }
            System.out.println("Création du premier calulateur.");
            creerCalculateur("127.0.0.1", nouveauPort);


            // La première VM est créée. Maintenant, on va faire tourner le VMManager en continu pour surveiller
            // l'activité des calculateurs et en ajouter / supprimer si besoin est
            while (true) {
                     /* D'abord, la suppression de VM inutiles

                     *  Le fonctionnement actuel est du semi-round robin, pour savoir quand ajouter un VM, on peut
                     * définir que si l'activité de toutes les VM est supérieur à un certain seuil (pas le max par
                     * précaution pour perdre le moins de messages possibles), alors on ajoute une VM
                     *
                    * Pour la suppression, on pourrait choisir de libérer une VM si la somme totale de CPU libre
                    * était égale à la CPU d'une VM mais par soucis de simplicité, on libérera les VM au cas par cas
                    * selon leur propre VM*/

                /********************** AJOUT DE VM **********************/
                double cpuForAllVM = 0.;
                for (InfoCalculateur calc :
                        calculateurs) {

                    double cpu = (double) calc.getClient().execute("Calculateur.getCPUCharge", new Object[]{});
                    LOGGER.info("" + cpu);
                    cpuForAllVM += cpu;
                }
                if (cpuForAllVM / calculateurs.size() > 80. && calculateurs.size() < 5) {
                    creerCalculateur(null, 0);
                }
            }
//        Thread clean = new Thread(){
//            @Override
//            public void run() {
//                List<InfoCalculateur> calcs = new ArrayList<>(VMManager.this.calculateurs.values());
//                for (InfoCalculateur calc:
//                calcs){
//                    if(!calc.getAdresse().equals(VMManager.this.calculateurCourant.getAdresse()) && calc
// .getCharge_courante() < 10. / 100. * calc.getCharge_max()) {
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
        } else {
            System.out.println("Erreur : usage -> ./VMManager <adresse du repartiteur> <port du repartiteur>");
        }
    }

    private synchronized void deleteVM(InfoCalculateur calc) {
        OSClient os = this.cloudmipConnection();
        this.calculateurs.remove(calc.getAdresse());
        os.compute().servers().delete(calc.getId());
    }

    public static synchronized InfoCalculateur creerCalculateur(String machine, int port) throws
            MissingImageException, XmlRpcException {
        // Création dudit calculateur
        OSClient os = cloudmipConnection();
        // Récupérer l'image
        nombreVM++;
//        Image img = os.compute().images().get("jUb");
        List<? extends Image> imagesList = os.compute().images().list();
        Image imageForNewVM = null;
        for (Image image :
                imagesList) {
            if (image.getName().equals("jcWNimg")) {
                imageForNewVM = image;
            }
        }

        Server server;
        if (imageForNewVM != null) {
            // Create a Server Model Object
            ArrayList<String> networks = new ArrayList<>();
            networks.add("c1445469-4640-4c5a-ad86-9c0cb6650cca");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            ServerCreate sc = Builders.server().name("z_WN_" + nombreVM + "_" + df.format(new Date())).networks
                    (networks).flavor("2").keypairName("jckey").image(imageForNewVM.getId()).build();

            server = os.compute().servers().boot(sc);
        } else {
            LOGGER.severe("L'image trueJCWNimg n'a pas été trouvée");
            throw new MissingImageException("L'image trueJCWNimg n'a pas été trouvée");
        }

        boolean wait = true;
        while (wait) {
            System.out.println("5 : " + os.compute().servers().get(server.getId()).getStatus().value());
            System.out.println("6 : " + os.compute().servers().get(server.getId()).getImage().getName());
            if (os.compute().servers().get(server.getId()).getStatus().equals(Server.Status.ACTIVE)) {
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
        for (List<? extends Address> adresse :
                server.getAddresses().getAddresses().values()) {
            for (Address addr :
                    adresse) {
                System.out.println("1 : " + addr.getAddr());

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
//        }

        Object[] params = new Object[]{nouveau_calc};
        client.execute("Repartiteur.addCalculateur", params);
        calculateurs.add(nouveau_calc);
        System.out.println(calculateurs.size() + " calculateur(s)");
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
}
