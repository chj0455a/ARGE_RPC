package m2dl.arge.xmlrpc.exception;

import java.io.Serializable;

/**
 * Created by danton on 15/05/16.
 */
public class NotEnoughtResourceException extends Throwable implements Serializable {
    public NotEnoughtResourceException(String s) {
        super(s);
    }
}
