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
package org.rifidi.edge.adapter.thinkifyusb;

import gnu.io.CommPortIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.api.SessionDTO;
import org.rifidi.edge.configuration.AnnotationMBeanInfoStrategy;
import org.rifidi.edge.configuration.JMXMBean;
import org.rifidi.edge.configuration.Property;
import org.rifidi.edge.configuration.PropertyType;
import org.rifidi.edge.exceptions.CannotCreateSessionException;
import org.rifidi.edge.sensors.AbstractCommandConfiguration;
import org.rifidi.edge.sensors.AbstractSensor;
import org.rifidi.edge.sensors.CannotDestroySensorException;
import org.rifidi.edge.sensors.SensorSession;

import com.thinkify.rfid.SerialManager;

/**
 * 
 * 
 * @author Matthew Dean - matt@transcends.co
 */
@JMXMBean
public class ThinkifyUSBSensor extends AbstractSensor<ThinkifyUSBSensorSession> {

	/** Logger for this class. */
	private static final Log logger = LogFactory.getLog(ThinkifyUSBSensor.class);

	/** A hashmap containing all the properties for this reader */
	@SuppressWarnings("unused")
	private final ConcurrentHashMap<String, String> readerProperties;
	/** The port the reader is connected to */
	private volatile String port = ThinkifyUSBConstants.PORT;
	/** The read rate for the reader */
	private volatile int readrate = 1000;
	/** Flag to check if this reader is destroyed. */
	private AtomicBoolean destroyed = new AtomicBoolean(false);
	/** The reader attenuation */
	private Integer ra = 7;
	private Integer ag = 0;
	private Integer q = 4;
	private Integer fl = 7;
	private Integer fh = 7;
	private Integer p = 131;
	private String generic = "";
	
	/** The reader mask */
	private String ma = "";
	/** Time between two connection attempts. */
	private volatile Integer reconnectionInterval = 2500;
	/** Number of connection attempts before a connection goes into fail state. */
	private volatile Integer maxNumConnectionAttempts = 10;
	/** The only session an LLRP reader allows. */
	private AtomicReference<ThinkifyUSBSensorSession> session = new AtomicReference<ThinkifyUSBSensorSession>();
	/** Provided by spring. */
	private final Set<AbstractCommandConfiguration<?>> commands;
	/** The ID of the session */
	private AtomicInteger sessionIDcounter = new AtomicInteger(0);
	
	private Boolean disableAutoStart=false;

	public static final MBeanInfo mbeaninfo;
	private String displayName;

	static {
		AnnotationMBeanInfoStrategy strategy = new AnnotationMBeanInfoStrategy();
		mbeaninfo = strategy.getMBeanInfo(ThinkifyUSBSensor.class);
	}

