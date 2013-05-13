package org.restit.objectmapping;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;

public class RestItMapper {

	private static final String LOG_TAG = RestItMapper.class.getName();
	protected static Map<String, ClassRegistration> classMaps;
	
	/**
	 * Get the map of java classes and json class names
	 * @return
	 */
	protected static Map<String, ClassRegistration> getClassMaps()
	{
		if(classMaps == null)
		{
			classMaps = new HashMap<String, ClassRegistration>();
		}
		
		return classMaps;
	}
	
	/**
	 * Register a java class with a json class
	 * @param jsonName The name of the json key
	 * @param clazz The java class that is associated with the json key
	 */
	public static void addClass(String jsonName, Class clazz)
	{
		if(jsonName == null || clazz == null)
			return;
		
		addClass(jsonName, clazz, null, null);
	}
	
	/**
	 * Register a java class with a json class
	 * @param jsonName The name of the json key
	 * @param clazz The java class that is associated with the json key
	 * @param jsonSerializer Custom object to json mappings
	 * @param jsonDeserializer Cutom json to object mappings
	 */
	public static void addClass(String jsonName, Class clazz, JsonSerializer jsonSerializer, JsonDeserializer jsonDeserializer)
	{
		if(jsonName == null || clazz == null)
			return;
		
		//create class registration object
		ClassRegistration classRegistration = new ClassRegistration(clazz, jsonSerializer, jsonDeserializer);
		
		//store relationship
		getClassMaps().put(jsonName, classRegistration);
	}
	
	/**
	 * Convert a json string to a registered Pojo Object
	 * @param jsonString
	 * @return
	 */
	public static Object toPojo(String jsonString)
	{
		if(jsonString == null)
			return null;
		
		try 
		{
			Object convertedObject = null;
			
			//first create JSON object
			JSONObject parentObject = new JSONObject(jsonString);
			if(parentObject.length() > 0)
			{
				//get the first key to see if it matches a known class
				String key = (String)parentObject.names().get(0);
				if(key != null && getClassMaps().containsKey(key))
				{
					//the key that we have found matches a known class
					ClassRegistration classRegistration = getClassMaps().get(key);
					Object childObject = parentObject.get(key);
					if(classRegistration != null && classRegistration.getClass() != null && childObject != null)
					{	
						
						String json = null;
			
						if(childObject instanceof JSONArray)
						{
							json = childObject.toString();
							
							//loop through JSON Array and parse each object
							List<Object> results = new ArrayList<Object>();
							JSONArray array = (JSONArray) childObject;
							for(int i = 0; i < array.length(); i++)
							{
								JSONObject jsonObject = (JSONObject)array.get(i);
								Object value = toPojo(jsonObject.toString(), classRegistration.getClazz(), classRegistration.getJsonDeserializer());
								results.add(value);
							}
							
							return results;
						}
						else if(childObject instanceof JSONObject)
						{
							json = childObject.toString();
						} else
						{
							//if we are dealing with a json object use that. If we ended up with a key value pair, use the parent Object
							json = parentObject.toString();
						}
						

						convertedObject = toPojo(json, classRegistration.getClazz(), classRegistration.getJsonDeserializer());
					}
					
				} else
				{
					Log.w(LOG_TAG, "No class is registered for response key '"+ key +"'");
				}
			}
			
			
			
			
			return convertedObject;
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			
			return null;
		}
	}
	
	/**
	 * Create a POJO from a json string
	 * @param json
	 * @param clazz
	 * @param jsonDeserializer
	 * @return
	 */
	private static Object toPojo(String json, Class clazz, JsonDeserializer jsonDeserializer)
	{
		Gson gson = null;
		Object convertedObject = null;
		
		JsonReader reader = new JsonReader(new StringReader(json));
		//reader.setLenient(true); //allow malformed JSON
		
		
		if(jsonDeserializer != null)
		{
			//perform custom object mapping
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(clazz, jsonDeserializer);

			gson = builder.create();
				
		} else
		{
			//do simple automatic mapping
			gson = new Gson();
		}
		
		//convert to pojo
		convertedObject =  gson.fromJson(reader, clazz);
		
		return convertedObject;
	}
}
