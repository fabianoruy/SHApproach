package shmatcher.model;

/* Represents a Match between an Element and a Concept/Element. */
public class SimpleMatch extends Match {
    private Notion target;

    public SimpleMatch(Element elem, Concept conc, Coverage cover, String comm) {
	super(elem, cover, comm);
	this.target = conc;
    }

    public SimpleMatch(Element source, Element target, Coverage cover, String comm) {
	super(source, cover, comm);
	this.target = target;
    }

    public Notion getTarget() {
	return target;
    }

    public String getType() {
	return "E [" + getCoverage().name().charAt(0) + "] " + target.getClass().getName().charAt(0);
    }

    @Override
    public String toString() {
	return getSource().getName() + " [" + getCoverage().name().charAt(0) + "] " + target.getName() + " {" + getComment()
		+ "}";
    }

}