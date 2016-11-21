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
	private InitiativeStatus	status;
	private List<Package>		packages;
	private List<Mapping>		structmaps;
	private List<Mapping>		contentmaps;
	private Map<String, Notion>	notionMap;

	public static enum InitiativeStatus {
		CREATED, PARSED, STRUCTURED, CONTENTED, FINISHED
	}

	public SHInitiative(String domain) {
		this.domain = domain;
		this.packages = new ArrayList<Package>();
		this.structmaps = new ArrayList<Mapping>();
		this.contentmaps = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
		this.status = InitiativeStatus.CREATED;
	}

	public SHInitiative(String domain, String purpose, String scope, String people, String path) {
		this(domain);
		this.purpose = purpose;
		this.scope = scope;
		this.people = people;
		this.astahPath = path;
	}

	/* Creating the Content Mappings for this Initiative. */
	public void createContentMappings() {
		// Getting the models
		SeonView seon = getSeonView();
		IntegratedModel integrated = getIntegratedCM();
		List<StandardModel> standards = getStandardCMs();

		// One VM for Standard (Std * 1)
		for (int i = 0; i < standards.size(); i++) {
			contentmaps.add(new VerticalMapping(standards.get(i), seon));
		}
		// One HM for each pair of Standards (Std * (Std-1))
		for (int i = 0; i < standards.size(); i++) {
			for (int j = i + 1; j < standards.size(); j++) {
				contentmaps.add(new HorizontalMapping(standards.get(i), standards.get(j)));
			}
		}
		// One DM for Standard (Std * 1)
		for (int i = 0; i < standards.size(); i++) {
			contentmaps.add(new DiagonalMapping(standards.get(i), integrated));
		}
	}

	// Resets all the initiative's packages, mappings and notions.
	public void resetInitiative() {
		this.packages = new ArrayList<Package>();
		this.contentmaps = new ArrayList<Mapping>();
		this.structmaps = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
		this.status = InitiativeStatus.CREATED;
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

	public InitiativeStatus getStatus() {
		return status;
	}

	public void setStatus(InitiativeStatus status) {
		this.status = status;
	}

	public List<Package> getAllPackages() {
		return this.packages;
	}

	public void addPackage(Package pack) {
		this.packages.add(pack);
	}

	public List<Mapping> getContentMappings() {
		return contentmaps;
	}

	public List<Mapping> getStructuralMappings() {
		return structmaps;
	}

	public int getStartedContentMappingsNumber() {
		int count = 0;
		for (Mapping mapping : contentmaps) {
			if (mapping.getStatus() == MappingStatus.STARTED) count++;
		}
		return count;
	}

	public Mapping getMappingById(String mapId) {
		for (Mapping map : contentmaps) {
			if (map.getId().equals(mapId)) return map;
		}
		for (Mapping map : structmaps) {
			if (map.getId().equals(mapId)) return map;
		}
		return null;
	}

	public List<VerticalMapping> getVerticalContentMappings() {
		List<VerticalMapping> vmaps = new ArrayList<VerticalMapping>();
		for (Mapping map : contentmaps) {
			if (map instanceof VerticalMapping) {
				vmaps.add((VerticalMapping) map);
			}
		}
		// System.out.println("Returning " + vmaps.size() + " VMs: " + vmaps);
		return vmaps;
	}

	public List<VerticalMapping> getVerticalStructuralMappings() {
		List<VerticalMapping> vmaps = new ArrayList<VerticalMapping>();
		for (Mapping map : structmaps) {
			if (map instanceof VerticalMapping) {
				vmaps.add((VerticalMapping) map);
			}
		}
		return vmaps;
	}

	public List<HorizontalMapping> getHorizontalContentMappings() {
		List<HorizontalMapping> hmaps = new ArrayList<HorizontalMapping>();
		for (Mapping map : contentmaps) {
			if (map instanceof HorizontalMapping) {
				hmaps.add((HorizontalMapping) map);
			}
		}
		return hmaps;
	}

	public List<HorizontalMapping> getHorizontalStructuralMappings() {
		List<HorizontalMapping> hmaps = new ArrayList<HorizontalMapping>();
		for (Mapping map : structmaps) {
			if (map instanceof HorizontalMapping) {
				hmaps.add((HorizontalMapping) map);
			}
		}
		return hmaps;
	}

	public List<DiagonalMapping> getDiagonalContentMappings() {
		List<DiagonalMapping> dmaps = new ArrayList<DiagonalMapping>();
		for (Mapping map : contentmaps) {
			if (map instanceof DiagonalMapping) {
				dmaps.add((DiagonalMapping) map);
			}
		}
		return dmaps;
	}

	public List<DiagonalMapping> getDiagonalStructuralMappings() {
		List<DiagonalMapping> dmaps = new ArrayList<DiagonalMapping>();
		for (Mapping map : structmaps) {
			if (map instanceof DiagonalMapping) {
				dmaps.add((DiagonalMapping) map);
			}
		}
		return dmaps;
	}

	public void addContentMapping(Mapping mapping) {
		mapping.setStructural(false);
		this.contentmaps.add(mapping);
	}

	public void addStructuralMapping(Mapping mapping) {
		mapping.setStructural(true);
		this.structmaps.add(mapping);
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
	public Package getPackageById(String id) {
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

	/* Returns the Integrated Content Model (ICM). */
	public IntegratedModel getIntegratedCM() {
		for (Package pack : packages) {
			if (pack instanceof IntegratedModel && !((IntegratedModel) pack).isStructural()) {
				return (IntegratedModel) pack;
			}
		}
		return null;
	}

	/* Returns the Standard Structural Models (SSMs). */
	public List<StandardModel> getStandardSMs() {
		List<StandardModel> models = new ArrayList<StandardModel>();
		for (Package pack : packages) {
			if (pack instanceof StandardModel && ((StandardModel) pack).isStructural()) {
				models.add((StandardModel) pack);
			}
		}
		return models;
	}

	/* Returns the Integrated Structural Model (ISM). */
	public IntegratedModel getIntegratedSM() {
		for (Package pack : packages) {
			if (pack instanceof IntegratedModel && ((IntegratedModel) pack).isStructural()) {
				return (IntegratedModel) pack;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.domain + " (" + status + ")";
	}

}