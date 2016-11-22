package shmapper.model;

import java.io.Serializable;
import java.util.UUID;

/* Responsable for the main features for serializing the Initiative objects. */
public abstract class SerializableObject implements Serializable {
	private static final long	serialVersionUID	= -7104770683761284518L;
	private String				id					= UUID.randomUUID().toString();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		return (this.getClass().equals(other.getClass()) && this.id.equals(((SerializableObject) other).id));
	}

}
