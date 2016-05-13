package m2dl.arge.xmlrpc;



import org.apache.xmlrpc.client.XmlRpcClient;

public class InfoCalculateur {

	private String adresse;
	private XmlRpcClient client;
	private int charge_courante;
	private int charge_max;
	private int port;
    private CalcState state;
    private String id;

    public InfoCalculateur(XmlRpcClient client, int charge_courante, int charge_max, int port, String adresse, String id) {
		super();
		this.client = client;
		this.charge_courante = charge_courante;
		this.charge_max = charge_max;
		this.port = port;
		this.adresse = adresse;
        this.state = CalcState.OK;
        this.id = id;
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public CalcState getState() {
        return state;
    }

    public void setState(CalcState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
