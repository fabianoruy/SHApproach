package shmapper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shmapper.model.DiagonalMapping;
import shmapper.model.HorizontalMapping;
import shmapper.model.Mapping;
import shmapper.model.Notion;
import shmapper.model.Relation;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;
import shmapper.model.VerticalMapping;

/* Responsible for generate the CSV files to plot a Network Graph of concepts and relations. */
public class GraphDataWriter {
	private SHInitiative	initiative;
	private String			path;
	private List<Notion>	notions;
	private Set<Relation>	relations;

	public GraphDataWriter(SHInitiative initiative, String path) {
		this.initiative = initiative;
		this.path = path;
	}

	/* Generates all the HTML Seon Pages. */
	public void generateDataFiles() {
		for (Mapping map : initiative.getContentMappings()) {
			if (!(map instanceof VerticalMapping)) {
				notions = new ArrayList<Notion>();
				relations = new HashSet<Relation>();
				// Generating the nodes file
				generateNodesFile(map);
				// Generating the edges file
				generateEdgesFile(map);
			}
		}
	}

	/* Reads the SEON Concepts and generates the Nodes CSV File. */
	private void generateNodesFile(Mapping map) {
		System.out.println("\nGenerating the Nodes CSV File (" + map + "): ");
		PrintWriter writer;
		try {
			// Creating the file
			writer = new PrintWriter(path + File.separator + map.getBase() + "-" + map.getTarget() + ".nodes.csv".replaceAll("[^a-zA-Z0-9.-]", ""), "UTF-8");
			writer.println("Id;Label;UFOType;Pack");

			// Getting the base notions info
			String pack = map.getBase().getName();
			int id = 1;
			for (Notion notion : map.getBase().getElements()) {
				String cid = String.format("%03d", id);
				String name = notion.getName();
				String ufot = notion.getIndirectUfotype().name();
				System.out.println(cid + ";" + name + ";" + ufot + ";" + pack);
				writer.println(cid + ";" + name + ";" + ufot + ";" + pack);
				notions.add(notion);
				relations.addAll(notion.getRelations());
				id++;
			}

			// Accessing the target notions
			List<Notion> tnotions = new ArrayList<Notion>();
			if (map instanceof DiagonalMapping) {
				DiagonalMapping dmap = (DiagonalMapping) map;
				tnotions.addAll(dmap.getTarget().getConcepts());
				tnotions.addAll(dmap.getTarget().getElements());
			} else if (map instanceof HorizontalMapping) {
				HorizontalMapping hmap = (HorizontalMapping) map;
				tnotions.addAll(hmap.getTarget().getElements());
			}

			// Getting the target notions info
			pack = map.getTarget().getName();
			for (Notion notion : tnotions) {
				String cid = String.format("%03d", id);
				String name = notion.getName();
				String ufot = notion.getIndirectUfotype().name();
				System.out.println(cid + ";" + name + ";" + ufot + ";" + pack);
				writer.println(cid + ";" + name + ";" + ufot + ";" + pack);
				notions.add(notion);
				relations.addAll(notion.getRelations());
				id++;
			}
			System.out.println((id - 1) + " notions.");
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/* Reads the SEON Concepts and generates the Edges CSV File. */
	private void generateEdgesFile(Mapping map) {
		System.out.println("Generating the Edges CSV File (" + map + "): ");
		PrintWriter writer;
		try {
			// Creating the file
			writer = new PrintWriter(path + File.separator + map.getBase() + "-" + map.getTarget() + ".edges.csv".replaceAll("[^a-zA-Z0-9.-]", ""), "UTF-8");
			writer.println("Source;Target;Label;RelationType;Id;Type");

			int id = 1;
			String type = "Directed";
			String cid, sid, tid, reltype, label;

			// Getting the notions' relations info
			reltype = "Relation";
			for (Relation relation : relations) {
				Notion source = relation.getSource();
				Notion target = relation.getTarget();
				int spos = notions.indexOf(source);
				int tpos = notions.indexOf(target);
				if (spos >= 0 && tpos >= 0) {
					cid = String.format("%03d", id);
					sid = String.format("%03d", spos + 1);
					tid = String.format("%03d", tpos + 1);
					label = relation.getName();
					if (label == null)
						label = relation.getType().name().toLowerCase();
					System.out.println(sid + ";" + tid + ";" + label + ";" + reltype + ";" + cid + ";" + type);
					writer.println(sid + ";" + tid + ";" + label + ";" + reltype + ";" + cid + ";" + type);
					id++;
				}
			}
			int count = id - 1;
			System.out.println(count + " relations.");

			// Getting the notions' matches info
			reltype = "Match";
			for (SimpleMatch match : map.getSimpleMatches()) {
				Notion source = match.getSource();
				Notion target = match.getTarget();
				int spos = notions.indexOf(source);
				int tpos = notions.indexOf(target);
				if (spos >= 0 && tpos >= 0) {
					cid = String.format("%03d", id);
					sid = String.format("%03d", spos + 1);
					tid = String.format("%03d", tpos + 1);
					label = match.getCoverage().getAbbreviation();
					System.out.println(sid + ";" + tid + ";" + label + ";" + reltype + ";" + cid + ";" + type);
					writer.println(sid + ";" + tid + ";" + label + ";" + reltype + ";" + cid + ";" + type);
					id++;
				}
			}
			System.out.println(id - 1 - count + " matches.");

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}