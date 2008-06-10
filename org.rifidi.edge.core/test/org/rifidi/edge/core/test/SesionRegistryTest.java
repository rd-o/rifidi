package org.rifidi.edge.core.test;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rifidi.edge.core.adapter.dummyadapter.DummyConnectionInfo;
import org.rifidi.edge.core.adapter.dummyadapter.DummyReaderAdapterFactory;
import org.rifidi.edge.core.readerAdapterService.ReaderAdapterRegistryService;
import org.rifidi.edge.core.session.IRemoteSessionRegistryService;
import org.rifidi.edge.core.session.ISession;
import org.rifidi.edge.core.session.SessionRegistryService;
import org.rifidi.edge.core.session.SessionRegistryServiceImpl;
import org.rifidi.services.annotations.Inject;
import org.rifidi.services.registry.ServiceRegistry;


public class SesionRegistryTest {

	
	
	private SessionRegistryService sessionRegistryService;
	
	private ReaderAdapterRegistryService readerAdapterRegistryService;
	
	@Before
	public void setUp() throws Exception {
		ServiceRegistry.getInstance().service(this);
		
	}

	
	/**
	 * Test to make sure SessionRegistry is available as OSGi Service
	 */
	@Test
	public void testGetSessionRegistry(){
		Assert.assertNotNull(sessionRegistryService);
		Assert.assertTrue(sessionRegistryService.sessionCount() == 0);
	}
	
	/**
	 * Test to make sure ReaderAdapterRegistry is available as OSGi Service
	 */
	@Test
	public void testGetReaderAdapterRegistryService(){
		Assert.assertNotNull(readerAdapterRegistryService);
	}
	
	/**
	 * Test to make sure we can create a new Session
	 */
	@Test
	public void testCreateSession(){
		readerAdapterRegistryService.registerReaderAdapter(
				DummyConnectionInfo.class, new DummyReaderAdapterFactory());
		
		IRemoteSessionRegistryService sessionRegistryService2 = new SessionRegistryServiceImpl();
		
		ISession s = sessionRegistryService2.createReaderSession(new DummyConnectionInfo());
		
		Assert.assertNotNull(s);
	}
	
	@Test
	public void testSetAndGetSession(){
		IRemoteSessionRegistryService sessionRegistryService2 = new SessionRegistryServiceImpl();
		
		ISession s = sessionRegistryService2.createReaderSession(new DummyConnectionInfo());
		
		//Assert.assertTrue(sessionRegistryService2.getReaderSession(id) == sessionRegistryService2);
		//Assert.assertTrue(sessionRegistryService2.sessionCount() == 1);
	}
	
	@Inject
	public void setSessionRegistryService(SessionRegistryService regSer){
		this.sessionRegistryService = regSer;
	}
	
	@Inject
	public void setAdapterRegistryService(ReaderAdapterRegistryService readerAdapterRegistryService){
		this.readerAdapterRegistryService = readerAdapterRegistryService;
	}

}
