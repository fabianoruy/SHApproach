package shmapper.applications;

import java.util.logging.Logger;

import shmapper.model.SHInitiative;

/** Main Application, responsible for managing the remain. */
public class ManagerApp {
	private InitiativeStartApp		startApp;
	private AstahParseApp			parseApp;
	private StructuralMappingApp	smapApp;
	private MappingApp				mapApp;
	private SHInitiative			initiative;
	private String					astahpath;
	private String					mapperpath;
	private String					initpath;
	private String					logfile;

}
