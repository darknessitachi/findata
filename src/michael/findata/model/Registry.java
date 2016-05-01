package michael.findata.model;

import javax.persistence.*;

@Entity
@Table(name = "registry")
public class Registry {

	@Id
	private int id;

	@Basic
	@Column(name = "name")
	private String name;

	@Basic
	@Column(name = "value")
	private String value;

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
}