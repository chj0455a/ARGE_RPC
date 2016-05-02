package m2dl.arge.xmlrpc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by Tales of symphonia on 01/05/2016.
 */
public class GestionnaireRessourceTest {

    GestionnaireRessource gestionnaireRessource;

    @Before
    public void setUpt() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void creerCalculateur() throws Exception {
        // GIVEN

        // WHEN un gestionnaire est créé et créé donc un pr'emier calculateur
        this.gestionnaireRessource = GestionnaireRessource.getGestionnaireRessource();

        // THEN le gestionnaire a un nouveau calculateur et le port a augmenté
        assertThat(this.gestionnaireRessource.getCalculateurs().size(), is(1));
        assertThat(this.gestionnaireRessource.getNouveauPort(), is(2013));
        assertThat(this.gestionnaireRessource.getDerniereTraceDeProcess(), is("Le Worker Node web a demarre ..."));
    }
}