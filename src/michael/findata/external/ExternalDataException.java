package michael.findata.external;

import com.sun.xml.internal.ws.api.model.CheckedException;

public class ExternalDataException extends Exception {
	public ExternalDataException(String msg) {
		super (msg);
	}
}
