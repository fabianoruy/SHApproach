package shmapper.applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.LicenseNotFoundException;
import com.change_vision.jude.api.inf.exception.NonCompatibleException;
import com.change_vision.jude.api.inf.exception.ProjectLockedException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import shmapper.model.Concept;
import shmapper.model.Diagram;
import shmapper.model.Diagram.DiagramType;
import shmapper.model.Element;
import shmapper.model.IntegratedModel;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.Ontology;
import shmapper.model.Ontology.Level;
import shmapper.model.Package;
import shmapper.model.Relation;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.StandardModel;

/** Responsible for parsing the provided Astah file, creating the objects' model. */
public class AstahParseApp {
	private SHInitiative		initiative;
	private String				astahPath;
	private StringBuffer		parsingResults	= new StringBuffer();
	private Map<String, IClass>	astahClassMap	= new HashMap<String, IClass>();
	private static String		winPath			= '"' + "C:/Program Files/astah-professional/astah-commandw.exe" + '"';
	private static String		linuxPath		= "/var/lib/tomcat7/astah/astah_professional/astah-command.sh";
	private static String		astahCommandPath;

	static {
		String os = System.getProperty("os.name");
		System.out.println("* SO: " + os);
		if (os.contains("Linux")) astahCommandPath = linuxPath;
		else if (os.contains("Windows")) astahCommandPath = winPath;
	}

	public AstahParseApp(SHInitiative initiative) {
		this.initiative = initiative;
	}

	// public SHInitiative getInitiative() {
	// return this.initiative;
	// }

	/* Reads an Astah file for parsing the models (1). */
	public void parseAstah(String filename) throws ParserException {
		System.out.println("Astah: " + filename);
		this.astahPath = filename;
		ProjectAccessor accessor = null;
		try {
			// Accessing the astah model
			accessor = AstahAPI.getAstahAPI().getProjectAccessor();
			// Opening a project (name, true not to check model version, false not to lock a project file,
			// true to open a project file with the read only mode if the file is locked.)
			accessor.open(filename, true, false, true);
			IModel model = accessor.getProject();
			addResult("Astah Model accessed.\n");

			// Reading the model Packages (Ontologies and Models) and Notions (Concepts and Elements)
			parseAstahModel(model);

		} catch (IOException | ClassNotFoundException | LicenseNotFoundException | ProjectNotFoundException | NonCompatibleException | ProjectLockedException e) {
			e.printStackTrace();
		} finally {
			accessor.close();
		}
	}

	//////////////////// PARSING MODELS ////////////////////

	/* Reads the packages and creates the Ontologies and Standards Models. */
	private void parseAstahModel(IModel model) throws ParserException {
		// Get the Structural and Initiative packages.
		IPackage ssmpack = getPackage(model, "Standards Structural Models");
		IPackage initpack = getPackage(model, "Initiative");
		// Get the Domain package (the single subpackage of the "Initiative" package)
		IPackage domainpack = getPackage(initpack, initpack.getOwnedElements()[0].getName());
		// Get the SEON View package (1)
		IPackage seonpack = getPackage(domainpack, "1.SEON View");
		// Get the ISM package (2)
		IPackage ismpack = getPackage(domainpack, "2.Structure");
		// Get the Standards Content Models (SCMs) package (3)
		IPackage contentpack = getPackage(domainpack, "3.Content");

		// Creating the Packages for the Initiative
		initiative.setAstahPath(astahPath);

		// Parsing the ontologies (SEON View)
		parseOntologies(seonpack);

		// Parsing the standards structural models (SSMs and ISM)
		parseStructuralModels(ssmpack, ismpack);

		// Parsing the standards content models (SCMs and ICM)
		parseContentModels(contentpack);

		addResult("\n" + initiative.getAllNotions().size() + " concepts and elements parsed.\n");

		// Reading the model Relations and Generalizations
		parseRelations(initiative.getAllNotions());
		parseGeneralizations(initiative.getAllNotions());
		int noBTs = checkBasetypes(initiative.getAllNotions());
		if (noBTs > 0) {
			addResult("<b>There are " + noBTs + " classes without a basetype. Fixing some of them allows a better support during the Mappings.<b>\n\n");
		}
	}

