package m2dl.arge.xmlrpc;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.networking.domain.NeutronNetwork;
import org.openstack4j.openstack.networking.domain.NeutronSubnet;

import java.io.InputStream;
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
    }
}
