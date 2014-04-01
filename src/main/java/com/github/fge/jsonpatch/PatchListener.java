package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

public interface PatchListener {

  void add(JsonNode node, JsonPointer path);
  
  void remove(JsonNode node, JsonPointer path);
  
  void replace(JsonNode node, JsonPointer path, JsonNode value);

}
