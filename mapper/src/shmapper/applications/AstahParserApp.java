package shmapper.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.change_vision.jude.api.inf.AstahAPI;
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
import com.change_vision.jude.api.inf.model.IMultiplicityRange;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import shmapper.model.Concept;
import shmapper.model.Diagram;
import shmapper.model.Package;
import shmapper.model.Diagram.DiagramType;
import shmapper.model.Element;
import shmapper.model.IntegratedModel;
import shmapper.model.Notion;
import shmapper.model.Ontology;
import shmapper.model.Ontology.Level;
import shmapper.model.Relation;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.StandardModel;

/** Responsible for parsing the provided Astah file, creating the objects' model. */
public class AstahParserApp {
    private SHInitiative initiative;
    private StringBuffer parsingResults = new StringBuffer();
    //private String workingDir = System.getProperty("user.dir").replace('\\', '/');
    private static String astahInstallPath = "C:/Program Files/astah-professional"; //TODO: get the real path

    public SHInitiative getInitiative() {
	return this.initiative;
    }

    /* Reads an Astah file and parses the models. */
    public void parseAstah(String filename) {
	ProjectAccessor accessor = null;
	try {
	    // Accessing the astah model
	    accessor = AstahAPI.getAstahAPI().getProjectAccessor();
	    System.out.println("before open");
	    // Opening a project (name, true not to check model version, false not to lock a project file,
	    // true to open a project file with the read only mode if the file is locked.)
	    accessor.open(filename, true, false, true);
	    //	    accessor.open(filename);
	    System.out.println("after open");
	    IModel model = accessor.getProject();
	    addResult("Astah Model accessed.\n");

	    // Reading the model Packages (Ontologies and Models) and Notions (Concepts and Elements)
	    parsePackages(model);

	    // Reading the model Relations
	    parseRelations(initiative.getAllNotions());

	    // Reading the model Generalizations
	    parseGeneralizations(initiative.getAllNotions());

	} catch (IllegalStateException e) {
	    //TODO: deal with that
	    System.out.println("!Exception! " + e.getMessage());
	} catch (IOException | ClassNotFoundException | LicenseNotFoundException | ProjectNotFoundException
		| NonCompatibleException | ProjectLockedException e) {
	    System.out.println("!Exception! " + e.getMessage());
	    e.printStackTrace();
	} finally {
	    accessor.close();
	}

	addResult("\n<b>Astah File successfully read and parsed! Please, proceed to the Mapping.<b>");
    }

    /* Reads the packages and creates the Ontologies and Standards Models. */
    private void parsePackages(IModel model) {
	// Get the Structural and Initiative packages.
	IPackage ssmpack = null;
	IPackage initpack = null;
	for (INamedElement node : model.getOwnedElements()) {
	    if (node instanceof IPackage && node.getName().equals("Standards Structural Models")) {
		ssmpack = (IPackage) node;
		addResult("Standards Structural Models package found.\n");
	    }
	    if (node instanceof IPackage && node.getName().equals("Initiative")) {
		initpack = (IPackage) node;
		addResult("Initiative package found.\n");
	    }
	}
	// Get the Domain package (the single subpackage of the "Initiative" package)
	IPackage domainpack = null;
	INamedElement domainnode = initpack.getOwnedElements()[0];
	if (domainnode instanceof IPackage) {
	    domainpack = (IPackage) domainnode;
	    addResult("Domain package found: " + domainpack.getName() + "\n");
	}

	// Get the SEON View package (1)
	IPackage seonpack = null;
	for (INamedElement node : domainpack.getOwnedElements()) {
	    if (node instanceof IPackage && node.getName().equals("1.SEON View")) {
		seonpack = (IPackage) node;
		addResult("SEON View found.\n");
		break;
	    }
	}
	// Get the ISM package (2)
	IPackage ismpack = null;
	for (INamedElement node : domainpack.getOwnedElements()) {
	    if (node instanceof IPackage && node.getName().equals("2.Structure")) {
		ismpack = (IPackage) node;
		addResult("ISM package found.\n");
		break;
	    }
	}
	// Get the Standards Content Models (SCMs) package (3)
	IPackage contentpack = null;
	for (INamedElement node : domainpack.getOwnedElements()) {
	    if (node instanceof IPackage && node.getName().equals("3.Content")) {
		contentpack = (IPackage) node;
		break;
	    }
	}

	// Creating the SH Initiative
	initiative = new SHInitiative(domainpack.getName(), null, null);

	// Parsing the standards structural models (SSMs and ISM)
	parseStructuralModels(ssmpack, ismpack);

	// Parsing the standards content models (SCMs and ICM)
	parseContentModels(contentpack);

	// Parsing the ontologies (SEON View)
	parseOntologies(seonpack);

	addResult(initiative.getAllNotions().size() + " concepts and elements parsed.\n");
    }

