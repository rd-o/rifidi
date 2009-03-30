package org.rifidi.edge.core.api.jms.notifications;

import java.io.Serializable;

public class ReaderFactoryRemovedNotification implements Serializable {

	/** SerialVersionID */
	private static final long serialVersionUID = 1L;
	/** ReaderFactoryID */
	private String readerFactoryID;

	/**
	 * @param readerFactoryID
	 */
	public ReaderFactoryRemovedNotification(String readerFactoryID) {
		this.readerFactoryID = readerFactoryID;
	}

	/**
	 * @return the readerFactoryID
	 */
	public String getReaderFactoryID() {
		return readerFactoryID;
	}

}