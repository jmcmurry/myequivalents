package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.ServiceManager;

/**
 * The 'mapping store' command. This will use {@link EntityMappingManager#deleteMappings(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingDeleteCommandLineCommand extends LineCommand
{
	public MappingDeleteCommandLineCommand ()
	{
		super ( "mapping delete" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		EntityMappingManager emMgr = new EntityMappingManager ();
		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			emMgr.deleteMappings ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nMapping(s) Deleted" );
		return;
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping delete <service:accession|uri>..." );
		err.println (   "   Deletes mappings between entities, which have to be listed as pairs of identifiers" );
	}

}
