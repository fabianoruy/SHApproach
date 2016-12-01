package shmapper.applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import shmapper.model.SHInitiative;
import shmapper.model.User;

/** Main Application, responsible for managing the remaining. */
public class ManagerApp {
	private AstahParseApp			parseApp;
	private StructuralMappingApp	smapApp;
	private MappingApp				mapApp;
	private SHInitiative			initiative;
	private String					astahpath;
	private String					mapperpath;
	private String					initpath;
	private String					logpath;
	public PrintStream				log;
	private static boolean			readUsers	= false;

	/** Constructor initializing the main application parameters. */
	public ManagerApp(User user, String path) {
		// Saving paths
		this.mapperpath = path;
		String title = user.getLogin().toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "");
		this.initpath = "initiative/" + title + File.separator;

		// Creating log
		this.logpath = createLogOutput();
		log.println("\n### STARTING APPLICATION ###");
		log.println("mapperpath: " + mapperpath);
		log.println("initpath: " + initpath);

		// Revovering the initiative
		this.initiative = openInitiative(user.getLogin());
	}

	/** Returns the AstahParseApp, creates if doesn't exists. */
	public AstahParseApp getParser() {
		if (this.parseApp == null) {
			this.parseApp = new AstahParseApp(this, initiative);
		}
		return parseApp;
	}

	/** Returns the StructuralMappingApp, creates if doesn't exists. */
	public StructuralMappingApp getStructMapper() {
		if (this.smapApp == null) {
			this.smapApp = new StructuralMappingApp(this, initiative);
		}
		return smapApp;
	}

	/** Returns the MappingApp, creates if doesn't exists. */
	public MappingApp getMapper() {
		if (this.mapApp == null) {
			this.mapApp = new MappingApp(this, initiative);
		}
		return mapApp;
	}

	/** Returns the Initiative. */
	public SHInitiative getInitiative() {
		return initiative;
	}

	/** Returns the log path (for the page link). */
	public String getLogpath() {
		return logpath;
	}

	public String getMapperpath() {
		return mapperpath;
	}

	public String getInitpath() {
		return initpath;
	}

	/** Opens an Initiative, if exists, or creates a new one. */
	private SHInitiative openInitiative(String title) {
		// recover initiative from MD/ID/data/[domain].ser, if exists
		SHInitiative init = recoverInitiative(mapperpath + initpath);
		// creates a new one if doesn't
		if (init == null) {
			init = new SHInitiative(title);
			init.setDatafile(mapperpath + initpath + "data/initdata.ser");
			log.println("# NEW Initiative: " + init);
		} else {
			log.println("# Recovered Initiative: " + init);
		}
		return init;
	}

	/** Recovers the selected initiative from the disk, if exists. */
	private SHInitiative recoverInitiative(String path) {
		SHInitiative init = null;
		try {
			FileInputStream fileIn = new FileInputStream(path + "data/initdata.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			init = (SHInitiative) in.readObject();
			init.setDatafile(path + "data/initdata.ser");
			in.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			log.println("Initiative file not found: " + path + "data/initdata.ser");
			return null;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(log);
		}
		return init;
	}

	/** Creates the output log file. */
	private String createLogOutput() {
		String logname = "log." + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date()) + ".txt";
		String logfile = mapperpath + initpath + "log/" + logname;
		new File(logfile).getParentFile().mkdirs();
		System.out.println("# log: " + logfile + "\n");
		try {
			PrintStream ps = new PrintStream(logfile);
			String os = System.getProperty("os.name");
			if (os.contains("Linux"))
				this.log = ps;
			else if (os.contains("Windows"))
				this.log = System.out;

			this.log.println("SH Approach log file - " + new java.util.Date());
			this.log.println("----------------------------------------------------");

			// indexing the logfile
			FileWriter fw = new FileWriter(mapperpath + "initiative/logindex.txt", true);
			fw.write(logfile + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initpath + "log/" + logname;
	}

	//////////////////////////// STATIC ////////////////////////////
	public static void manageUsers(String path) {
		// createUsers(path); only once!!
		if (!readUsers) {
			recoverUsers(path);
		}
	}

	/** Recovers the users from the disk. */
	@SuppressWarnings("unchecked")
	private static void recoverUsers(String path) {
		try {
			FileInputStream fileIn = new FileInputStream(path + "initiative/index.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			User.setList((List<User>) in.readObject());
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		readUsers = true;
	}

	/** Saves the users to the disk (to be called just once). */
	@SuppressWarnings("unused")
	private static void createUsers(String path) {
		// Creates the users and save them in a index file
		List<User> users = new ArrayList<User>();
		users.add(new User("Quality Assurance", "."));
		users.add(new User("Configuration Management", "CM17"));
		users.add(new User("Requirements Development", "RD18"));
		users.add(new User("Software Design", "SD19"));
		try {
			FileOutputStream fileOut = new FileOutputStream(path + "initiative/index.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(users);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Creates a log index file
		PrintStream logindex;
		try {
			logindex = new PrintStream(path + "initiative/logindex.txt");
			logindex.println("SH Approach Log Index - " + new java.util.Date());
			logindex.println("---------------------------------------------------");
			logindex.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
