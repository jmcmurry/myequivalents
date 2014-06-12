package uk.ac.ebi.fg.myequivalents.provenance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ExposedService;
import uk.ac.ebi.fg.myequivalents.model.Describeable;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>6 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
@Embeddable
public class ProvenanceRegistryParameter
{
	private String valueType;
	private String value;
	
	protected ProvenanceRegistryParameter () {
	}
	
	public ProvenanceRegistryParameter ( String valueType, String value )
	{
		super ();
		this.valueType = valueType;
		this.value = value;
	}

	public ProvenanceRegistryParameter ( String value )
	{
		this ( null, value );
	}

	@Column ( length = 50 )
	@Index ( name = "prov_param_vtype" )
	public String getValueType ()
	{
		return valueType;
	}
	
	protected void setValueType ( String valueType )
	{
		this.valueType = valueType;
	}

	
	@Column ( length = 2000 )
	@Index ( name = "prov_param_val" )
	public String getValue ()
	{
		return value;
	}
	
	protected void setValue ( String value )
	{
		this.value = value;
	}

	public static List<ProvenanceRegistryParameter> buildFromPairs ( Collection<String> typeValuePairs )
	{
		if ( typeValuePairs == null || typeValuePairs.isEmpty () ) return null;
		if ( typeValuePairs.size () % 2 != 0 ) throw new RuntimeException ( 
			"Internal error: cannot build a list of ProvenanceRegistryParameter from an uneven number of string pairs" );
		int sz = typeValuePairs.size () - 1;
		List<ProvenanceRegistryParameter> result = new ArrayList<ProvenanceRegistryParameter> ( (int) Math.ceil ( sz / 2 ) );
		
		for ( Iterator<String> itr = typeValuePairs.iterator (); itr.hasNext ();  )
			result.add ( new ProvenanceRegistryParameter ( itr.next (), itr.next () ) );
		
		return result;
	}
	
	public static List<ProvenanceRegistryParameter> buildFromValues ( String valueType, Collection<String> values )
	{
		if ( values == null || values.isEmpty () ) return null;
		List<ProvenanceRegistryParameter> result = new ArrayList<ProvenanceRegistryParameter> ( values.size () );
		for ( String value: values )
			result.add ( new ProvenanceRegistryParameter ( value ) );
		return result;
	}
	

	public static <D extends Describeable> List<ProvenanceRegistryParameter> buildFromObjects 
		( List<ProvenanceRegistryParameter> result, Collection<D> objects )
	{
		if ( objects == null || objects.isEmpty () ) return result;
		
		if ( result == null ) result = new ArrayList<ProvenanceRegistryParameter> ( objects.size () );
		for ( Describeable d: objects ) 
		{
			if ( d == null ) continue;
			String type = d instanceof ExposedService ? "service" : StringUtils.uncapitalize ( d.getClass ().getSimpleName () );
			String value = d.getName ();
			result.add ( new ProvenanceRegistryParameter ( type, value ) );
		}
		return result;
	}
	
	public static <D extends Describeable> List<ProvenanceRegistryParameter> buildFromObjects ( Collection<D> objects )
	{
		return buildFromObjects ( null, objects );
	}
	
	
	
	@Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	ProvenanceRegistryParameter that = (ProvenanceRegistryParameter) o;
    if ( this.getValueType () == null || that.getValueType () == null || !this.valueType.equals ( that.valueType ) ) return false; 
    if ( this.getValue () == null || that.getValue () == null || !this.value.equals ( that.value ) ) return false; 
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getValueType () == null ) ? 0 : valueType.hashCode () );
		result = 31 * result + ( ( this.getValue () == null ) ? 0 : value.hashCode () );
		return result;
	}
  
  @Override
  public String toString() {
  	return String.format ( 
  		"%s { valueType: '%s', value: '%s' }", this.getClass ().getSimpleName (), getValueType (), getValue ()
  	);
  }

}