	public ThinkifyUSBSensor(Set<AbstractCommandConfiguration<?>> commands) {
		this.commands = commands;
		this.readerProperties = new ConcurrentHashMap<String, String>();

		HashSet<CommPortIdentifier> set = SerialManager.getAvailableSerialPorts();

		for (CommPortIdentifier com : set) {
			logger.info("Port: " + com.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.AbstractSensor#createSensorSession()
	 */
	@Override
	public String createSensorSession() throws CannotCreateSessionException {
		if (!destroyed.get() && session.get() == null) {
			Integer sessionID = this.sessionIDcounter.incrementAndGet();
			if (session.compareAndSet(null, new ThinkifyUSBSensorSession(this,
					sessionID.toString(), notifierService, super.getID(), port,
					reconnectionInterval, maxNumConnectionAttempts, commands,
					readrate, ra, ma, ag, q, p, fl, fh, generic))) {

				// TODO: remove this once we get AspectJ in here!
				notifierService.addSessionEvent(this.getID(),
						Integer.toString(sessionID));
				return sessionID.toString();
			}
		}
		throw new CannotCreateSessionException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.AbstractSensor#createSensorSession(org.rifidi
	 * .edge.api.SessionDTO)
	 */
	@Override
	public String createSensorSession(SessionDTO sessionDTO)
			throws CannotCreateSessionException {
		if (!destroyed.get() && session.get() == null) {
			Integer sessionID = Integer.parseInt(sessionDTO.getID());
			if (session.compareAndSet(null, new ThinkifyUSBSensorSession(this,
					sessionID.toString(), notifierService, super.getID(), port,
					reconnectionInterval, maxNumConnectionAttempts, commands,
					readrate, ra, ma, ag, q, p, fl, fh, generic))) {
				session.get().restoreCommands(sessionDTO);
				// TODO: remove this once we get AspectJ in here!
				notifierService.addSessionEvent(this.getID(),
						Integer.toString(sessionID));
				return sessionID.toString();
			}

		}
		throw new CannotCreateSessionException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.AbstractSensor#getSensorSessions()
	 */
	@Override
	public Map<String, SensorSession> getSensorSessions() {
		Map<String, SensorSession> ret = new HashMap<String, SensorSession>();
		ThinkifyUSBSensorSession thinksession = session.get();
		if (thinksession != null) {
			ret.put(thinksession.getID(), thinksession);
		}
		return ret;
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	@Property(displayName = "Port", description = "Port of the Reader", writable = true, type = PropertyType.PT_STRING, category = "connection"
			+ "", orderValue = 1, defaultValue = ThinkifyUSBConstants.PORT)
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	@Property(displayName = "DisableAutoStart", description = "Set to true to disable autostart", writable = true, type = PropertyType.PT_BOOLEAN, 
			category = "connection", orderValue = 8, defaultValue = "false")
	public Boolean getDisableAutoStart() {
		return disableAutoStart;
	}
	public void setDisableAutoStart(Boolean disableAutoStart) {
		this.disableAutoStart = disableAutoStart;
	}

	@Property(displayName = "ReadRate", description = "The rate that the reader will read at in milliseconds", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 2, defaultValue = ThinkifyUSBConstants.READRATE)
	public int getReadRate() {
		return readrate;
	}

	public void setReadRate(Integer readrate) {
		this.readrate = readrate;
	}

	@Property(displayName = "ag", description = "Sets the reader attenuation", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 1, defaultValue = ThinkifyUSBConstants.AG, minValue = "-6", maxValue = "19")
	public int getag() {return ag;}
	public void setag(Integer ag) {this.ag = ag;}
	
	@Property(displayName = "ra", description = "Sets the reader attenuation", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 2, defaultValue = ThinkifyUSBConstants.RA, minValue = "0", maxValue = "19")
	public int getra() {return ra;}
	public void setra(Integer ra) {this.ra = ra;}

	@Property(displayName = "ma", description = "Sets the mask for the reader", writable = true, type = PropertyType.PT_STRING, category = "reading"
			+ "", orderValue = 3, defaultValue = ThinkifyUSBConstants.MA)
	public String getma() {return ma;}
	public void setma(String ma) {this.ma = ma;}
	
	@Property(displayName = "p", description = "Sets the p value for the reader", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 4, defaultValue = ThinkifyUSBConstants.P)
	public Integer getp() {return p;}
	public void setp(Integer p) {this.p = p;}
	
	@Property(displayName = "q", description = "Sets the q value for the reader.  The number of expected tags on this reader should be 2^q.", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 5, defaultValue = ThinkifyUSBConstants.Q)
	public Integer getq() {return q;}
	public void setq(Integer q) {this.q = q;}
	
	@Property(displayName = "fl", description = "Sets the fl value for the reader.", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 6, defaultValue = ThinkifyUSBConstants.FL)
	public Integer getfl() {return fl;}
	public void setfl(Integer fl) {this.fl = fl;}
	
	@Property(displayName = "fr", description = "Sets the fr value for the reader.", writable = true, type = PropertyType.PT_INTEGER, category = "reading"
			+ "", orderValue = 7, defaultValue = ThinkifyUSBConstants.FH)
	public Integer getfh() {return fh;}
	public void setfh(Integer fh) {this.fh = fh;}
	
	@Property(displayName = "generic", description = "Allows you pass in any number of valid Thinkify commands, separated by pipes.  Example:"
			+ "'am3|dt2' will set 'am' to 3 and 'dt' to 2.", writable = true, type = PropertyType.PT_STRING, category = "reading"
			+ "", orderValue = 8, defaultValue = "")
	public String getgeneric() {return generic;}
	public void setgeneric(String generic) {this.generic = generic;}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.AbstractSensor#destroySensorSession(java.lang
	 * .String)
	 */
	@Override
	public void destroySensorSession(String id)
			throws CannotDestroySensorException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.AbstractSensor#applyPropertyChanges()
	 */
	@Override
	public void applyPropertyChanges() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.sensors.base.AbstractSensor#getDisplayName()
	 */
	@Override
	@Property(displayName = "Display Name", description = "Logical Name of Reader", writable = true, type = PropertyType.PT_STRING, category = "connection", defaultValue = "ThinkifyUSB", orderValue = 0)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	/**
	 * Gets the reconnect interval.
	 * 
	 * @return the reconnectionInterval
	 */
	@Property(displayName = "Reconnection Interval", description = "Upon connection failure, the "
			+ "time to wait between two connection attempts (ms)", writable = true, type = PropertyType.PT_INTEGER, category = "connection"
			+ "", defaultValue = ThinkifyUSBConstants.RECONNECTION_INTERVAL, orderValue = 4, minValue = "0")
	public Integer getReconnectionInterval() {
		return reconnectionInterval;
	}

	/**
	 * Sets the reconnect interval.
	 * 
	 * @param reconnectionInterval
	 *            the reconnectionInterval to set
	 */
	public void setReconnectionInterval(Integer reconnectionInterval) {
		this.reconnectionInterval = reconnectionInterval;
	}

	/**
	 * Gets the number of connection attempts to try before giving up.
	 * 
	 * @return the maxNumConnectionAttempts
	 */
	@Property(displayName = "Maximum Connection Attempts" + "", description = "Upon connection failure, the number of times to attempt to "
			+ "recconnect before giving up. If set to '-1', then try forever", writable = true, type = PropertyType.PT_INTEGER, category = "connection"
			+ "", defaultValue = ThinkifyUSBConstants.MAX_CONNECTION_ATTEMPTS, orderValue = 5, minValue = "-1")
	public Integer getMaxNumConnectionAttempts() {
		return maxNumConnectionAttempts;
	}

	/**
	 * Sets the number of connection attempts to try before giving up.
	 * 
	 * @param maxNumConnectionAttempts
	 *            the maxNumConnectionAttempts to set
	 */
	public void setMaxNumConnectionAttempts(Integer maxNumConnectionAttempts) {
		this.maxNumConnectionAttempts = maxNumConnectionAttempts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.AbstractSensor#unbindCommandConfiguration(org
	 * .rifidi.edge.sensors.AbstractCommandConfiguration, java.util.Map)
	 */
	@Override
	public void unbindCommandConfiguration(
			AbstractCommandConfiguration<?> commandConfiguration,
			Map<?, ?> properties) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.configuration.RifidiService#getMBeanInfo()
	 */
	@Override
	public MBeanInfo getMBeanInfo() {
		return (MBeanInfo) mbeaninfo.clone();
	}

}
