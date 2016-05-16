package m2dl.arge.xmlrpc.exception;


import java.io.Serializable;

public class CalculatorsManagementException extends Exception implements Serializable {

	public CalculatorsManagementException(String message) {
		super(message);
	}
}
