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
	System.out.println(os.images().list());

		// Find all Users
		List<? extends User> users = os.identity().users().list();
System.out.println(users.toString());
// List all Tenants
		List<? extends Tenant> tenants = os.identity().tenants().list();
        System.out.println(tenants.toString());
// Find all Compute Flavors
		List<? extends Flavor> flavors = os.compute().flavors().list();
        System.out.println(flavors.toString());
// Find all running Servers
		List<? extends Server> servers = os.compute().servers().list();
        System.out.println(servers.toString());
// Suspend a Server
//		os.compute().servers().action("serverId", Action.SUSPEND);

// List all Networks
		List<? extends Network> networks = os.networking().network().list();
        System.out.println(networks.toString());
// List all Subnets
		List<? extends Subnet> subnets = os.networking().subnet().list();
        System.out.println(subnets.toString());
// List all Routers
		List<? extends Router> routers = os.networking().router().list();
        System.out.println(routers.toString());
// List all Images (Glance)
		List<? extends Image> images = os.images().list();
        System.out.println(images.toString());
// Download the Image Data
		InputStream is = os.images().getAsStream("imageId");
    }
}
