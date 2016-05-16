package m2dl.arge.xmlrpc;

import java.io.Serializable;

/**
 * Created by danton on 12/05/16.
 */
public class MissingImageException extends Exception implements Serializable{
    public MissingImageException(String s) {
        super(s);
    }
}
