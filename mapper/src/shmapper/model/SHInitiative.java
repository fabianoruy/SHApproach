package shmapper.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shmapper.model.Element.CoverateSituation;

/* Represents a Standard Harmonization Initiative. */
public class SHInitiative extends SerializableObject {
	private static final long	serialVersionUID	= 6817595375134398343L;
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
	private transient String	datafile;

	public static enum InitiativeStatus {
		INITIATED, PARSED, STRUCTURED, CONTENTED, FINISHED, CREATED
	}

	public SHInitiative(String domain) {
		this.domain = domain;
		this.packages = new ArrayList<Package>();
		this.structmaps = new ArrayList<Mapping>();
		this.contentmaps = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
		this.status = InitiativeStatus.INITIATED;
	}

	/* Resets all the initiative's packages, mappings and notions. */
	public void resetInitiative() {
		System.out.println("* INITIATIVE RESET");
		this.packages = new ArrayList<Package>();
		this.structmaps = new ArrayList<Mapping>();
		this.contentmaps = new ArrayList<Mapping>();
		this.notionMap = new HashMap<String, Notion>();
		this.status = InitiativeStatus.INITIATED;
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

	// public String getAstahPath() {
	// return astahPath;
	// }

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

	/* Returns the Current Coverage Situation of an Element in the context of the Initiative. */
	public CoverateSituation getCoverageSituation(Element elem) {
		Model model = elem.getModel();
		VerticalMapping vmapping = null;
		DiagonalMapping dmapping = null;
		// Accessing the Vertical and Diagonal Mappings of the element.
		for (Mapping map : contentmaps) {
			if (map.getBase().equals(model)) {
				if (map instanceof VerticalMapping) {
					vmapping = (VerticalMapping) map;
				} else if (map instanceof DiagonalMapping) {
					dmapping = (DiagonalMapping) map;
				}
			}
		}
		// Checking the situation on each Mapping.
		CoverateSituation situation = dmapping.getCoverageSituation(elem);
		if (situation == CoverateSituation.NONCOVERED)
			situation = vmapping.getCoverageSituation(elem);
		return situation;
	}

	/* Returns All matches of Vertical mappings of the given source Element. */
	public List<Match> getAllVerticalMatches(Element elem) {
		// Accessing the vertical mapping
		for (Mapping map : contentmaps) {
			if (map instanceof VerticalMapping && map.getBase().equals(elem.getModel())) {
				return map.getMatchesBySource(elem);
			}
		}
		return new ArrayList<Match>(); // empty list
	}

	/* Returns All matches of Diagonal mappings of the given source Element. */
	public List<Match> getAllDiagonalMatches(Element elem) {
		// Accessing the diagonal mapping
		for (Mapping map : contentmaps) {
			if (map instanceof DiagonalMapping && map.getBase().equals(elem.getModel())) {
				return map.getMatchesBySource(elem);
			}
		}
		return new ArrayList<Match>(); // empty list
	}

	/* Returns the unique simple match in the Initiative with the given source and target, if it exists. */
	public SimpleMatch getSimpleMatch(Element source, Notion target) {
		Mapping mapping = getMapping(source.getModel(), target.getPackage());
		return mapping.getSimpleMatch(source, target);
	}

	/* Returns the unique Mapping for the given base and target. */
	private Mapping getMapping(Model base, Package target) {
		// Identifying the Mapping level
		List<Mapping> mappings = (base.isStructural() ? structmaps : contentmaps);

		// Identifying direction (VM, DM, HM) of the Mapping
		if (target instanceof Ontology) {
			// Selecting the unique Vertical Mapping with the same base
			for (Mapping vmap : mappings) {
				if (vmap instanceof VerticalMapping && base.equals(vmap.getBase()))
					return vmap;
			}
		} else if (target instanceof IntegratedModel) {
			// Selecting the unique Diagonal Mapping with the same base
			for (Mapping dmap : mappings) {
				if (dmap instanceof DiagonalMapping && base.equals(dmap.getBase()))
					return dmap;
			}
		} else if (target instanceof StandardModel) {
			// Selecting the unique Horizontal Mapping with the same base and target (or vice-versa)
			for (Mapping hmap : mappings) {
				if (hmap instanceof HorizontalMapping
						&& ((base.equals(hmap.getBase()) && target.equals(hmap.getTarget())) || (base.equals(hmap.getTarget()) && target.equals(hmap.getBase()))))
					return hmap;
			}
		}
		return null;
	}

	/* Returns the Mapping with the given ID. */
	public Mapping getMappingById(String mapId) {
		for (Mapping map : contentmaps) {
			if (map.getId().equals(mapId))
				return map;
		}
		for (Mapping map : structmaps) {
			if (map.getId().equals(mapId))
				return map;
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

	/* Returns the unique Vertical Content Mapping for the given base. */
	public VerticalMapping getVerticalContentMapping(StandardModel base) {
		// Selecting the unique Vertical Mapping with the same base
		for (Mapping vmap : contentmaps) {
			if (vmap instanceof VerticalMapping && base.equals(vmap.getBase()))
				return (VerticalMapping) vmap;
		}
		return null;
	}

	/* Returns the unique Diagonal Content Mapping for the given base. */
	public DiagonalMapping getDiagonalContentMapping(StandardModel base) {
		// Selecting the unique Vertical Mapping with the same base
		for (Mapping dmap : contentmaps) {
			if (dmap instanceof DiagonalMapping && base.equals(dmap.getBase()))
				return (DiagonalMapping) dmap;
		}
		return null;
	}

	/* Returns the unique Horizontal Content Mapping for the given base and target. */
	public HorizontalMapping getHorizontalContentMapping(StandardModel base, StandardModel target) {
		// Selecting the unique Horizontal Mapping with the same base and target
		for (Mapping hmap : contentmaps) {
			if (hmap instanceof HorizontalMapping
					&& ((base.equals(hmap.getBase()) && target.equals(hmap.getTarget())) || base.equals(hmap.getTarget()) && target.equals(hmap.getBase())))
				return (HorizontalMapping) hmap;
		}
		return null;
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

	public Notion getNotionById(String id) {
		return notionMap.get(id);
	}

	public void addNotion(Notion notion) {
		this.notionMap.put(notion.getId(), notion);
	}

	/* Removes a Notion from the Initiative. */
	public void removeNotion(Notion notion) {
		this.notionMap.remove(notion.getId());
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

	/* Saves this initiative. */
	public void saveInitiative() {
		try {
			new File(datafile).getParentFile().mkdirs();
			FileOutputStream fileOut = new FileOutputStream(datafile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();

			// backup file
			String data = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date());
			String title = domain.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "");
			fileOut = new FileOutputStream(datafile.substring(0, datafile.lastIndexOf("initdata"))+ title + "." + data + ".ser");
			out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(".");
	}

	public String getDatafile() {
		return datafile;
	}

	public void setDatafile(String file) {
		this.datafile = file;
	}

}