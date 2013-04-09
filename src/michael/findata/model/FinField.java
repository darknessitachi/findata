package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç4:58
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "fin_field", schema = "", catalog = "findata")
@Entity
public class FinField {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String finSheet;

	@javax.persistence.Column(name = "fin_sheet", nullable = true, insertable = true, updatable = true, length = 255, precision = 0)
	@Basic
	public String getFinSheet() {
		return finSheet;
	}

	public void setFinSheet(String finSheet) {
		this.finSheet = finSheet;
	}

	private String name;

	@javax.persistence.Column(name = "name", nullable = true, insertable = true, updatable = true, length = 255, precision = 0)
	@Basic
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FinField finField = (FinField) o;

		if (id != finField.id) return false;
		if (finSheet != null ? !finSheet.equals(finField.finSheet) : finField.finSheet != null) return false;
		if (name != null ? !name.equals(finField.name) : finField.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (finSheet != null ? finSheet.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
