package org.restit.objectmapping;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class ClassRegistration {

	private Class clazz;
	private JsonDeserializer jsonDeserializer;
	private JsonSerializer jsonSerializer;
	
	public ClassRegistration(Class clazz, JsonSerializer jsonSerializer, JsonDeserializer jsonDeserializer)
	{
		setClazz(clazz);
		setJsonSerializer(jsonSerializer);
		setJsonDeserializer(jsonDeserializer);
	}

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public JsonDeserializer getJsonDeserializer() {
		return jsonDeserializer;
	}

	public void setJsonDeserializer(JsonDeserializer jsonDeserializer) {
		this.jsonDeserializer = jsonDeserializer;
	}

	public JsonSerializer getJsonSerializer() {
		return jsonSerializer;
	}

	public void setJsonSerializer(JsonSerializer jsonSerializer) {
		this.jsonSerializer = jsonSerializer;
	}
	
	
}
