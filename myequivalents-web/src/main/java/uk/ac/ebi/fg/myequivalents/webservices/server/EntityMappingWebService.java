package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

/**
 * <p>The web service version of the {@link EntityMappingManager} interface. This uses Jersey and set up a REST web service
 * See {@link uk.ac.ebi.fg.myequivalents.webservices.client.EntityMappingWSClientTest} for usage examples.</p>
 * 
 * <p>The web services are backed by a {@link ManagerFactory}, which needs to be configured via Spring, see {@link Resources}.
 * By default {@link DbManagerFactory} is used.</p>
 * 
 * <p>Usually these services are located at /ws/mapping, e.g., 
 * "https://localhost:8080/ws/mapping/get?entityId=service1:acc1". You can build the path by appending the value in 
 * &#064;Path (which annotates every service method) to /mapping.</p> 
 *
 * <p>TODO: We need proper exception handling, see <a href = "http://jersey.java.net/documentation/latest/user-guide.html#https://jersey.java.net/documentation/latest/user-guide.html#d0e6592">here</a>.</p>
 * 
 * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/mapping" )
public class EntityMappingWebService
{
	@Context 
	private ServletContext servletContext;
		
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * We need this version of {@link EntityMappingManager#getMappings(Boolean, String...)} because Jersey/JAX-WS doesn't like arrays.
	 */
	@POST
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappings ( 
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "raw" ) Boolean isRaw, 
		@FormParam ( "entity" ) List<String> entityIds 
	) 
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		EntityMappingSearchResult result = emgr.getMappings ( isRaw, entityIds.toArray ( new String [ 0 ] ) );
		emgr.close();
		return result; 
	}
	
	/** 
	 * This is just the HTTP/GET version of {@link #getMappings(Boolean, List)}. Normally you will want the 
	 * POST invocation, this is here for testing purposes only. 
	 */
	@GET
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsViaGET ( 
		@QueryParam ( "login" ) String email, 
		@QueryParam ( "login-secret" ) String apiPassword,
		@QueryParam ( "raw" ) Boolean isRaw, 
		@QueryParam ( "entity" ) List<String> entityIds 
	) 
	{
		return getMappings ( email, apiPassword, isRaw, entityIds );
	}
	
	/**
	 * This is the equivalent of {@link EntityMappingManager#storeMappings(String...)}.
	 */
	@POST
	@Path( "/store" )
	@Produces ( MediaType.APPLICATION_XML )
	public void storeMappings (
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "entity" ) List<String> entityIds 
	)
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		emgr.storeMappings ( entityIds.toArray ( new String [ 0 ]) );
		emgr.close();
	}

	/**
	 * This is the equivalent of {@link EntityMappingManager#storeMappingBundle(String...)}.
	 */
	@POST
	@Path( "/bundle/store" )
	@Produces ( MediaType.APPLICATION_XML )
	public void storeMappingBundle (
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "entity" ) List<String> entityIds 
	)
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		emgr.storeMappingBundle ( entityIds.toArray ( new String [ 0 ]) );
		emgr.close();
	}

	/**
	 * This is the equivalent of {@link EntityMappingManager#storeMappingBundles(EntityMappingSearchResult)}.
	 */
	@POST
	@Path( "/bundles/store" )
	@Produces ( MediaType.APPLICATION_XML )
	public void storeMappingBundles (
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "mappings-xml" ) String mappingsXml
	)
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		emgr.storeMappingBundlesFromXML ( new StringReader ( mappingsXml ) );
		emgr.close();
	}

	
	
	
	/**
	 * This is the equivalent of {@link EntityMappingManager#deleteMappings(String...)}.
	 * @return an integer in the form of a string, cause Jersey doesn't like other types very much.
	 */
	@POST
	@Path( "/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteMappings ( 
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "entity" ) List<String> entityIds 
	)
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		int result = emgr.deleteMappings ( entityIds.toArray ( new String [ 0 ]) );
		emgr.close ();
		return String.valueOf ( result );
	}

	/**
	 * This is the equivalent of {@link EntityMappingManager#deleteEntities(String...)}.
	 * @return an integer in the form of a string, cause Jersey doesn't like other types very much.
	 */
	@POST
	@Path( "/entity/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteEntities ( 
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "entity" ) List<String> entityIds 
	)
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		int result = emgr.deleteEntities ( entityIds.toArray ( new String [ 0 ]) );
		emgr.close ();
		return String.valueOf ( result );
	}
	
	/**
	 * This is the equivalent of {@link EntityMappingManager#getMappingsForTarget(Boolean, String, String)}.
	 */
	@POST
	@Path( "/target/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsForTarget (
		@FormParam ( "login" ) String email, 
		@FormParam ( "login-secret" ) String apiPassword,
		@FormParam ( "raw" ) Boolean wantRawResult, 
		@FormParam ( "service" ) String targetServiceName, 
		@FormParam ( "entity" ) String entityId )
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		EntityMappingSearchResult result = emgr.getMappingsForTarget ( wantRawResult, targetServiceName, entityId );
		emgr.close();
		return result; 
	}

	/** 
	 * This is just the HTTP/GET version of {@link #getMappingsForTarget(Boolean, String, String)}. Normally you will want the 
	 * POST invocation, this is here for testing purposes only. 
	 */
	@GET
	@Path( "/target/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsForTargetViaGET (
			@QueryParam ( "login" ) String email, 
			@QueryParam ( "login-secret" ) String apiPassword,
			@QueryParam ( "raw" ) Boolean wantRawResult, 
			@QueryParam ( "service" ) String targetServiceName, 
			@QueryParam ( "entity" ) String entityId )
	{
		return getMappingsForTarget ( email, apiPassword, wantRawResult, targetServiceName, entityId );
	}

	/**
	 * <p>This returns the result from {@link #getMappingsForTarget(Boolean, String, String)} in the format of 
	 * an HTTP 301 response, so the browser is re-directed to the result's URI {@link Entity#getURI()}. In case
	 * the result is not unique, some XML is returned, that is similar to the XML returned by 
	 * {@link #getMappingsAs(String, Boolean, String...) getMappingsAs ( "xml", ... )}. You can couple such XML
	 * with the URL of an XSL, using the xsl/xslUri parameter. The default for such xsl is /go-to-target/multiple-results.xsl.
	 * See the myEquivalents wiki for details. If the input has no mapping, then HTTP/404 (Not Found) is returned</p> 
	 * 
	 * <p>You can test this with the examples (after 'mvn jetty:run'):
	 *   <ul>  
	 *   	<li><a href = "http://localhost:8080/ws/mapping/target/go-to?entity=test.testweb.service8:acc10&service=test.testweb.service7">common case</a></li>
	 *   	<li><a href = "http://localhost:8080/ws/mapping/target/go-to?entity=test.testweb.service7:acc1&service=test.testweb.service6">multiple entities on the target</a></li>
	 *   	<li><a href = "http://localhost:8080/ws/mapping/target/go-to?entity=foo-service:foo-acc&service=foo-target">non-existing target</a></li>
	 *   </ul>
	 * </p>
	 */
	@GET
	@Path( "/target/go-to" )
	@Produces ( MediaType.TEXT_XML )
	public Response getMappingsForTargetRedirection (
			@QueryParam ( "login" ) String email, 
			@QueryParam ( "login-secret" ) String apiPassword,
			@QueryParam ( "service" ) String targetServiceName, 
			@QueryParam ( "entity" ) String entityId,
			@QueryParam ( "xsl" ) String xslUri 
	) throws URISyntaxException
	{
		EntityMappingSearchResult result = getMappingsForTarget ( email, apiPassword, false, targetServiceName, entityId );
		Collection<Bundle> bundles = result.getBundles ();

		if ( bundles.size () == 0 ) return Response.status ( Status.NOT_FOUND ).build ();
		if ( bundles.size () > 1 ) throw new RuntimeException ( 
			"Internal error while getting mappings for '" + entityId + "' to '" + targetServiceName 
			+ ": there is more than a bundle as result and that's impossible, must be some bug, sorry." );
		
		Bundle bundle = bundles.iterator ().next ();
		Set<Entity> entities = bundle.getEntities ();
		if ( entities.size () == 1 )
			return Response.status ( Status.MOVED_PERMANENTLY ).location ( 
					new URI ( entities.iterator ().next ().getURI () ) ).build ();
		
		if ( xslUri == null )
			xslUri = UriBuilder.fromPath ( 
				servletContext.getContextPath () + "/go-to-target/multiple-results.xsl" ).build ().toASCIIString ();
		else
			xslUri = new URI ( xslUri ).toASCIIString ();
		
		String resultXml = JAXBUtils.marshal ( result, EntityMappingSearchResult.class );

		// Inject the XSL pointer into the XML being returned, this will usually make the browser to automatically apply
		// the XSL rendering.
		resultXml = resultXml.replaceFirst ( 
			"<\\?xml(.*)\\?>", "<?xml$1 ?>\n" + "<?xml-stylesheet type='text/xsl' href='" + xslUri + "' ?>\n" );
		
		// Turn the result XML into a single-result syntax
		resultXml = resultXml.replaceFirst ( 
			"<mappings>", 
			String.format ( "<mappings entity-id = '%s' target-service-name = '%s'>\n", entityId, targetServiceName ) 
		);
		
		return Response.ok ( resultXml, MediaType.TEXT_XML ).build ();		
	}

	
	/** 
	 * Gets the {@link EntityMappingManager} that is used internally in this web service. This is obtained from
	 * {@link Resources} and hence it depends on the Spring configuration, accessed through {@link WebInitializer}. 
	 * 
	 * TODO: AOP 
	 * 
	 */
	private EntityMappingManager getEntityMappingManager ( String email, String apiPassword )
	{
		log.trace ( "Returning mapping manager for the user {}, {}", email, apiPassword == null ? null : "***" );
		return Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( email, apiPassword );
	}
}
