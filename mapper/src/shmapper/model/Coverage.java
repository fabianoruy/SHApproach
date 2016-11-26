package shmapper.model;

/* Represents the Coverage of a Concept on an Element in a match. */
public enum Coverage {
	EQUIVALENT("is EQUIVALENT to"),
	PARTIAL("is PART of"),
	WIDER("is WIDER than"),
	INTERSECTION("has INTERSECTION with"),
	NOCOVERAGE("has NO COVERAGE."),
	CORRESPONDENCE("is (structurally) CORRESPONDENT with");
	
	private final String text;
	
    private Coverage (final String text) {
        this.text = text;
    }
    
    public String getText() {
	return text;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public String getAbbreviation() {
		return "[" + this.name().charAt(0) + "]";
    }
}
