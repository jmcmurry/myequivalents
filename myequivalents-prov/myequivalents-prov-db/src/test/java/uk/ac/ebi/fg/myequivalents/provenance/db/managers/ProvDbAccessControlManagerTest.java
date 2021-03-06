package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.adminUser;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.editorUser;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testPass;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testSecret;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * Tests the functionality of {@link AccessControlManager}
 *
 * <dl><dt>date</dt><dd>16 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbAccessControlManagerTest
{
	@Before
	public void init ()
	{
		ProvDbServiceManagerTest.init ();
	}
	
	@After
	public void cleanUp ()
	{
		ProvDbServiceManagerTest.cleanUp ();
	}
	
	@Test
	public void testUsers ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		AccessControlManager accMgr = mgrFact.newAccessControlManagerFullAuth ( adminUser.getEmail (), testPass );
		
		User user = new User ( 
			"test.new.user", "Test New", "User", "test.pwd", null, Role.VIEWER, "test.secret" 
		);
		accMgr.storeUser ( user );

		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( 
			adminUser.getEmail (), "accessControl.storeUser", Arrays.asList ( p ( "user", "test.new.user" ) )
		);

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved (test.new.user)!", 1, proves.size () );
	}
	
	/**
	 * Test visibility commands. 
	 */
	@Test
	public void testVisibility ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();

		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		// Test mappings
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mapMgr.storeMappings ( "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
		
		AccessControlManager accMgr = mgrFact.newAccessControlManager ( editorUser.getEmail (), testSecret );
		accMgr.setEntitiesVisibility ( "true", "2014-12-31", "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
	
		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( 
			null, "%.setEntitiesVisibility", 
			Arrays.asList ( p ( "publicFlag", "true" ), p ( "%Date", "2014%" ), p ( "entity", null, "%acc1" ) )
		);

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved (test.new.user)!", 1, proves.size () );
	}
	
	@Test
	public void testVisibilityAndUris ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();

		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		smgr.storeServices ( Service.UNSPECIFIED_SERVICE );
		
		Service service6 = smgr.getServices ( "test.testmain.service6" ).getServices ().iterator ().next ();
		
		String universalUri = "http://totally.faked.uri/bygyx67cc6/ACC:123";
		
		// Test mappings, let's use some URIs as well
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mapMgr.storeMappingBundle ( 
			"<" + EntityIdResolver.buildUriFromAcc ( "acc1", service6.getUriPattern () ) + ">",
			"test.testmain.service8:acc1",
			":<" + universalUri + ">"
		);
		
		AccessControlManager accMgr = mgrFact.newAccessControlManager ( editorUser.getEmail (), testSecret );
		accMgr.setEntitiesVisibility ( 
			"true", "2014-12-31", 
			"<" + EntityIdResolver.buildUriFromAcc ( "acc1", service6.getUriPattern () ) + ">",
			"test.testmain.service8:acc1",
			":<" + universalUri + ">"
		);
	
		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( 
			null, "%.setEntitiesVisibility", 
			Arrays.asList ( p ( "publicFlag", "true" ), p ( "%Date", "2014%" ), p ( "entity", service6.getName (), "acc1" ) )
		);

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved!", 1, proves.size () );

		// Turn URI into service:accession, provenance entry param are always normalised this way
		proves = provDao.find ( 
			null, "%.setEntitiesVisibility", 
			Arrays.asList (
				p ( "publicFlag", "true" ), p ( "%Date", "2014%" ),
				ProvenanceRegisterParameter.pent ( 
					provDao.getEntityIdResolver (),
					"<" + EntityIdResolver.buildUriFromAcc ( "acc1", service6.getUriPattern () ) + ">" 
				) 
			)
		);
		out.println ( "------ MAPPING RECORDS FROM SPLIT URI: " + proves );
		assertEquals ( "Expected provenance records not saved!", 1, proves.size () );

		
		// Universal URI
		proves = provDao.find ( 
			null, "%.setEntitiesVisibility", 
			Arrays.asList (
				p ( "publicFlag", "true" ), p ( "%Date", "2014%" ),
				p ( "entity", Service.UNSPECIFIED_SERVICE_NAME, universalUri ) 
			)
		);
		out.println ( "------ MAPPING RECORDS GOT FROM UNIVERSAL URI: " + proves );
		assertEquals ( "Expected provenance records not saved!", 1, proves.size () );
	}
}
