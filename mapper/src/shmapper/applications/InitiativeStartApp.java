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

/** Responsible for manager the starting of an initiative. */
public class InitiativeStartApp {
	private String mapperdir;
	private String initdir;
	private SHInitiative initiative;
	private static boolean readUsers = false;

	public InitiativeStartApp(String mapperdir) {
		this.mapperdir = mapperdir;
		//createUsers(mapperdir); //only once!
		if (!readUsers) {
			recoverUsers(mapperdir);
		}
	}

	public SHInitiative openInitiative(String login, String pword) {
		// Check user
		User user = User.validate(login, pword);
		System.out.println("Validated " + user);
		if (user != null) {
			String title = login;
			// set initdir (initiative/[domain]/)
			this.initdir = "initiative/" + title.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "") + "/";
			// recover initiative from MD/ID/data/[domain].ser, if exists
			this.initiative = recoverInitiative(mapperdir + initdir);
			// creates a new one if doesn't
			if (this.initiative == null) {
				this.initiative = new SHInitiative(title);
				this.initiative.setDatafile(mapperdir + initdir + "data/initdata.ser");
				this.initiative.saveInitiative();
				System.out.println("NEW Initiative: " + initiative);
			} else {
				System.out.println("Recovered Initiative: " + initiative);
			}
		}
		return initiative;
	}

	/* Recovers the selected initiative from the disk. */
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
			System.out.println("Initiative file not found: " + path + "data/initdata.ser");
			return null;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return init;
	}

	/* Recovers the users from the disk. */
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

	/* Saves the users to the disk (to be called just once). */
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

	/* Defines the output log file. */
	public String createLogOutput() {
		
		String logfile = "log/SHLog." + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date()) + ".txt";
		PrintStream ps;
		new File(mapperdir + initdir + logfile).getParentFile().mkdirs();
		System.out.println("# log: " + mapperdir +"\n"+ initdir +"\n"+ logfile + "\n");
		try {
			ps = new PrintStream(mapperdir + initdir + logfile);
			System.setOut(ps);
			System.setErr(ps);
			System.out.println("SH Approach log file - " + new java.util.Date());
			System.out.println("----------------------------------------------------");

			FileWriter fw = new FileWriter(mapperdir + "initiative/logindex.txt", true);
			fw.write(mapperdir + initdir + logfile + "\n");
		    fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initdir + logfile;
	}

}