package m2dl.arge.xmlrpc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by danton on 15/05/16.
 */
public class RepartiteurTest {
    Repartiteur repartiteur;

    @Before
    public void setUp() throws FileNotFoundException, UnsupportedEncodingException {
        this.repartiteur = new Repartiteur();
        this.repartiteur.setCalculateursLoadBalancing(initCalc());
        this.repartiteur.setWriter(new PrintWriter(new PrintWriter("logRepartiteur.txt", "UTF-8"), true));
    }

    private List<InfoCalculateur> initCalc() {
        List<InfoCalculateur> calcList = new ArrayList<>();
        calcList.add(new InfoCalculateur(null, 81.00, 100, 2012, "toto1", null, CalcState.OK));
        calcList.add(new InfoCalculateur(null, 0, 100, 2013, "toto2", null, CalcState.WILL_BE_DELETED));
        calcList.add(new InfoCalculateur(null, 0, 100, 2014, "toto3", null, CalcState.OK));
        calcList.add(new InfoCalculateur(null, 81.00, 100, 2015, "toto4", null, CalcState.WILL_BE_DELETED));
        return calcList;
    }

    @After
    public void tearDown() {

    }

    @Test
    public void creerCalculateurTest() throws Exception {
        this.repartiteur.setCalcIndexLoadBalance(3);
        this.repartiteur.creerCalculateur("toto5", 2016);

        assertThat(this.repartiteur.getCalculateursLoadBalancing().size(), is(5));
        assertThat(this.repartiteur.getCalculateursLoadBalancing().get(4).getAdresse(), is("toto5"));
        assertThat(this.repartiteur.getCalculateursLoadBalancing().get(4).getPort(), is(2016));

    }

    @Test(expected = NotEnoughtResourceException.class)
    public void choisirCalculateurTest() throws Exception, NotEnoughtResourceException {
            InfoCalculateur choosenCalc = this.repartiteur.choisirCalculateur();
            assertThat(choosenCalc.getAdresse(), is("toto3"));
            assertThat(choosenCalc.getPort(), is(2014));

            this.repartiteur.getCalculateursLoadBalancing().get(2).setCharge_courante(81.00);
            choosenCalc = this.repartiteur.choisirCalculateur();
    }

    @Test
    public void supprimerCalculateurTest() throws Exception {
        this.repartiteur.supprimerCalculateur("toto1", 2012);

        assertThat(this.repartiteur.getCalculateursLoadBalancing().size(), is(3));
        assertThat(this.repartiteur.getCalculateursLoadBalancing().get(0).getAdresse(), is("toto2"));
        assertThat(this.repartiteur.getCalculateursLoadBalancing().get(0).getPort(), is(2013));
    }

    @Test
    public void linkRequestTest() throws MalformedURLException {
        this.repartiteur.linkRequest("add", "toto10", "2011");
    }
}
