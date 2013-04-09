package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.sql.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç4:58
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "fin_data_updates", schema = "", catalog = "findata")
@Entity
public class FinDataUpdates {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private Date date;

	@javax.persistence.Column(name = "_date", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Basic
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FinDataUpdates that = (FinDataUpdates) o;

		if (id != that.id) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (date != null ? date.hashCode() : 0);
		return result;
	}
}
