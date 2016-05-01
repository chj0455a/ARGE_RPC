package m2dl.arge.xmlrpc;




import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import com.google.common.collect.Lists;
//  import org.apache.xmlrpc.demo.proxy.Adder;

public class Client {
	private static Logger LOGGER = Logger.getLogger("Client");
	private static String nb_requete = null;
	private static String machine = null;
	private static String port = null;
	private static Boolean argOK = false;
	private static PrintWriter writer;
	private static XmlRpcClient client;

	public static void main(String[] args) throws Exception {
		parseArgs(args);
		if (argOK) {
			machine = (machine.equals("localhost")) ? "127.0.0.1" : machine;
			// create configuration
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			// config.setServerURL(new URL("http://127.0.0.1:8080/xmlrpc"));
			config.setServerURL(new URL("http://" + machine + ":" + port + "/xmlrpc"));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(60 * 1000);
			config.setReplyTimeout(60 * 1000);

			client = new XmlRpcClient();

			// use Commons HttpClient as transport
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			// set configuration
			client.setConfig(config);
			writer = new PrintWriter(new PrintWriter("the-file-name.txt", "UTF-8"), true);
			writer.println("test");
			int j = 0;
			while (true) {
				// make the a regular call
				int nb = Integer.parseInt(nb_requete);
				if (j < 500) {

					for (int i = 0; i < 500; i++) {

						j++;
						LOGGER.info("Envoi de la requ�te num�ro : " + i);
						Integer result = 0;
						// result = (Integer) client.execute("Calculateur.add",
						// params);

						new Thread(new Runnable() {
							public void run() {
								String result2 = "";
								try {
									Object[] params = new Object[] { new Integer(10000), new Integer(3) };
									client.executeAsync("Calculateur.add", params, new AsyncCallback() {

										public void handleResult(XmlRpcRequest arg0, Object arg1) {
											System.out.println("File is created!");

											synchronized (writer) {
												writer.println(arg0.toString() + "\n" + arg1.toString() + "\n\n");
LOGGER.info(arg0.toString() + "\n" + arg1.toString() + "\n\n");
											}
										}

										public void handleError(XmlRpcRequest arg0, Throwable arg1) {
											LOGGER.severe(
													"Un probleme est survenu, le calculateur doit �tre cr�� et ajout� au r�partiteur. Le calculateur actif dans le r�partiteur doit exister. Le r�partiteur doit �tre cr��. V�rifier la concordance des ports.");
											arg1.printStackTrace();
										}
									});
								} catch (XmlRpcException e) {
									LOGGER.severe(
											"Un probleme est survenu, le calculateur doit �tre cr�� et ajout� au r�partiteur. Le calculateur actif dans le r�partiteur doit exister. Le r�partiteur doit �tre cr��. V�rifier la concordance des ports.");
									e.printStackTrace();
								}

							}
						}).start();

						// System.out.println("2 + 3 = " + result);

					}
					// String result2 = "";
					// try {
					// Object[] params = new Object[] { new Integer(0), new
					// Integer(0) };
					// result2 = (String) client.execute("Calculateur.add",
					// params);
					// System.out.println(result2);
					//
					// } catch (XmlRpcException e) {
					// LOGGER.severe(
					// "Un probleme est survenu, le calculateur doit �tre cr��
					// et ajout� au r�partiteur. Le calculateur actif dans le
					// r�partiteur doit exister. Le r�partiteur doit �tre cr��.
					// V�rifier la concordance des ports.");
					// e.printStackTrace();
					// }
					Thread.sleep(10000);
				}
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
			nb_requete = arguments.get(1);
			machine = arguments.get(2);
			port = arguments.get(3);
		}
	}
}