	/* Reads the packages and creates the Ontologies and Standards Models. */
	private IPackage getPackage(IPackage superpack, String packname) throws ParserException {
		IPackage pack = null;
		for (INamedElement node : superpack.getOwnedElements()) {
			if (node instanceof IPackage && node.getName().equals(packname)) {
				pack = (IPackage) node;
				addResult("Package found: " + packname + "\n");
				return pack;
			}
		}
		throw new ParserException("Package not found: " + packname + ".\n");
	}

	/* Reads the SEON View package and creates the Ontologies. */
	private void parseOntologies(IPackage seonpack) throws ParserException {
		// Creates the SEON View
		SeonView seonview = new SeonView(seonpack);
		initiative.addPackage(seonview);
		addResult("\nSEON View created. ");

		// Get the Ontologies' packages and creates the Ontologies.
		addResult("Ontologies:\n");
		for (INamedElement node : seonpack.getOwnedElements()) {
			if (node instanceof IPackage) {
				IPackage levelpack = (IPackage) node;
				Level level = null;
				if (levelpack.getName().contains("Domain Level")) level = Level.DOMAIN;
				else if (levelpack.getName().contains("Core Level")) level = Level.CORE;
				else if (levelpack.getName().contains("Foundational Level")) level = Level.FOUNDATIONAL;
				for (INamedElement pack : levelpack.getOwnedElements()) {
					if (pack instanceof IPackage) {
						IPackage ontopack = (IPackage) pack;
						Ontology onto = new Ontology(level, ontopack);
						seonview.addOntology(onto);
						initiative.addPackage(onto);
						addResult(" - " + onto + "\n");
						parseConcepts(onto, ontopack);
					}
				}
			}
		}
		// Parse the SEON View Diagram
		Diagram diagram = parseDiagram(seonpack, DiagramType.SEONVIEW);
		seonview.setDiagram(diagram);
	}

	/* Reads the structural models packages and creates the SSMs. */
	private void parseStructuralModels(IPackage ssmpack, IPackage ismpack) throws ParserException {
		// Creates the Standards Structure Models (SSMs) and their elements.
		addResult("\nStandards' Structural Models created: \n");
		for (INamedElement node : ssmpack.getOwnedElements()) {
			if (node instanceof IPackage) {
				IPackage stdpack = (IPackage) node;
				StandardModel stdmodel = new StandardModel(true, stdpack);
				// stdmodel.setName(stdmodel.getName() + " SM");
				stdmodel.setName(stdmodel.getName());
				initiative.addPackage(stdmodel);
				addResult(" - " + stdmodel + "\n");
				parseElements(stdmodel, stdpack);
			}
		}

		// Creates the Integrated Structural Model (ISM) and its (specific) elements.
		IntegratedModel ism = new IntegratedModel(true, ismpack);
		ism.setName("Integrated Structural Model");
		initiative.addPackage(ism);
		addResult(" * Integrated SM created.\n\n");
		parseIMElements(ism, ismpack);

	}

	/* Reads the content model package and creates the SCMs. */
	private void parseContentModels(IPackage contentpack) throws ParserException {
		// Creates the Standards Content Models (SCMs) and their element.
		addResult("Standards' Content Models created: \n");
		for (INamedElement node : contentpack.getOwnedElements()) {
			if (node instanceof IPackage) {
				IPackage stdpack = (IPackage) node;
				StandardModel stdmodel = new StandardModel(false, stdpack);
				// stdmodel.setName(stdmodel.getName() + " CM");
				stdmodel.setName(stdmodel.getName());
				initiative.addPackage(stdmodel);
				// System.out.print(stdmodel.getId());
				addResult(" - " + stdmodel + "\n");

				// Parse the SCM Diagrams and elements.
				parseElements(stdmodel, stdpack);
				Diagram diagram = parseDiagram(stdpack, DiagramType.SCM);
				stdmodel.setDiagram(diagram);
			}
		}

		// Creates the Integrated Content Model (ICM) and its (specific) elements.
		IntegratedModel icm = new IntegratedModel(false, contentpack);
		icm.setName("Integrated Content Model");
		initiative.addPackage(icm);
		addResult(" * Integrated CM created.\n");

		// Parse the ICM Diagram and elements
		parseIMElements(icm, contentpack);
		Diagram diagram = parseDiagram(contentpack, DiagramType.ICM);
		icm.setDiagram(diagram);

	}

