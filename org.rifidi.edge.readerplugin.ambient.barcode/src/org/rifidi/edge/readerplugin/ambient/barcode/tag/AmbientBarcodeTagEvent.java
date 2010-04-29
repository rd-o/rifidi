/*
 *  AmbientBarcodeTagEvent.java
 *
 *  Created:	Apr 23, 2010
 *  Project:	Rifidi Edge Server - A middleware platform for RFID applications
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	GNU Public License (GPL)
 *  				http://www.opensource.org/licenses/gpl-3.0.html
 */
package org.rifidi.edge.readerplugin.ambient.barcode.tag;

import java.math.BigInteger;

import org.rifidi.edge.core.services.notification.data.DatacontainerEvent;

/**
 * This class represents a barcode tag for the Ambient reader.
 * 
 * @author Matthew Dean - matt@pramari.com
 */
public class AmbientBarcodeTagEvent extends DatacontainerEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5334113422705563853L;

	/** Store a hex copy of the epc for comparison. */
	protected String barcode = "";

	/**
	 * Constructor.
	 */
	public AmbientBarcodeTagEvent() {
		memoryBanks.add(new MemoryBankLengthTuple(new BigInteger("0"), 1));
	}

	/**
	 * Set the barcode
	 * 
	 * @param memBank
	 * @param length
	 */
	public void setBarcode(BigInteger memBank, Integer length) {
		memoryBanks.get(0).setLength(length);
		memoryBanks.get(0).setMemory(memBank);
		barcode = memBank.toString();
	}

	/**
	 * Returns the barcode in decimal encoding.
	 * 
	 * @return
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * Get the epc memory bank.
	 * 
	 * @return
	 */
	public BigInteger getBarcodeMemory() {
		return memoryBanks.get(0).getMemory();
	}

	/**
	 * Returns the length of the memory bank.
	 * 
	 * @return the number of bits in the EPC
	 */
	public Integer getBarcodeMemoryLength() {
		return memoryBanks.get(0).getLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AmbientBarcodeTagEvent) {
			return barcode.hashCode() == obj.hashCode();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return barcode.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.services.notification.data.DatacontainerEvent#getID
	 * ()
	 */
	@Override
	public BigInteger getID() {
		return this.memoryBanks.get(0).getMemory();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AmbientBarcodeTagEvent [hex=" + barcode + "]";
	}

}