package shmapper.model;

/** Represents a Decision taken on an uncovered element. */
public class AnalysisDecision extends SerializableObject {
	private static final long	serialVersionUID	= -2589581715892766105L;
	private Reason				reason;
	private String				justification;
	private Element				element;

	public static enum Reason {
		ALREADYCOVERED("Already Covered", "All the aspects of the element are already covered by other matches"),
		OUTOFSCOPE("Out of Scope", "The element's uncovered portion is not to be considered in the initiative scope"), OTHER("Other", "Other reason (explain in justification)");

		private final String	text;
		private final String	description;

		private Reason(final String text, final String description) {
			this.text = text;
			this.description = description;
		}

		public String getText() {
			return text;
		}

		public String getDescription() {
			return description;
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
		return (reason.equals(oDecision.reason) && justification.equals(oDecision.justification) && element.equals(oDecision.element));
	}

}