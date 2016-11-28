package shmapper.applications;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;

import shmapper.model.Coverage;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.IntegratedModel;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative.InitiativeStatus;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/** Responsible for process the previously defined structural mapping. */
public class StructuralMappingApp {
	private SHInitiative initiative;

	public StructuralMappingApp(SHInitiative initiative) {
		this.initiative = initiative;
		// System.out.println("# StructuralMappingApp");
	}

	/* Performs the creation of all structural mappings and matches. */
	public void performStructuralMapping(String smapfile) {
		createStructuralMappings();
		populateStructuralMappings(smapfile);
		deduceHorizontalMappings();
		finishStructuralMappings();
		initiative.setStatus(InitiativeStatus.STRUCTURED);
		System.out.println("\nStructural Mappings Created (" + initiative.getStructuralMappings() + "): " + initiative.getStructuralMappings());
	}

	/* Creates the structural mappings with the predefined structural matches. */
	private void createStructuralMappings() {
		// TODO: develop a front-end for the user inform this. structuralMappings = new ArrayList<Mapping>();
		// Getting the models
		SeonView seon = initiative.getSeonView();
		IntegratedModel integrated = initiative.getIntegratedSM();
		List<StandardModel> standards = initiative.getStandardSMs();
		// One VM for Standard (Std * 1)
		for (int i = 0; i < standards.size(); i++) {
			initiative.addStructuralMapping(new VerticalMapping(standards.get(i), seon));
		}
		// One HM for each pair of Standards (Std * (Std-1))
		for (int i = 0; i < standards.size(); i++) {
			for (int j = i + 1; j < standards.size(); j++) {
				initiative.addStructuralMapping(new HorizontalMapping(standards.get(i), standards.get(j)));
			}
		}
		// One DM for Standard (Std * 1)
		for (int i = 0; i < standards.size(); i++) {
			initiative.addStructuralMapping(new DiagonalMapping(standards.get(i), integrated));
		}
	}

	/* Populates the structural mappings with the predefined structural matches (vertical and diagonal). */
	private void populateStructuralMappings(String smapfile) {
		// READING THE STRUCTURAL MAPPINGS FROM THE FILE and POPULATING THEM WITH THE DEFINED MATCHES
		System.out.println("\nReading file: " + smapfile);
		String structmaps = null;
		try {
			// Reading the file to a String
			new File(smapfile.substring(0, smapfile.lastIndexOf(File.separator))).mkdirs();
			structmaps = FileUtils.readFileToString(new File(smapfile), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringTokenizer tkzr = new StringTokenizer(structmaps.substring(1), "\n\r\t");
		String tktype = "", tkbase = "", tktarg, tksource, tkufo;
		Mapping currentMap = null;
		while (tkzr.hasMoreTokens()) {
			String token = tkzr.nextToken();
			// Identifying the Mappings and pointing to them
			if (token.equals("Mapping")) {
				tktype = tkzr.nextToken();
				tkbase = tkzr.nextToken();
				tktarg = tkzr.nextToken();
				// System.out.println(tktype + ": " + tkbase + "-->" + tktarg);
				if (tktype.equals("Vertical")) {
					for (VerticalMapping vmap : initiative.getVerticalStructuralMappings()) {
						if (vmap.getBase().getName().contains(tkbase)) {
							currentMap = vmap;
							break;
						}
					}
				} else if (tktype.equals("Diagonal")) {
					for (DiagonalMapping dmap : initiative.getDiagonalStructuralMappings()) {
						if (dmap.getBase().getName().contains(tkbase)) {
							currentMap = dmap;
							break;
						}
					}
				}
			} else {
				// Reading lines and creating the matches
				tksource = token;
				tktarg = tkzr.nextToken();
				tkufo = tkzr.nextToken();
				// System.out.println(tksource + " x " + tktarg + " (" + tkufo + ")");
				SimpleMatch match = null;
				Element source = currentMap.getBase().getElementByName(tksource);
				Notion target = null;
				if (tktype.equals("Vertical")) {
					target = ((SeonView) currentMap.getTarget()).getConceptByName(tktarg);
				} else if (tktype.equals("Diagonal")) {
					target = ((IntegratedModel) currentMap.getTarget()).getElementByName(tktarg);
					// System.out.println("Target: " + tktarg);
				}
				match = new SimpleMatch(source, target, Coverage.CORRESPONDENCE, null);
				source.setUfotype(UFOType.valueOf(tkufo));
				target.setUfotype(UFOType.valueOf(tkufo));
				currentMap.addMatch(match);
			}
		}
	}

	/* Deduces the horizontal structural mappings from the vertical and diagonal ones. */
	private void deduceHorizontalMappings() {
		for (HorizontalMapping hmap : initiative.getHorizontalStructuralMappings()) {
			// Select the VERTICAL Mapping with the same HM base
			List<VerticalMapping> vmappings = initiative.getVerticalStructuralMappings();
			// For the VM1 with the same base of HM
			for (VerticalMapping vmap1 : vmappings) {
				if (vmap1.getBase().equals(hmap.getBase())) {
					// For the VM2 with the base equals the target of HM
					for (VerticalMapping vmap2 : vmappings) {
						if (vmap2.getBase().equals(hmap.getTarget())) {
							// For each VM1 match, get the source
							for (Match bmatch : vmap1.getMatches()) {
								Element source = bmatch.getSource();
								// And search for the matches with the same target in VM2
								for (Match tmatch : vmap2.getSimpleMatchesByTarget(((SimpleMatch) bmatch).getTarget())) {
									// Create a match in HM with source (from MV1 source) and target (from VM2 source).
									hmap.addMatch(new SimpleMatch(source, tmatch.getSource(), Coverage.CORRESPONDENCE, null));
								}
							}
							break;
						}
					}
					break;
				}
			}
			// Select the DIAGONAL Mapping with the same HM base (and do as made for VERTICAL)
			List<DiagonalMapping> dmappings = initiative.getDiagonalStructuralMappings();
			for (DiagonalMapping dmap1 : dmappings) {
				if (dmap1.getBase().equals(hmap.getBase())) {
					for (DiagonalMapping dmap2 : dmappings) {
						if (dmap2.getBase().equals(hmap.getTarget())) {
							for (Match bmatch : dmap1.getMatches()) {
								Element source = bmatch.getSource();
								for (Match tmatch : dmap2.getSimpleMatchesByTarget(((SimpleMatch) bmatch).getTarget())) {
									hmap.addMatch(new SimpleMatch(source, tmatch.getSource(), Coverage.CORRESPONDENCE, null));
								}
							}
							break;
						}
					}
					break;
				}
			}
		}
	}

	/* Finishes the structural mappings. */
	private void finishStructuralMappings() {
		for (Mapping smapping : initiative.getStructuralMappings()) {
			smapping.finishMapping();
			// System.out.println("\nStructural Mappings: " + smapping + " (" + smapping.getMatches().size() + ")");
			// for (Match match : smapping.getMatches()) {
			// System.out.println("- " + match);
			// }
		}
	}

}