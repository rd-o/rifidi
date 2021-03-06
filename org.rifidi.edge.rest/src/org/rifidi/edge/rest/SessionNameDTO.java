/**
 * 
 */
package org.rifidi.edge.rest;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Matthew Dean - matt@transcends.co
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "session")
public class SessionNameDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5143356679507192666L;

	@XmlElement(name = "ID")
	private String sessionId;
	
	@XmlElement(name = "status")
	private String sessionStatus;
	
	@XmlElementWrapper(required = true, name="executingcommands")
	@XmlElement(name = "executingcommand")
	private List<ExecutingCommandDTO> executingCommands;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(String sessionStatus) {
		this.sessionStatus = sessionStatus;
	}

	public List<ExecutingCommandDTO> getExecutingCommands() {
		return executingCommands;
	}

	public void setExecutingCommands(List<ExecutingCommandDTO> executingCommands) {
		this.executingCommands = executingCommands;
	}
	
}
