/**
 * This package exists in this project to hack-override classes from the JSON Patch project at https://github.com/fge/json-patch.
 * The overrides enable the patch operations to call into PatchListener implementations when a patch operation is performed.
 * This enables thing such as the "remove" operation to be performed not only on the JSON, but for the listener to properly delete the targeted 
 * entity in the database.
 * Without this, the patch would only be applied to the JSON representation of the document and it would be difficult (at best) to know when
 * to remove the deleted item from a collection.
 */
package com.github.fge.jsonpatch;