package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

public class PatchListenerAdapter implements PatchListener {

	@Override
	public void remove(JsonNode node, JsonPointer path) { }
	
}
