package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

public interface PatchListener {

	void remove(JsonNode node, JsonPointer path);

}
