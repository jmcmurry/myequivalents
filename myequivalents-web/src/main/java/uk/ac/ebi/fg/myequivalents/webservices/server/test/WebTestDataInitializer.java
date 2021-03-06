/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.server.test;

import java.io.StringReader;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;

/**
 * This defines some test data if the system property uk.ac.ebi.fg.myequivalents.test_flag is true. It is attached to 
 * the servlet engine via web.xml.
 *
 * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class WebTestDataInitializer implements ServletContextListener
{
	/**
	 * When this is true, test data are loaded into the DB back end, during server initialisation, via 
	 * {@link #contextInitialized(ServletContextEvent)}. These data are cleaned up upon server shutdown, 
	 * by {@link #contextDestroyed(ServletContextEvent)}.
	 * 
	 */
	public static final String INIT_FLAG_PROP = "uk.ac.ebi.fg.myequivalents.test_flag";
	
	public static final String editorPass = "test.password";
	// Alternatively you can use: User.generateSecret (); using something else here cause we need to test with the browser 
	public static final String editorSecret = "test.secret"; 
	public static final User editorUser = new User ( 
		"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret );

	public static final String adminPass = "test.password";
	public static final String adminSecret = "test.secret";
	public static final User adminUser = new User ( 
		"test.admin", "Test", "Admin", adminPass, "test notes", Role.ADMIN, adminSecret 
	);

	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * @see #INIT_FLAG_PROP.
	 */
	@Override
	public void contextInitialized ( ServletContextEvent e )
	{
		if ( !"true".equals ( System.getProperty ( INIT_FLAG_PROP, null ) ) ) return;
	
		System.out.println ( "\n\n _________________________________ Creating Test Data ________________________________ \n\n\n" );
		
		ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
		
		// An editor is needed for writing operations.
		//
		EntityManager em = ((DbManagerFactory) mgrf).getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( adminUser );
		userDao.storeUnauthorized ( editorUser );
		ts.commit ();
		em.close ();
		
		ServiceManager serviceMgr = mgrf.newServiceManager ( editorUser.getEmail (), editorSecret );
		
		ServiceCollection sc1 = new ServiceCollection ( 
			"test.testweb.serviceColl1", null, "Test Service Collection 1", "The Description of the SC 1" 
		);
		serviceMgr.storeServiceCollections ( sc1 );
		
		Repository repo1 = new Repository ( "test.testweb.repo1", "Test Repo 1", "The Description of Repo1" );
		serviceMgr.storeRepositories ( repo1 );

		String testServiceXml =
			"<service-items>\n" +
			"  <services>\n" +
	    "    <service uri-pattern='http://somewhere.in.the.net/testweb/service6/someType1/$id'\n" + 
	    "           entity-type='testweb.someType1' title='A Test Service 6' name='test.testweb.service6'>\n" +
	    "      <description>The Description of a Test Service 6</description>\n" + 
	    "    </service>\n" + 
	    "    <service entity-type='testweb.someType7' title='A Test Service 7' name='test.testweb.service7'" +
	    "           repository-name = 'test.testweb.repo1'" +
	    "           service-collection-name = 'test.testweb.serviceColl1'" +
	    "           uri-pattern = 'http://somewhere.in.the.net/testweb/service7/someType1/$id'>\n" +
	    "      <description>The Description of a Test Service 7</description>\n" +
	    "    </service>\n" +
	    "    <service\n" +
	    "             entity-type='testweb.someType2' title='A Test Service 8' name='test.testweb.service8'" +
	    "             repository-name = 'test.testweb.addedRepo1'" +
	    "             uri-pattern = 'http://somewhere.else.in.the.net/testweb/service8/someType1/$id'>\n" + 
	    "      <description>The Description of a Test Service 8</description>\n" + 
	    "    </service>\n" +
	    "  </services>\n" +
	    "  <repositories>" +
	    "  		<repository name = 'test.testweb.addedRepo1'>\n" +
	    "       <description>A test Added Repo 1</description>\n" +
	    "     </repository>\n" +
	    "  </repositories>\n" +
	    "  <service-collections>" +
	    "  		<service-collection name = 'test.testweb.added-sc-1' title = 'Added Test SC 1'>\n" +
	    "       <description>A test Added SC 1</description>\n" +
	    "     </service-collection>\n" +
	    "  </service-collections>\n" +
	    "</service-items>";		

		serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );
		
		EntityMappingManager emapMgr = mgrf.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		emapMgr.storeMappings (
			"test.testweb.service6:acc1", "test.testweb.service8:acc2", 
			"test.testweb.service6:acc3", "test.testweb.service6:acc4" 
		);
		emapMgr.storeMappingBundle ( 
			"test.testweb.service7:acc1", "test.testweb.service6:acc4", "test.testweb.service6:acc1"
		);
		
		emapMgr.storeMappings (
			"test.testweb.service7:acc10", "test.testweb.service8:acc10" 
		);
		
		emapMgr.close ();
		

		// More data, used to test the backup manager
		log.info ( "Generating data for testing the backup manager" );
		MappingsGenerator mgen = new MappingsGenerator ();
		mgen.generateMappings ();
		
	}	
	
	@Override
	public void contextDestroyed ( ServletContextEvent e )
	{
		if ( !"true".equals ( System.getProperty ( INIT_FLAG_PROP, null ) ) ) return;

		ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
		
		EntityMappingManager emapMgr = mgrf.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		emapMgr.deleteEntities ( "test.testweb.service6:acc3" );
		emapMgr.deleteMappings ( "test.testweb.service7:acc1" );
		emapMgr.close ();
		
		EntityManager em = ((DbManagerFactory) mgrf).getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.deleteUnauthorized ( editorUser.getEmail () );
		userDao.deleteUnauthorized ( adminUser.getEmail () );
		ts.commit ();
		
		// More data, used to test the backup manager
		log.info ( "Removing data used by the backup manager" );
		MappingsGenerator mgen = new MappingsGenerator ();
		mgen.cleanUp ();
	}


}
