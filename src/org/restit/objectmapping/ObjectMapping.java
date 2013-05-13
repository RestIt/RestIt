package org.restit.objectmapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to create a mapping object that can be easily defined without writing specific serializer and deserializer
 *  objects
 * @author coneill
 *
 */
public class ObjectMapping {

	private Class clazz;
	private String jsonName;
	private Map<String, String> attributeMap;
	
	/**
	 * Constructor
	 * @param jsonName The JSON class key name
	 * @param clazz The POJO class
	 */
	public ObjectMapping(String jsonName, Class clazz)
	{
		this.jsonName = jsonName;
		this.clazz = clazz;
	}
	
	/**
	 * Get the attribute map
	 * @return
	 */
	private Map<String, String> getAttributeMap()
	{
		if(this.attributeMap == null)
		{
			this.attributeMap = new HashMap<String, String>();
		}
		
		return this.attributeMap;
	}
	
	/**
	 * Add a direct mapping between the response key name and the POJO attribute
	 * @param responseKey
	 * @param pojoVariable
	 */
	public void addAttributeMapping(String responseKey, String pojoAttribute)
	{
		if(responseKey == null || pojoAttribute == null)
			return;
		
		getAttributeMap().put(responseKey, pojoAttribute);
	}
	
}
