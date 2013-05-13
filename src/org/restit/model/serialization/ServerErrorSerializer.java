package org.restit.model.serialization;

import java.lang.reflect.Type;

import org.restit.model.ServerError;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ServerErrorSerializer implements JsonSerializer<ServerError> {

	@Override
	public JsonElement serialize(ServerError serverError, Type typeOfSrc, JsonSerializationContext arg2context) {
		// TODO Auto-generated method stub
		return null;
	}

}
