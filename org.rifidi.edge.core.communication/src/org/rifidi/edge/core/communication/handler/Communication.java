/*
 *  Communication.java
 *
 *  Created:	Jun 27, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.edge.core.communication.handler;

import java.io.IOException;

import org.rifidi.edge.core.communication.buffer.ConnectionBuffer;

public interface Communication {

	public ConnectionBuffer startCommunication() throws IOException;

	public void stopCommunication() throws IOException;

}
