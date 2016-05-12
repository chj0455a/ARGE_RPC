package m2dl.arge.xmlrpc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by Tales of symphonia on 01/05/2016.
 */
public class VMManagerTest {

    VMManager vMManager;

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
        this.vMManager = VMManager.getGestionnaireRessource();

        // THEN le gestionnaire a un nouveau calculateur et le port a augmenté
        assertThat(this.vMManager.getCalculateurs().size(), is(1));
        assertThat(this.vMManager.getNouveauPort(), is(2013));
        assertThat(this.vMManager.getDerniereTraceDeProcess(), is("Le Worker Node web a demarre ..."));
    }
}