package michael.findata.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "cache")
public class Cache {

	public static final String CACHE_NAME_SHORTABLES = "shortables";

	@Id
	private int id;

	@Basic
	@Column(name = "name")
	private String name;

	@Basic
	@Column(name = "value", columnDefinition = "text")
	private String value;

	@Basic
	@Column(name = "last_updated")
	private Timestamp lastUpdated;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}