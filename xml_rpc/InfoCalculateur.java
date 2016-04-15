import org.apache.xmlrpc.client.XmlRpcClient;

public class InfoCalculateur {

	private XmlRpcClient client;
	private int charge_courante;
	private int charge_max;
	private int port;
	
	public InfoCalculateur(XmlRpcClient client, int charge_courante, int charge_max, int port) {
		super();
		this.client = client;
		this.charge_courante = charge_courante;
		this.charge_max = charge_max;
		this.port = port;
	}

	public XmlRpcClient getClient() {
		return client;
	}

	public void setClient(XmlRpcClient client) {
		this.client = client;
	}

	public int getCharge_courante() {
		return charge_courante;
	}

	public void setCharge_courante(int charge_courante) {
		this.charge_courante = charge_courante;
	}

	public int getCharge_max() {
		return charge_max;
	}

	public void setCharge_max(int charge_max) {
		this.charge_max = charge_max;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
