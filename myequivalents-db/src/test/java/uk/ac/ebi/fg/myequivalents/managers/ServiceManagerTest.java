package uk.ac.ebi.fg.myequivalents.managers;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.StringReader;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBException;

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * Tests for {@link DbServiceManager}.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceManagerTest
{
	private ServiceManager serviceMgr;

	private Service service1, service2, service3, service4, service5;
	private ServiceCollection sc1;
	private Repository repo1;
	
	// An editor is needed for writing operations.
	public static final String editorPass = "test.password";
	public static final String editorSecret = User.generateSecret ();
	public static User editorUser = new User ( 
		"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret );

	
	@Before
	public void init ()
	{
		// Something around is changing this
		editorUser = new User ( 
			"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret 
		);
		
		// An editor is needed for writing operations.
		EntityManager em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( editorUser );
		ts.commit ();

		// This is how you should obtain a manager from a factory
		serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( editorUser.getEmail (), editorSecret );
		
		service1 = new Service ( "test.testservmgr.service1", "testservmgr.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPattern ( "http://somewhere.in.the.net/testservmgr/service1/someType1/$id" );
				
		sc1 = new ServiceCollection ( 
			"test.testservmgr.serviceColl1", service1.getEntityType (), "Test Service Collection 1", "The Description of the SC 1" 
		);
		service1.setServiceCollection ( sc1 );
		
		repo1 = new Repository ( "test.testservmgr.repo1", "Test Repo 1", "The Description of Repo1" );
		service1.setRepository ( repo1 );

		service2 = new Service ( "test.testservmgr.service2", "testservmgr.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		// Should pop-up on the XML
		service2.setReleaseDate ( new GregorianCalendar ( 2010, GregorianCalendar.APRIL, 25, 18, 13 ).getTime () );


		service3 = new Service ( "test.testservmgr.service3", "testservmgr.someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service4 = new Service ( "test.testservmgr.service4", "testservmgr.someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service5 = new Service ( "test.testservmgr.service5", "testservmgr.someType2", "A Test Service 5", "The Description of a Test Service 5" );

		String servNames[] = new String[] { 
			service1.getName (), service2.getName (), service3.getName (), service4.getName (), service5.getName (),
			"test.testservmgr.service6", "test.testservmgr.service7", "test.testservmgr.service8"
		};
		serviceMgr.deleteServices ( servNames );
		serviceMgr.deleteServiceCollections ( sc1.getName () );
		serviceMgr.deleteRepositories ( repo1.getName () );
		
		assertTrue ( "Services not deleted!", serviceMgr.getServices ( servNames ).getServices ().isEmpty () );
		assertTrue ( "Repository not deleted!", serviceMgr.getRepositories ( repo1.getName () ).getRepositories ().isEmpty () );
		assertTrue ( "Service-Collection not deleted!", serviceMgr.getServiceCollections ( sc1.getName () ).getServiceCollections ().isEmpty () );
		
		// TODO: more checks 
		
		serviceMgr.storeServices ( service1, service2, service3, service4, service5 );
		
		assertEquals ( "Services not created!", 5, serviceMgr.getServices ( servNames ).getServices ().size () );
		assertEquals ( "Repository not created!", 1, serviceMgr.getRepositories ( repo1.getName () ).getRepositories ().size () );
		assertEquals ( "Service-Collection not created!", 1, serviceMgr.getServiceCollections ( sc1.getName () ).getServiceCollections ().size () );
	}

	@After
	public void cleanUp () 
	{
		// An editor is needed for writing operations.
		EntityManager em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.deleteUnauthorized ( editorUser.getEmail () );
		ts.commit ();
	}
	

	@Test
	public void testSearch ()
	{
		// Work as anonymous
		serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ();
			
		ServiceSearchResult result = serviceMgr.getServices ( 
			service4.getName (), service2.getName (), service5.getName (), "test.servMgr.foo" 
		);
		out.println ( "Search Result:\n" + result.toString () );
		
		assertEquals ( "Search returns a wrong no. of results!", 3, result.getServices ().size () );
		// TODO: More checks
		
		String xml = serviceMgr.getServicesAs ( "xml", service4.getName (), service2.getName () );
		out.println ( "Search Result:\n" + xml );
		
		// TODO: checks over the XML
	}
	
	
	@Test
	public void testAddFromXML () throws JAXBException
	{
		// TODO: Test Service Collection too
		
		String xml =
		"<service-items>\n" +
		"  <services>\n" +
    "    <service uri-pattern='http://somewhere.in.the.net/testservmgr/service6/someType1/$id'\n" + 
    "           entity-type='testservmgr.someType1' title='A Test Service 6' name='test.testservmgr.service6'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testservmgr.someType7' title='A Test Service 7' name='test.testservmgr.service7'" +
    "           repository-name = 'test.testservmgr.repo1'" +
    "           service-collection-name = 'test.testservmgr.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service\n" +
    "             entity-type='testservmgr.someType2' title='A Test Service 8' name='test.testservmgr.service8'>\n" + 
    "      <description>The Description of a Test Service 8</description>\n" + 
    "    </service>\n" +
    "  </services>\n" +
    "</service-items>";

		serviceMgr.storeServicesFromXML ( new StringReader ( xml ) );
		
		ServiceSearchResult result = serviceMgr.getServices ( 
			"test.testservmgr.service6", "test.testservmgr.service7", "test.testservmgr.service8" );
		
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of services stored", 3, result.getServices ().size () );
		
		xml = serviceMgr.getServicesAs ( "xml", 
			"test.testservmgr.service6", "test.testservmgr.service7", "test.testservmgr.service8" );
		out.println ( "Search Result (XML):\n" + xml );
		// TODO: checks on the XML
	}
	
	
	@Test
	public void testComplexAddFromXML () throws JAXBException
	{
		// TODO: Test Service Collection too
		
		String xml =
		"<service-items>\n" +
		"  <services>\n" +
    "    <service uri-pattern='http://somewhere.in.the.net/testservmgr/service6/someType1/$id'\n" + 
    "           entity-type='testservmgr.someType1' title='A Test Service 6' name='test.testservmgr.service6'\n" +
    "						release-date = '20130110' public-flag = 'null'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testservmgr.someType7' title='A Test Service 7' name='test.testservmgr.service7'" +
    "           repository-name = 'test.testservmgr.repo1'" +
    "           service-collection-name = 'test.testservmgr.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service\n" +
    "             entity-type='testservmgr.someType2' title='A Test Service 8' name='test.testservmgr.service8'" +
    "             repository-name = 'test.testservmgr.addedRepo1'>\n" + 
    "      <description>The Description of a Test Service 8</description>\n" + 
    "    </service>\n" +
    "  </services>\n" +
    "  <repositories>" +
    "  		<repository name = 'test.testservmgr.addedRepo1' public-flag = 'false'>\n" +
    "       <description>A test Added Repo 1</description>\n" +
    "     </repository>\n" +
    "  </repositories>\n" +
    "  <service-collections>" +
    "  		<service-collection name = 'test.testservmgr.added-sc-1' title = 'Added Test SC 1'>\n" +
    "       <description>A test Added SC 1</description>\n" +
    "     </service-collection>\n" +
    "  </service-collections>\n" +
    "</service-items>";

		serviceMgr.storeServicesFromXML ( new StringReader ( xml ) );
		
		ServiceSearchResult result = serviceMgr.getServices ( 
			"test.testservmgr.service6", "test.testservmgr.service7", "test.testservmgr.service8" );
		
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of services stored!", 3, result.getServices ().size () );
		
		xml = serviceMgr.getServicesAs ( "xml", 
			"test.testservmgr.service6", "test.testservmgr.service7", "test.testservmgr.service8" );
		out.println ( "Search Result (XML):\n" + xml );
		// TODO: checks on the XML
		
		result = serviceMgr.getServiceCollections ( "test.testservmgr.added-sc-1" );
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of SC stored!", 1, result.getServiceCollections ().size () );

		xml = serviceMgr.getServiceCollectionsAs ( "xml", "test.testservmgr.added-sc-1" );
		out.println ( "Search Result (XML):\n" + xml );
		// TODO: checks on the XML
		
		Service srv6 = serviceMgr.getServices ( "test.testservmgr.service6" ).getServices ().iterator ().next ();
		assertTrue ( "release date defined in the XML not stored!", 
			new DateMidnight ( 2013, 01, 10 ).isEqual ( srv6.getReleaseDate ().getTime () )
		);
		assertNull ( "public flag defined in the XML not stored!", srv6.getPublicFlag () );
		
		Repository repo1 = serviceMgr.getRepositories ( "test.testservmgr.addedRepo1" ).getRepositories ().iterator ().next ();
		assertFalse ( "public flag defined in the XML not stored (repo1)!", repo1.getPublicFlag () );
		
	}
	
}
