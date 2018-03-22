package eu.driver.adaptor.callback;

import org.apache.avro.generic.IndexedRecord;
import org.apache.log4j.Logger;

import eu.driver.adaptor.ws.CallbackController;
import eu.driver.api.IAdaptorCallback;

public class AdapterCallback implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void messageReceived(IndexedRecord receivedMessage) {
		log.info("-->messageReceived");
		
		CallbackController.getInstance().sendMessage(receivedMessage.toString());
		
		log.info("messageReceived-->");
	}

}
