package m2dl.arge.xmlrpc;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Image;
import org.openstack4j.openstack.OSFactory;

import java.util.List;
import java.util.logging.Logger;


public class OsLi {
	private static Logger LOGGER = Logger.getLogger("OsLi");
    public static void main(String[] args) {
        LOGGER.info("Try to connect to cloudmip vm");
	OSClient os = OSFactory.builder()
	    .endpoint("http://195.220.53.61:5000/v2.0")
	    .credentials("ens27","BTAAMU").tenantName("service")
	    .authenticate();
        LOGGER.info("Connection succeed");
	System.out.println(os);
        List<? extends Image> imagesList = os.compute().images().list();
        for (Image image :
                imagesList) {
            LOGGER.info(image.getName() + " " + image.getName());
        }
    }
}