    /* Reads the structural models packages and creates the SSMs. */
    private void parseStructuralModels(IPackage ssmpack, IPackage ismpack) {
	// Creates the Standards Structure Models (SSMs) and their elements.
	addResult("\nStandards' Structural Models created: \n");
	for (INamedElement node : ssmpack.getOwnedElements()) {
	    if (node instanceof IPackage) {
		IPackage stdpack = (IPackage) node;
		StandardModel stdmodel = new StandardModel(true, stdpack);
		stdmodel.setName(stdmodel.getName() + " SM");
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
    private void parseContentModels(IPackage contentpack) {
	// Creates the Standards Content Models (SCMs) and their element.
	addResult("Standards' Content Models created: \n");
	for (INamedElement node : contentpack.getOwnedElements()) {
	    if (node instanceof IPackage) {
		IPackage stdpack = (IPackage) node;
		StandardModel stdmodel = new StandardModel(false, stdpack);
		stdmodel.setName(stdmodel.getName() + " CM");
		initiative.addPackage(stdmodel);
		//System.out.print(stdmodel.getId());
		addResult(" - " + stdmodel + "\n");

		// Parse the SCM Diagrams and elements.
		parseDiagram(stdmodel, DiagramType.SCM);
		parseElements(stdmodel, stdpack);
	    }
	}

	// Creates the Integrated Content Model (ICM) and its (specific) elements.
	IntegratedModel icm = new IntegratedModel(false, contentpack);
	icm.setName("Integrated Content Model");
	initiative.addPackage(icm);
	addResult(" * Integrated CM created.\n\n");

	// Parse the ICM Diagram and elements
	parseDiagram(icm, DiagramType.ICM);
	parseIMElements(icm, contentpack);

    }

    /* Reads the SEON View package and creates the Ontologies. */
    private void parseOntologies(IPackage seonpack) {
	// Creates the SEON View and its ontologies.
	SeonView seonview = new SeonView(seonpack);
	initiative.addPackage(seonview);
	addResult("SEON View created. ");

	// Parse the SEON View Diagram
	parseDiagram(seonview, DiagramType.SEONVIEW);

	// Get the Ontologies' packages and creates the Ontologies.
	addResult("Ontologies:\n");
	for (INamedElement node : seonpack.getOwnedElements()) {
	    if (node instanceof IPackage) {
		IPackage levelpack = (IPackage) node;
		Level level = null;
		if (levelpack.getName().contains("Domain Level"))
		    level = Level.DOMAIN;
		else if (levelpack.getName().contains("Core Level"))
		    level = Level.CORE;
		else if (levelpack.getName().contains("Foundational Level"))
		    level = Level.FOUNDATIONAL;
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
	addResult("\n");
    }

    /* Reads the classes of an Ontology package and creates the Concepts. */
    private void parseConcepts(Ontology onto, IPackage pack) {
	for (INamedElement node : pack.getOwnedElements()) {
	    // Parsing classes and creating Concepts
	    if (node instanceof IClass) {
		Concept concept = new Concept(onto, (IClass) node);
		onto.addConcept(concept);
		initiative.addNotion(concept);
		// addResult(" . " + concept + "\n");
	    }
	    // Recursivelly parsing packages
	    else if (node instanceof IPackage) {
		parseConcepts(onto, (IPackage) node);
	    }
	}
    }

    /* Reads the classes of a Standard Model package and creates the Elements. */
    private void parseElements(StandardModel model, IPackage pack) {
	for (INamedElement node : pack.getOwnedElements()) {
	    // Parsing classes and creating Elements
	    if (node instanceof IClass) {
		Element element = new Element(model, (IClass) node);
		model.addElement(element);
		initiative.addNotion(element);
		// addResult(" . " + element + "\n");
	    }
	    // Recursivelly parsing packages
	    else if (node instanceof IPackage) {
		parseElements(model, (IPackage) node);
	    }
	}
    }

    /* Reads the classes of a Standard Model package and creates the Elements. */
    private void parseIMElements(IntegratedModel im, IPackage pack) {
	for (INamedElement node : pack.getOwnedElements()) {
	    // Parsing classes and creating Elements
	    if (node instanceof IClass) {
		Element element = new Element(null, (IClass) node);
		im.addElement(element);
		initiative.addNotion(element);
		// addResult(" . " + element + "\n");
	    }
	}
    }

    /* Reads and sets the generalizations of each notion (Concepts and Elements). */
    private void parseGeneralizations(List<Notion> notions) {
	int gcount = 0;
	for (Notion child : notions) {
	    // Reading and setting generalizations
	    for (IGeneralization node : child.getAstahClass().getGeneralizations()) {
		Notion parent = initiative.getNotionById(node.getSuperType().getId());
		child.addGeneralization(parent);
		gcount++;
	    }
	}
	addResult(gcount + " generalizations set.\n");
    }

    /* Reads and creates the Relations between the Notions. */
    private void parseRelations(List<Notion> notions) {
	int rcount = 0;
	for (Notion source : notions) {
	    // Reading and creating relations
	    for (IAttribute attrib : source.getAstahClass().getAttributes()) {
		IAssociation assoc = attrib.getAssociation();
		if (assoc != null) { // it is an Association, not an Attribute
		    IAttribute firstEnd = assoc.getMemberEnds()[0];
		    IAttribute secondEnd = assoc.getMemberEnds()[1];
		    // Selecting only the relations where this concept is source (not target).
		    if (firstEnd.getType().equals(source.getAstahClass())) {
			String name = assoc.getName();
			String def = assoc.getDefinition();
			String ster = null;
			if (assoc.getStereotypes().length > 0) {
			    ster = assoc.getStereotypes()[0]; // only the first for while
			}
			boolean composition = (firstEnd.isComposite() || firstEnd.isAggregate());
			String smult = "";
			String tmult = "";
			if (firstEnd.getMultiplicity().length > 0) {
			    smult = multiplicityToString(firstEnd.getMultiplicity()[0]);
			}
			if (secondEnd.getMultiplicity().length > 0) {
			    tmult = multiplicityToString(secondEnd.getMultiplicity()[0]);
			}

			Notion target = initiative.getNotionById(attrib.getType().getId());
			// System.out.println(source + " [S] " + name + " [T] " + attrib.getType().toString());
			// Creating the Relation object
			Relation relation = new Relation(name, def, ster, composition, source, target, smult, tmult);
			source.addRelation(relation);
			target.addRelation(relation);
			rcount++;
		    }
		} else {
		    // It is an attribute
		    // System.out.println("# Attribute: " + attrib.getName());
		}
	    }
	}
	addResult(rcount + " relations parsed.\n");
    }

    /* Returns the multiplicity of an end in text format (n..m). */
    private String multiplicityToString(IMultiplicityRange imult) {
	int lower = imult.getLower();
	int upper = imult.getUpper();
	if (lower == IMultiplicityRange.UNDEFINED)
	    return "";
	if (lower == IMultiplicityRange.UNLIMITED)
	    return "*";
	if (upper == IMultiplicityRange.UNDEFINED)
	    return lower + "";
	if (upper == IMultiplicityRange.UNLIMITED)
	    return lower + "..*";
	return lower + ".." + upper;
    }

    /* Reads and creates an astah Diagrams from a package. */
    private void parseDiagram(Package pack, DiagramType type) {
	for (IDiagram node : pack.getAstahPack().getDiagrams()) {
	    String name = node.getName();
	    String desc = node.getDefinition();
	    Diagram diagram = new Diagram(name, desc, type, node);
	    pack.setDiagram(diagram);
	    //System.out.println("Diagram: " + diagram);
	    return; // only one diagram for each package, in this case.
	}
    }

    /* Imports the astah PNG images (from astah file) to the images directory. */
    public void importImages(String astahFile, String workingDir) {
	String targetPath = workingDir + "images/tmp/";
	File dir = new File(targetPath);
	if (!dir.exists())
	    dir.mkdirs();
	try {
	    // Exporting images from the Astah file (using command line).
	    System.out.println("\n# Exporting images from Astah to " + targetPath);
	    String command = '"' + astahInstallPath + "/astah-commandw.exe" + '"'; // command for exporting
	    command += " -image cl"; // selecting only Class diagrams
	    command += " -f " + astahFile; // defining input astah file
	    command += " -o " + targetPath; // defining output directory
	    System.out.println("$ " + command);

	    long start = System.currentTimeMillis();
	    Process process = Runtime.getRuntime().exec(command);
	    process.waitFor();
	    System.out.print("[-] Time: " + (System.currentTimeMillis() - start) + " - ");

	    // Getting the identified diagrams' paths
	    List<String> identDiagrams = new ArrayList<String>();
	    for (Package pack : initiative.getAllPackages()) {
		Diagram diagram = pack.getDiagram();
		if (diagram != null) {
		    String relativePath = "/" + diagram.getAstahDiagram().getFullName("/") + ".png";
		    identDiagrams.add(relativePath);
		    //System.out.println("\n#ID: " + relativePath);
		}
	    }

	    // TODO: test images exportation in other machines/conditions.
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

	    // Copying the .PNG files (of the identified diagrams) from the tmp directory to the images directory
	    String target = workingDir + "images/";
	    int count = 0;
	    System.out.println("\nCopying the .PNG files from " + dir.getPath() + " and subdirectories to " + target);
	    List<File> allFiles = (List<File>) FileUtils.listFiles(dir, new String[] { "png" }, true);
	    for (File file : allFiles) {
		String path = file.getAbsolutePath();
		String relativePath = path.substring(path.indexOf('\\', path.indexOf("Uploaded_"))).replace('\\', '/');
		//System.out.println("#FILE: " + relativePath);
		if (identDiagrams.contains(relativePath)) {
		    File dest = new File(target + file.getName());
		    FileUtils.copyFile(file, dest); // copies each PNG file
		    System.out.print(++count + " ");
		    System.out.println(dest);
		}
	    }
	    addResult(count + " diagrams imported.\n");
	    System.out.println("");

	    // Scheduling the Deletion of temporary astahdoc images directory
	    System.out.println("Deleting " + dir.getName());
	    FileUtils.forceDeleteOnExit(dir);
	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
	}
    }

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

    /* Adds a result to be returned to the page. */
    private void addResult(String result) {
	System.out.print(result);
	this.parsingResults.append(result.replaceAll("\n", "<br/>"));
    }

    /* Gets the results of the parsing tasks. */
    public String getResults() {
	return parsingResults.toString();
    }
}