	/* Reads the classes of an Ontology package and creates the Concepts. */
	private void parseConcepts(Ontology onto, IPackage pack) {
		try {
			for (INamedElement node : pack.getOwnedElements()) {
				// Parsing classes and creating Concepts
				if (node instanceof IClass) {
					if (node.getPresentations().length > 0) { // selects only visible classes (appear in some diagram)
						Concept concept = new Concept(onto, (IClass) node);
						onto.addConcept(concept);
						initiative.addNotion(concept);
						astahClassMap.put(node.getId(), (IClass) node);
						// addResult(" . " + concept + "\n");
					} else {
						System.out.println("Discarded Concept: " + node.getName());
					}
				}
				// Recursivelly parsing packages
				else if (node instanceof IPackage) {
					parseConcepts(onto, (IPackage) node);
				}
			}
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
	}

	/* Reads the classes of a Standard Model package and creates the Elements. */
	private void parseElements(StandardModel model, IPackage pack) {
		try {
			for (INamedElement node : pack.getOwnedElements()) {
				// Parsing classes and creating Elements
				if (node instanceof IClass) {
					if (node.getPresentations().length > 0) { // selects only visible classes (appear in some diagram)
						Element element = new Element(model, (IClass) node);
						model.addElement(element);
						initiative.addNotion(element);
						astahClassMap.put(node.getId(), (IClass) node);
					} else {
						System.out.println("Discarded Element: " + node.getName());
					}
				}
				// Recursivelly parsing packages
				else if (node instanceof IPackage) {
					parseElements(model, (IPackage) node);
				}
			}
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
	}

	/* Reads the classes of a Standard Model package and creates the Elements. */
	private void parseIMElements(IntegratedModel im, IPackage pack) throws ParserException {
		int ecount = 0;
		try {
			for (INamedElement node : pack.getOwnedElements()) {
				// Parsing classes and creating Elements
				if (node instanceof IClass) {
					if (node.getPresentations().length > 0) { // selects only visible classes (appear in some diagram)
						Element element = new Element(im, (IClass) node);
						im.addElement(element);
						initiative.addNotion(element);
						astahClassMap.put(node.getId(), (IClass) node);
						ecount++;
					} else {
						System.out.println("Discarded Element: " + node.getName());
					}
				}
			}
			if(ecount == 0 && im.isStructural()) {
				throw new ParserException("No elements were found in the Integrated Structural Model");
			}
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
	}

	/* Reads and creates the Relations between the Notions. */
	private void parseRelations(List<Notion> notions) {
		int rcount = 0;
		for (Notion source : notions) {
			// Reading and creating relations
			for (IAttribute attrib : astahClassMap.get(source.getId()).getAttributes()) {
				IAssociation assoc = attrib.getAssociation();
				if (assoc != null) { // it is an Association, not an Attribute
					IClass asource = assoc.getMemberEnds()[0].getType();
					// Selecting only the relations where this concept is source (not target).
					if (asource.equals(astahClassMap.get(source.getId()))) {
						Notion target = initiative.getNotionById(attrib.getType().getId());
						// Creating the Relation object
						if (target != null) {
							Relation relation = new Relation(source, target, assoc);
							source.addRelation(relation);
							target.addRelation(relation);
							rcount++;
						}
					}
				} else {
					// It is an attribute
					// System.out.println("# Attribute: " + attrib.getName());
				}
			}
		}
		addResult(rcount + " relations parsed.\n");
	}

	/* Reads and sets the generalizations of each notion (Concepts and Elements). */
	private void parseGeneralizations(List<Notion> notions) {
		int gcount = 0;
		for (Notion child : notions) {
			// Reading and setting generalizations
			for (IGeneralization node : astahClassMap.get(child.getId()).getGeneralizations()) {
				Notion parent = initiative.getNotionById(node.getSuperType().getId());
				if (parent != null) {
					child.addGeneralization(parent);
					// System.out.println(child + " --> " + parent);
					gcount++;
				}
			}
		}
		addResult(gcount + " generalizations set.\n");
	}

	/* Checks if there is any notion without a basetype. */
	private int checkBasetypes(List<Notion> allNotions) {
		int count = 0;
		for (Notion notion : allNotions) {
			if (!notion.isBasetype()) {
				List<Notion> basetypes = notion.getBasetypes();
				if (basetypes.isEmpty()) {
					if (notion instanceof Concept) {
						addResult("The concept " + notion + " (package " + notion.getPackage() + ") has no generalization to a Core/Foundational Ontology Concept.\n");
					} else if (notion instanceof Element) {
						addResult("The element " + notion + " (package " + notion.getPackage() + ") has no generalization to a Structural Model Element.\n");
					}
					count++;
				}
			}
		}
		return count;
	}

	// if (!basetype.equals(notion)) {
	// if (basetype instanceof Element) {
	// Model model = ((Element) basetype).getModel();
	// if (!model.isStructural()) {
	// addResult("The element " + notion + " has no generalization to a Structural Model Element.\n");
	// System.out.println("BT: "+ basetype);
	// count++;
	// }
	// } else if (basetype instanceof Concept) {
	// Ontology onto = ((Concept) basetype).getOntology();
	// if (onto.getLevel() != Level.CORE && onto.getLevel() != Level.FOUNDATIONAL) {
	// addResult("The concept " + notion + " has no generalization to a Core/Foundational Ontology Concept.\n");
	// System.out.println("BT: "+ ((Concept)basetype).getOntology().getLevel());
	// count++;
	// }
	// }

	/* Reads and creates an astah Diagrams from a package. */
	private Diagram parseDiagram(IPackage pack, DiagramType type) throws ParserException {
		IDiagram[] diagrams = pack.getDiagrams();
		if (diagrams.length != 1) {
			throw new ParserException("A single diagram is expected in Package " + pack.getName() + ". It has " + diagrams.length + ".\n");
		}
		for (IDiagram diag : pack.getDiagrams()) {
			// Creating the diagram and getting its path
			Diagram diagram = new Diagram(type, diag);
			// System.out.println("AstahPath: "+ astahPath);
			String filename = astahPath.substring(astahPath.indexOf("uploaded_"), astahPath.indexOf(".asta"));
			String initdir = astahPath.substring(astahPath.indexOf("mapper")-1, astahPath.indexOf("uploaded_"));
			String path = initdir + "images/" + filename + File.separator + diag.getFullName(File.separator) + ".png";
			diagram.setPath(path.replace("\\", "/"));

			try {
				// Collecting the notions positions.
				for (IPresentation present : diag.getPresentations()) {
					if (present instanceof INodePresentation && present.getType().equals("Class")) {
						INodePresentation pnode = (INodePresentation) present;
						Notion notion = initiative.getNotionById(pnode.getModel().getId());
						NotionPosition position = new NotionPosition(notion, pnode, diag.getBoundRect());
						diagram.addPosition(position);
					}
				}
			} catch (InvalidUsingException e) {
				e.printStackTrace();
			}
			System.out.println("   . " + diagram + " (diagram)");
			return diagram; // only one diagram per package, in this case.
		}
		throw new ParserException("Diagram not found in package " + pack.getName() + ".\n");
	}

	//////////////////// IMPORTING IMAGES ////////////////////

	/* Imports the astah PNG images (from astah file) to the images directory. */
	public void importImages(String astahFile, String workingDir) throws ParserException {
		String targetPath = workingDir + "images";
		try {
			File dir = new File(targetPath);
			// if (!dir.exists()) dir.mkdirs();
			// TODO: remove
			System.out.print("\nWho am I? ");
			System.out.flush();
			Process process = Runtime.getRuntime().exec("whoami");
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s = null;
			while ((s = input.readLine()) != null) {
				System.out.println(s);
			}
			input.close();

			// Exporting images from the Astah file (using command line).
			System.out.println("\n* Exporting images from Astah");
			String command = astahCommandPath; // command for exporting
			command += " -image cl"; // selecting only Class diagrams
			command += " -f " + astahFile; // defining input astah file
			command += " -o " + targetPath; // defining output directory
			System.out.println("$ " + command);
			System.out.flush();

			long start = System.currentTimeMillis();
			process = Runtime.getRuntime().exec(command); // Executing command

			// TODO: remove
			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = input.readLine()) != null) {
				System.out.println(s);
			}
			input.close();
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((s = error.readLine()) != null) {
				System.out.println(s);
			}
			error.close();

			process.waitFor();
			System.out.print("[ -] Time: " + (System.currentTimeMillis() - start) + " - ");

			// TODO: test images exporting in other machines/conditions.
			// Waiting for all files being copied.
			int files = 0;
			int before = 0;
			int diff = 0;
			while (files == 0 || diff > 0) {
				waitFor(3, 1000);
				files = FileUtils.listFiles(dir, new String[] { "png" }, true).size();
				diff = files - before;
				before = files;
				System.out.print("[" + files + "] Time: " + (System.currentTimeMillis() - start) + " - ");
			}

			// Counting the identified diagrams' paths
			int dcount = 0;
			for (Package pack : initiative.getAllPackages()) {
				if (pack.getDiagram() != null) {
					dcount++;
				}
			}
			addResult(dcount + " diagrams imported.\n");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParserException("Failed during astah images importing/copying.");
		}
	}

	/* Imports the astah PNG images (from astah file) to the images directory. */
//	public void importImages2(String astahFile, String workingDir) throws ParserException {
//		String targetPath = workingDir + "images/";
//		File dir = new File(targetPath);
//		if (!dir.exists()) dir.mkdirs();
//		try {
//			// TODO: remove
//			System.out.println("Who am I?");
//			ProcessBuilder pb = new ProcessBuilder("whoami");
//			pb.redirectOutput(Redirect.INHERIT);
//			pb.redirectError(Redirect.INHERIT);
//			Process p = pb.start();
//
//			// Exporting images from the Astah file (using command line).
//			System.out.println("\n# Exporting images from Astah");
//			String command = astahCommandPath; // command for exporting
//			command += " -image cl"; // selecting only Class diagrams
//			command += " -f " + astahFile; // defining input astah file
//			command += " -o " + targetPath; // defining output directory
//			System.out.println("$ " + command);
//
//			long start = System.currentTimeMillis();
//			// process = Runtime.getRuntime().exec(command); // Executing command
//			pb = new ProcessBuilder(command);
//			pb.redirectOutput(Redirect.INHERIT);
//			pb.redirectError(Redirect.INHERIT);
//			p = pb.start();
//			p.waitFor();
//
//			// process.waitFor();
//			System.out.print("[ -] Time: " + (System.currentTimeMillis() - start) + " - ");
//
//			// TODO: test images exporting in other machines/conditions.
//			// Waiting for all files being copied.
//			int files = 0;
//			int before = 0;
//			int diff = 0;
//			while (files == 0 || diff > 0) {
//				waitFor(3, 1000);
//				files = FileUtils.listFiles(dir, new String[] { "png" }, true).size();
//				diff = files - before;
//				before = files;
//				System.out.print("[" + files + "] Time: " + (System.currentTimeMillis() - start) + " - ");
//			}
//
//			// Counting the identified diagrams' paths
//			int dcount = 0;
//			for (Package pack : initiative.getAllPackages()) {
//				if (pack.getDiagram() != null) {
//					dcount++;
//				}
//			}
//			addResult(dcount + " diagrams imported.\n");
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new ParserException("Failed during astah images importing/copying.");
//		}
//	}

	/* Waits for a period (millis) a number of times (times). */
	private void waitFor(int times, long millis) {
		System.out.print("Waiting (" + times + "*" + millis + ") ");
		try {
			for (int i = 0; i < times; i++) {
				Thread.sleep(millis); // 1000 milliseconds is one second.
				System.out.print(".");
			}
			System.out.println("");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//////////////////// MANAGING RESULTS ////////////////////

	/* Adds a result to be returned to the page. */
	private void addResult(String result) {
		System.out.print(result);
		this.parsingResults.append(result.replaceAll("\n", "<br/>"));
	}

	/* Gets the results of the parsing tasks. */
	public String getResults() {
		String results = parsingResults.toString();
		parsingResults = new StringBuffer();
		return results;
	}

}