package shmapper.model;

/* Represents the Coverage of a Concept on an Element in a match. */
public enum Coverage {
	EQUIVALENT("is EQUIVALENT to"),
	PARTIAL("is PART of"),
	WIDER("is WIDER than"),
	INTERSECTION("has INTERSECTION with"),
	NOTCOVERED("is NOT COVERED.");
	
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
}
