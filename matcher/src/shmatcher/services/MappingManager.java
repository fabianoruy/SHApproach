package shmatcher.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import shmatcher.model.Concept;
import shmatcher.model.Element;
import shmatcher.model.Notion;
import shmatcher.model.SHInitiative;

/** Responsible for providing the services for the mapping tasks. */
public class MappingManager {
    private SHInitiative initiative;
    private String parsingResults = "";

    public MappingManager(SHInitiative initiative) {
	this.initiative = initiative;
    }

    public JsonElement createJSON() {
	JsonObject root = new JsonObject();
	JsonObject elements = new JsonObject();
	JsonObject concepts = new JsonObject();
	int i = 0;
	for (Notion notion : initiative.getAllNotions()) {
	    JsonObject jnotion = new JsonObject();
	    jnotion.addProperty("name", notion.getName());
//	    jnotion.addProperty("definition", notion.getDefinition());
	    if (notion instanceof Element) {
//		jnotion.addProperty("model", "Model");
//		elements.add("id"+i, jnotion);
		
	    } else if (notion instanceof Concept) {
		jnotion.addProperty("ontology", "Ontology");
		concepts.add("id"+i,jnotion);
	    }
	    i++;
	}
	root.add("elements", elements);
	root.add("concepts", concepts);
	return root;
    }
    
//    public JsonElement createJSON() {
//	JsonObject root = new JsonObject();
//	JsonArray elements = new JsonArray();
//	JsonArray concepts = new JsonArray();
//	int i = 0;
//	for (Notion notion : initiative.getAllNotions()) {
//	    JsonObject jnotion = new JsonObject();
//	    jnotion.addProperty("id", "id"+i);
//	    jnotion.addProperty("name", notion.getName());
//	    jnotion.addProperty("definition", notion.getDefinition());
//	    if (notion instanceof Element) {
//		jnotion.addProperty("model", "Model");
//		elements.add(jnotion);
//	    } else if (notion instanceof Concept) {
//		jnotion.addProperty("ontology", "Ontology");
//		concepts.add(jnotion);
//	    }
//	    i++;
//	}
//	root.add("elements", elements);
//	root.add("concepts", concepts);
//	return root;
//    }
}
