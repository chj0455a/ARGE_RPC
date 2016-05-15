package m2dl.arge.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by danton on 15/05/16.
 */
public class UpdateRepartiteur {

    public static void main(String[] args) throws MalformedURLException, XmlRpcException {
        if (args.length != 5) {
            System.out.println(
                    "Usage : update_repartiteur <machine> <portRepartiteur> <add/delete> <machine � " +
                            "ajouter/supprimer> <nouveauPort>");
        } else {
            args[0] = (args[0].equals("localhost")) ? "127.0.0.1" : args[0];
            args[3] = (args[3].equals("localhost")) ? "127.0.0.1" : args[3];
            // create configuration
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
            int res = (int) client.execute("Repartiteur.linkRequest", params);
            switch (res)
            {
                case 1 :
                default:
                    System.out.println("Impossible d'ajouter : le calculateur existe déjà.");
                    break;
                case 2 :
                    System.out.println("Ajout OK.");
                    break;
                case 3 :
                    System.out.println("Impossible de supprimer : le calculateur n'existe pas.");
                    break;
                case 4 :
                    System.out.println("Suppression OK.");
            }
        }
    }
}
