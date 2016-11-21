package shmapper.applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import shmapper.model.SHInitiative;

/** Responsible for manager the starting of an initiative. */
public class InitiativeStartApp {
	private String			mapperdir;
	private String			initdir;
	private SHInitiative	initiative;

	public InitiativeStartApp(String mapperdir) {
		this.mapperdir = mapperdir;
	}

	public SHInitiative recoverInitiative(String title, String pword) {
		// TODO read MD/initiative/index.ser
		// TODO select (and set) initiative and check pword
		// set ID (initiative/ + [domain]/)
		this.initdir = "initiative/"+ title.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "") + "/";
		
		// TODO recover initiative from MD/ID/data/[domain].ser, if exists
		this.initiative = new SHInitiative(title, "", "", "", null);
		return initiative;

	}

	/* Defines the output log file. */
	public String createLogOutput() {
		String logfile = "log/SHLog." + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date()) + ".txt";
		PrintStream ps;
		File file = new File(mapperdir + initdir + "log/");
		boolean created = file.mkdirs();
		System.out.println("# Created"+ file +": "+ created);
		try {
			ps = new PrintStream(mapperdir + initdir + logfile);
			//System.setOut(ps);
			//System.setErr(ps);
			System.out.println("SH Approach log file - " + new java.util.Date());
			System.out.println("---------------------------------------------------");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return initdir + logfile;
	}

}