package shmapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Represents a Standard Harmonization Initiative. */
public class SHInitiative {
	private String				domain;
	private String				purpose;
	private String				scope;
	private String				description;
	private List<Package>		packages;
	private List<Mapping>		mappings;
	private Map<String, Notion>	notionMap;

	public SHInitiative(String domain, String purpose, String scope) {
		this.domain = domain;
		this.purpose = purpose;
		this.scope = scope;
		this.packages = new ArrayList<Package>();
		this.mappings = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
	}

	public void createMappings() {
		// Creating the Mappings for this Initiative
		SeonView seon = getSeonView();
		IntegratedModel integrated = getICM();
		List<StandardModel> standards = getSCMs();
		for (int i = 0; i < standards.size(); i++) {
			mappings.add(new VerticalMapping(standards.get(i), seon));
			mappings.add(new HorizontalMapping(standards.get(i), integrated));
			for (int j = i + 1; j < standards.size(); j++) {
				mappings.add(new HorizontalMapping(standards.get(i), standards.get(j)));
			}
		}
		System.out.println("Mappings Created: ");
		System.out.println(mappings);
	}

	public String getDomain() {
		return domain;
	}

	public String getPurpose() {
		return purpose;
	}

	public String getScope() {
		return scope;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Package> getAllPackages() {
		return this.packages;
	}

	public void addPackage(Package pack) {
		this.packages.add(pack);
	}

	public List<Mapping> getMappings() {
		return mappings;
	}

	public void addMapping(Mapping mapping) {
		this.mappings.add(mapping);
	}

	public List<Notion> getAllNotions() {
		return new ArrayList<Notion>(notionMap.values());
	}

	public void addNotion(Notion notion) {
		this.notionMap.put(notion.getAstahClass().getId(), notion);
	}

	public Notion getNotionById(String id) {
		return notionMap.get(id);
	}

	/* Returns a Package by id. */
	public Package getPackage(String id) {
		for (Package pack : packages) {
			if (pack.getId().equals(id)) {
				return pack;
			}
		}
		return null;
	}

	/* Returns the SEON View. */
	public SeonView getSeonView() {
		for (Package pack : packages) {
			if (pack instanceof SeonView) {
				return (SeonView) pack;
			}
		}
		return null;
	}

	/* Returns the Integrated Content Model (ICM). */
	public IntegratedModel getICM() {
		for (Package pack : packages) {
			if (pack instanceof IntegratedModel && ((IntegratedModel) pack).isStructural()) {
				return (IntegratedModel) pack;
			}
		}
		return null;
	}

	/* Returns the Standard Content Models (SCMs). */
	private List<StandardModel> getSCMs() {
		List<StandardModel> models = new ArrayList<StandardModel>();
		for (Package pack : packages) {
			if (pack instanceof StandardModel && !((StandardModel) pack).isStructural()) {
				models.add((StandardModel) pack);
			}
		}
		return models;
	}

	@Override
	public String toString() {
		return this.domain;
	}

}