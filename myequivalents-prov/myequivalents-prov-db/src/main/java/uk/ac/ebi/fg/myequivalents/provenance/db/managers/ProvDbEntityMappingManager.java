package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.pent;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbEntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * A wrapper of {@link DbEntityMappingManager} that uses the provenance register to keep track of mapping-related
 * changes in myEquivalents.
 *
 * <dl><dt>date</dt><dd>16 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbEntityMappingManager extends DbEntityMappingManager
{
	private ProvenanceRegisterEntryDAO provRegDao;
	
	/**
	 * Logins as anonymous.
	 */
	ProvDbEntityMappingManager ( EntityManager em ) {
		this ( em, null, null );
	}

	
	/**
	 * You don't instantiate this class directly, you must use the {@link DbManagerFactory}.
	 */
	ProvDbEntityMappingManager ( EntityManager em, String email, String apiPassword )
	{
		super ( em, email, apiPassword );
		provRegDao = new ProvenanceRegisterEntryDAO ( this.entityManager );
	}

	/**
	 * Stores "mapping.storeMappings"  and 'entity' parameters into into provenance register
	 */
	@Override
	public void storeMappings ( String ... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return;
		super.storeMappings ( entityIds );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "mapping.storeMappings", pent ( Arrays.asList ( entityIds ) ) 
	  ));
	  ts.commit ();
	}


	/**
	 * Stores "mapping.storeMappingBundle"  and 'entity' parameters into into provenance register
	 */
	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return;
		super.storeMappingBundle ( entityIds );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "mapping.storeMappingBundle", pent ( Arrays.asList ( entityIds ) ) 
	  ));
	  ts.commit ();
	}


	/**
	 * Stores "mapping.deleteMappings"  and 'entity' parameters into into provenance register
	 */
	@Override
	public int deleteMappings ( String ... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;
		int result = super.deleteMappings ( entityIds );
		if ( result == 0 ) return 0;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "mapping.deleteMappings", pent ( Arrays.asList ( entityIds ) ) 
	  ));
	  ts.commit ();
	  
	  return result;
	}

	/**
	 * Stores "mapping.deleteEntities"  and 'entity' parameters into into provenance register
	 */
	@Override
	public int deleteEntities ( String ... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;
		int result = super.deleteEntities ( entityIds );
		if ( result == 0 ) return 0;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "mapping.deleteEntities", pent ( Arrays.asList ( entityIds ) ) 
	  ));
	  ts.commit ();
	  
	  return result;
	}
	
}