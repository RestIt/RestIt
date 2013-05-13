package org.restit.model.serialization;

import java.lang.reflect.Type;

import org.restit.model.ServerError;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ServerErrorDeserializer implements JsonDeserializer<ServerError> {

	@Override
	public ServerError deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext context) throws JsonParseException 
	{
		if(jsonElement == null)
			return null;
		
		String message = null;
		if(jsonElement.isJsonPrimitive())
		{
			message = jsonElement.getAsString();
		} else 
		{
			//nested object
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			message = jsonObject.get(jsonObject.entrySet().iterator().next().getKey()).getAsString();
		}
		
		ServerError error = new ServerError();
		error.setMessage(message);
		
		return error;
	}

}
