package shmapper.model;

/** Represents a Decision taken on an uncovered element. */
public class AnalysisDecision extends SerializableObject {
	private static final long	serialVersionUID	= -2589581715892766105L;
	private Reason				reason;
	private String				justification;
	private Element				element;

	public static enum Reason {
		ALREADYCOVERED("Already Covered"), OUTOFSCOPE("Out of Scope"), OTHER("Other");

		private final String text;

		private Reason(final String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public AnalysisDecision(Reason reason, String justification, Element element) {
		this.reason = reason;
		this.justification = justification;
		this.element = element;
	}

	public Reason getReason() {
		return reason;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Element getElement() {
		return element;
	}

	@Override
	public String toString() {
		return "Decision: " + element + " (" + reason + ")";
	}

	@Override
	public boolean equals(Object other) {
		AnalysisDecision oDecision = (AnalysisDecision) other;
		return (reason.equals(oDecision.reason) && element.equals(oDecision.element));
	}

}