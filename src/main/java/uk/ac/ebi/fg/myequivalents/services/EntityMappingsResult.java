package uk.ac.ebi.fg.myequivalents.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * 
 * TODO: Comment me!
 * 
 * <dl><dt>date</dt><dd>Jun 11, 2012</dd></dl>
 * @author brandizi
 *
 */
@XmlRootElement ( name = "mappings" )
@XmlAccessorType ( XmlAccessType.NONE )
@XmlType ( name = "", propOrder = { "services", "repositories", "serviceCollections", "bundles" } )
public class EntityMappingsResult
{
	@XmlRootElement ( name = "bundle" )
	@XmlAccessorType ( XmlAccessType.FIELD )
	public static class BundleSet
	{
		protected BundleSet () {
		}
		
		@XmlElement ( name = "entity" )
		private Set<Entity> bundle = new HashSet<Entity> ();

		private void addEntity ( Entity entity ) {
			this.bundle.add ( entity );
		}

		public Set<Entity> getBundle ()
		{
			return bundle;
		}
	}
	
	private final boolean addServices, addServiceCollections, addRepositories;
	
	private final Set<Service> services;
	private final Set<ServiceCollection> serviceCollections;
	private final Set<Repository> repositories;

	private final Map<String, BundleSet> bundles = new HashMap<String, BundleSet> ();
	
	
	EntityMappingsResult ()
	{
		this ( true, true, true );
	}

	EntityMappingsResult ( boolean addServices, boolean addServiceCollections, boolean addRepositories )
	{
		super ();
		this.addServices = addServices;
		this.addServiceCollections = addServiceCollections;
		this.addRepositories = addRepositories;
		
		services = addServices ? new HashSet<Service> () : null;
		serviceCollections = addServiceCollections ? new HashSet<ServiceCollection> () : null;
		repositories = addRepositories ? new HashSet<Repository> () : null;
	}

	@XmlElementWrapper ( name = "services" )
	@XmlElement ( name = "service" ) 
	public Set<Service> getServices ()
	{
		return services;
	}
		
	@XmlElementWrapper ( name = "service-collections" )
	@XmlElement ( name = "service-collection" ) 
	public Set<ServiceCollection> getServiceCollections ()
	{
		return serviceCollections;
	}
	
	@XmlElementWrapper ( name = "repositories" )
	@XmlElement ( name = "repository" ) 
	public Set<Repository> getRepositories ()
	{
		return repositories;
	}

	@XmlElementWrapper ( name = "bundles" )
	@XmlElement ( name = "bundle" )
	public Collection<BundleSet> getBundles ()
	{
		return bundles.values ();
	}
 
	
	void addEntityMapping ( EntityMapping em ) 
	{
		String bundleId = em.getBundle ();
		BundleSet bundle = bundles.get ( em.getBundle () );
		if ( bundle == null ) {
			bundles.put ( bundleId, bundle = new BundleSet () );
		}
		bundle.addEntity ( em.getEntity () );
		Service service = em.getService ();
		if ( addServices ) services.add ( service );
		
		if ( addServiceCollections ) {
			ServiceCollection serviceCollection = service.getServiceCollection ();
			if ( serviceCollection != null ) serviceCollections.add ( serviceCollection );
		}
		
		if ( addRepositories ) {
			Repository repo = service.getRepository ();
			if ( repo != null ) repositories.add ( repo );
		}
	}
	
	void addAllEntityMappings ( Collection<EntityMapping> mappings ) {
		for ( EntityMapping em: mappings ) addEntityMapping ( em );
	}

	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ( "EntityMappingResult {\n" );
		
		sb.append ( "  services: {\n" );
		for ( Service service: services )
			sb.append ( "    " ).append ( service.toString () + "\n");
		sb.append ( "  }\n" );
		
		sb.append ( "  repositories: {\n" );
		for ( Repository repo: repositories )
			sb.append ( "    " ).append ( repo.toString () );
		sb.append ( "  }\n" );

		sb.append ( "  service-collections: {\n" );
		for ( ServiceCollection sc: serviceCollections )
			sb.append ( "    " ).append ( sc.toString () );
		sb.append ( "  }\n" );

		sb.append ( "  bundles: {\n" );
		for ( BundleSet bundle: this.getBundles () )
		{
			sb.append ( "    {\n" );
			for ( Entity entity: bundle.getBundle () )
				sb.append ( "      " + entity.toString () );
			sb.append ( "    }\n" );
		}
		sb.append ( "  }\n" );
		
		return sb.toString ();
	}
}
