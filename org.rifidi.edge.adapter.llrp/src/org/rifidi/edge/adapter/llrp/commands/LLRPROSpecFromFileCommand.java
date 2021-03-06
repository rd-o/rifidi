/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.edge.adapter.llrp.commands;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.CUSTOM_MESSAGE;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.GET_ROSPECS;
import org.llrp.ltk.generated.messages.GET_ROSPECS_RESPONSE;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.types.BytesToEnd_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.SignedByte;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.rifidi.edge.adapter.llrp.AbstractLLRPCommand;
import org.rifidi.edge.adapter.llrp.LLRPReaderSession;

/**
 * This command takes an ADD_ROSPEC command loaded from a file and submits it.
 * It will delete all ROSpecs on the reader itself, add the loaded ROSpec, and
 * enable the ROSpec it just added.
 * 
 * @author Matthew Dean - matt@pramari.com
 */
public class LLRPROSpecFromFileCommand extends AbstractLLRPCommand {

	private static final Log logger = LogFactory
			.getLog(LLRPROSpecFromFileCommand.class);

	/**
	 * The session.
	 */
	private LLRPReaderSession session = null;

	private boolean impinjExtensions;

	/**
	 * The ADD_ROSPEC command that will be loaded from a file and submitted.
	 */
	private LLRPMessage llrpcommand = null;

	/**
	 * @param commandID
	 */
	public LLRPROSpecFromFileCommand(String commandID) {
		super(commandID);
	}

	/**
	 * Sets the ROSpecCommand
	 * 
	 * @param llrpcommand
	 */
	public void setLLRPMessage(LLRPMessage llrpcommand) {
		this.llrpcommand = llrpcommand;

	}

	public void setImpinjExtensions(boolean impinjExtensions) {
		this.impinjExtensions = impinjExtensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.commands.TimeoutCommand#execute()
	 */
	@Override
	protected void execute() throws TimeoutException {
		this.session = (LLRPReaderSession) this.sensorSession;
		if (llrpcommand instanceof ADD_ROSPEC) {
			ADD_ROSPEC addRospec = (ADD_ROSPEC)llrpcommand;
			if (addRospec == null) {
				logger.error("Can't add ROSpec: the ROSpec was not initialized "
						+ "correctly.  Check the XML file.  ");
				return;
			}
			// Find and delete all ROSpecs on the reader.
			GET_ROSPECS rospecs = new GET_ROSPECS();
			GET_ROSPECS_RESPONSE response = null;
			response = (GET_ROSPECS_RESPONSE) session.transact(rospecs);
			List<ROSpec> rospecList = response.getROSpecList();
			for (ROSpec rspec : rospecList) {
				DELETE_ROSPEC delROSpec = new DELETE_ROSPEC();
				delROSpec.setROSpecID(new UnsignedInteger(rspec.getROSpecID()
						.intValue()));

				// TODO: check the response?
				session.transact(delROSpec);

			}

			if (impinjExtensions) {
				BytesToEnd_HEX data = new BytesToEnd_HEX();
				CUSTOM_MESSAGE msg = new CUSTOM_MESSAGE();
				msg.setVendorIdentifier(new UnsignedInteger(25882));
				msg.setMessageSubtype(new UnsignedByte(21));
				data.add(new SignedByte(0));
				data.add(new SignedByte(0));
				data.add(new SignedByte(0));
				data.add(new SignedByte(0));
				msg.setData(data);
				session.send(msg);
			}

			// Send the ADD_ROSPEC command
			// TODO: check the response?
			session.transact(addRospec);

			// Enable the ROSpec
			ENABLE_ROSPEC enablerospec = new ENABLE_ROSPEC();
			enablerospec.setROSpecID(addRospec.getROSpec().getROSpecID());
			// TODO: check the response?
			session.transact(enablerospec);
		} else {
			session.transact(llrpcommand);
		}
	}

}
