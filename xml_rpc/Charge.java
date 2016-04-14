
public class Charge {
	private int chargeCourante;
	
	private int chargeMax;

	public Charge(int chargeCourante, int chargeMax) {
		super();
		this.chargeCourante = chargeCourante;
		this.chargeMax = chargeMax;
	}

	public int getChargeCourante() {
		return chargeCourante;
	}

	public void setChargeCourante(int chargeCourante) {
		this.chargeCourante = chargeCourante;
	}

	public int getChargeMax() {
		return chargeMax;
	}

	public void setChargeMax(int chargeMax) {
		this.chargeMax = chargeMax;
	}
	
	
}
