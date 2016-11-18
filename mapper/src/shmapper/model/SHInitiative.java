package shmapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shmapper.model.Mapping.MappingStatus;

/* Represents a Standard Harmonization Initiative. */
public class SHInitiative {
	private String				domain;
	private String				purpose;
	private String				scope;
	private String				people;
	private String				description;
	private String				astahPath;
	private List<Package>		packages;
	private List<Mapping>		mappings;
	private Map<String, Notion>	notionMap;

	public SHInitiative(String domain) {
		this.domain = domain;
		this.packages = new ArrayList<Package>();
		this.mappings = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
	}

	public SHInitiative(String domain, String purpose, String scope, String people, String path) {
		this(domain);
		this.purpose = purpose;
		this.scope = scope;
		this.people = people;
		this.astahPath = path;
	}

	/* Creating the Mappings for this Initiative. */
	public void createMappings() {
		// Getting the models
		SeonView seon = getSeonView();
		IntegratedModel integrated = getIntegratedCM();
		List<StandardModel> standards = getStandardCMs();

		// One VM for Standard (Std * 1)
		for (int i = 0; i < standards.size(); i++) {
			mappings.add(new VerticalMapping(standards.get(i), seon));
		}

		// One HM for each pair of Standards (Std * (Std-1))
		for (int i = 0; i < standards.size(); i++) {
			for (int j = i + 1; j < standards.size(); j++) {
				mappings.add(new HorizontalMapping(standards.get(i), standards.get(j)));
			}
		}

		// A single DM (1) with all standards as base
		mappings.add(new DiagonalMapping(standards, integrated));
	}
	
	// Resets all the initiative's packages, mappings and notions.
	public void resetInitiative() {
		this.packages = new ArrayList<Package>();
		this.mappings = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
	}

	public String getDomain() {
		return domain;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getPeople() {
		return people;
	}

	public void setPeople(String people) {
		this.people = people;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAstahPath() {
		return astahPath;
	}

	public void setAstahPath(String path) {
		this.astahPath = path;
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

	public int getStartedMappingsNumber() {
		int count = 0;
		for (Mapping mapping : mappings) {
			if (mapping.getStatus() == MappingStatus.STARTED) count++;
		}
		return count;
	}

	public Mapping getMappingById(String mapId) {
		for (Mapping map : mappings) {
			if (map.getId().equals(mapId)) {
				return map;
			}
		}
		return null;
	}

	public List<VerticalMapping> getVerticalMappings() {
		List<VerticalMapping> vmaps = new ArrayList<VerticalMapping>();
		for (Mapping map : mappings) {
			if (map instanceof VerticalMapping) {
				vmaps.add((VerticalMapping) map);
			}
		}
		// System.out.println("Returning " + vmaps.size() + " VMs: " + vmaps);
		return vmaps;
	}

	public List<HorizontalMapping> getHorizontalMappings() {
		List<HorizontalMapping> hmaps = new ArrayList<HorizontalMapping>();
		for (Mapping map : mappings) {
			if (map instanceof HorizontalMapping) {
				hmaps.add((HorizontalMapping) map);
			}
		}
		return hmaps;
	}

	public DiagonalMapping getDiagonalMapping() {
		for (Mapping map : mappings) {
			if (map instanceof DiagonalMapping) {
				return (DiagonalMapping) map;
			}
		}
		return null;
	}

	public void addMapping(Mapping mapping) {
		this.mappings.add(mapping);
	}

	public List<Notion> getAllNotions() {
		return new ArrayList<Notion>(notionMap.values());
	}

	public void addNotion(Notion notion) {
		this.notionMap.put(notion.getId(), notion);
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
	public IntegratedModel getIntegratedCM() {
		for (Package pack : packages) {
			if (pack instanceof IntegratedModel && !((IntegratedModel) pack).isStructural()) {
				return (IntegratedModel) pack;
			}
		}
		return null;
	}

	/* Returns the Standard Content Models (SCMs). */
	public List<StandardModel> getStandardCMs() {
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