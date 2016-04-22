
public class GestionnaireRessource {

	private static GestionnaireRessource instance;
	
	private GestionnaireRessource ()
	{
		
	}
	
	public static GestionnaireRessource getGestionnaireRessource() 
	{
		if(instance == null)
		{
			instance = new GestionnaireRessource();
		}
		return instance;
	}
}
