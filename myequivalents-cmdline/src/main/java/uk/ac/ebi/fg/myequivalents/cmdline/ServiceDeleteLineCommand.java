/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The line command for {@link ServiceManager#deleteServices(String...)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDeleteLineCommand extends LineCommand
{
	public ServiceDeleteLineCommand () {
		super ( "service delete" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		ServiceManager servMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( this.email, this.apiPassword );

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			servMgr.deleteServices ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nService(s) Deleted" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n service delete name..." );
		err.println (   "   Deletes services, identified by their symbolic names" );
		err.println (   "   WARNING This removes ALL the entities referring to the service!" );
	}	

}
