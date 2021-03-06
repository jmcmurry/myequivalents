package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.Reader;

import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * <h2>The {@link MyEquivalentsManager manager} to access {@link Service}s (and related things)</h2>
 * 
 * <p>This is used to manager the contexts the {@link Entity entities} refer to,  
 * i.e., the {@link Service}s and connected things, namely {@link ServiceCollection}s and {@link Repository}s. 
 * The persistence-related invocations does the transaction management automatically (i.e., they commit all implied changes).</p>
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface ServiceManager extends MyEquivalentsManager
{

	/**
	 * Stores a set of services. Updates existing ones (identifying them by name) with new information in the parameter, 
	 * or create them from scratch if they don't exist in the backing storage system.
	 */
	public void storeServices ( Service ... services );

	/**
	 * Stores services described by means of XML passed to the parameter reader.
	 * 
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link ServiceManagerTest}, {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 */
	public void storeServicesFromXML ( Reader reader );

	/**
	 * Deletes services by name and returns the number of services that were actually deleted.
	 */
	public int deleteServices ( String ... names );

	/**
	 * Gets services by name. It pulls up related stuff (i.e., {@link ServiceCollection}s and {@link Repository repositories} 
	 * referred by the service) and put it all inside the {@link ServiceSearchResult} used as result.
	 * 
	 * It returns all the available services and related entities or only those that are public, depending on the current
	 * logged-in user (see {@link Describeable#isPublic()} and {@link #setAuthenticationCredentials(String, String)}).
	 *  
	 */
	public ServiceSearchResult getServices ( String ... names );

	/**
	 *  Returns the same result returned by {@link #getServices(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getServicesAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 *  
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getServicesAs ( String outputFormat, String ... names );

	/**
	 * Stores {@link ServiceCollection}s. This uses {@link ServiceCollectionDAO#store(ServiceCollection)} and wraps it 
	 * with transaction management. 
	 */
	public void storeServiceCollections ( ServiceCollection ... servColls );

	/**
	 * Deletes service-collections by name and returns the number of collections that were actually deleted.
	 */
	public int deleteServiceCollections ( String ... names );

	/**
	 * Gets {@link ServiceCollection}s by name. For coherence with the rest of this manager, puts the result into 
	 * {@link ServiceSearchResult}.
	 */
	public abstract ServiceSearchResult getServiceCollections ( String ... names );

	/**
	 *  Returns the same result returned by {@link #getServiceCollections(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getServiceCollectionAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 *  
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getServiceCollectionsAs ( String outputFormat, String ... names );

	/**
	 * Stores {@link Repository repositories} in the backing store. In case they exist, updates them with new information 
	 * in the parameter. Else, they are created from scratch in the backing store.
	 */
	public void storeRepositories ( Repository ... repos );

	/**
	 * Deletes {@link Repository repositories} by name and returns the number of the repos that were actually deleted. 
	 */
	public int deleteRepositories ( String ... names );

	/**
	 * Gets {@link Repository repositories} by name. For coherence with the rest of this manager, puts the result into 
	 * {@link ServiceSearchResult}.
	 */
	public ServiceSearchResult getRepositories ( String ... names );

	/**
	 *  Returns the same result returned by {@link #getRepositories(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getRepositoriesAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 *  
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getRepositoriesAs ( String outputFormat, String ... names );

	/**
	 * Does close/clean-up operations. There is no guarantee that a manager can be used after the invocation to this method.
	 * You may want to invoke this call in {@link Object#finalize()} in the implementation of this interface.
	 */
	public void close ();

}
