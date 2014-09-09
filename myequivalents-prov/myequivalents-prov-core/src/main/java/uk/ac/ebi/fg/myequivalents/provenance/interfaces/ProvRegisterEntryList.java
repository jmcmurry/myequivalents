package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * Defines a JAXB wrapper for a collection of {@link ProvenanceRegisterEntry} elements.
 *
 * <dl><dt>date</dt><dd>3 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
@XmlRootElement ( name = "provenance" )
@XmlAccessorType ( XmlAccessType.NONE )
@XmlType ( name = "", propOrder = { "entries" } )
public class ProvRegisterEntryList
{
	private List<ProvenanceRegisterEntry> entries;
	
	protected ProvRegisterEntryList () {}

	public ProvRegisterEntryList ( List<ProvenanceRegisterEntry> entries )
	{
		super ();
		this.entries = entries;
	}

	//@XmlElementWrapper( name = "entries" )
	@XmlElement ( name = "entry" )
	public List<ProvenanceRegisterEntry> getEntries ()
	{
		return entries;
	};
}
