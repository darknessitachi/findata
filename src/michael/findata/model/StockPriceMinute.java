package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.sql.Timestamp;

@Entity
@Table(name = "stock_price_minute")
public class StockPriceMinute {

	private long id;
	private Stock stock;
	private Timestamp date;

	@Override
	public int hashCode () {
		return (int) getId();
	}

	@Override
	public String toString () {
		return "Minute lines: "+getStock().getCode()+" "+getDate();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StockPriceMinute that = (StockPriceMinute) o;

//		if (id != that.id) return false;
		if (stock != null ? !stock.equals(that.stock) : that.stock != null) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;

		return true;
	}

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id", nullable = false, insertable = true, updatable = true, length = 20, precision = 0)
	@Id
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@JoinColumn(name = "stock_id", nullable = false, insertable = true, updatable = false)
	@ManyToOne
	public Stock getStock() {
		return stock;
	}
	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Column(name = "date", nullable = false, insertable = true, updatable = true, length = 19, precision = 0)
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}

	/**
	 * Data format: from 09:31AM to 15:00PM : open(int, 4byte)high(int, 4byte)low(int, 4byte)close(int, 4byte)volume(int, 4byte)amount(float, 4byte)
	 * Note: on and before 13-Jul-2016, low are not stored due to error. However, it shouldn't affect anything, because 'low' are not used much.
	 * */
	@Lob
	@Column(name = "minute_data", columnDefinition="blob")
	@Basic
	private byte[] getMinuteData() {
		if (bb == null) {
			return null;
		} else {
			return bb.array();
		}
	}

	private void setMinuteData(byte[] minuteData) {
		if (minuteData != null) {
			bb = ByteBuffer.wrap(minuteData);
			ib = bb.asIntBuffer();
			fb = bb.asFloatBuffer();
		}
	}

	@Transient
	private ByteBuffer bb = null;

	@Transient
	private IntBuffer ib = null;

	@Transient
	private FloatBuffer fb = null;

	public int getOpen(int minuteNo) {
		return getInt(minuteNo*6);
	}
	public void setOpen(int minuteNo, int value) {
		setInt(minuteNo*6, value);
	}

	public int getHigh(int minuteNo) {
		return getInt(minuteNo*6 + 1);
	}
	public void setHigh(int minuteNo, int value) {
		setInt(minuteNo*6 + 1, value);
	}

	public int getLow(int minuteNo) {
		return getInt(minuteNo*6 + 2);
	}
	public void setLow(int minuteNo, int value) {
		setInt(minuteNo*6 + 2, value);
	}

	public int getClose(int minuteNo) {
		return getInt(minuteNo*6 + 3);
	}
	public void setClose(int minuteNo, int value) {
		setInt(minuteNo*6 + 3, value);
	}

	public int getVolum(int minuteNo) {
		return getInt(minuteNo*6 + 4);
	}
	public void setVolume(int minuteNo, int value) {
		setInt(minuteNo*6 + 4, value);
	}

	public float getAmount(int minuteNo) {
		return getFloat(minuteNo*6 + 5);
	}
	public void setAmount(int minuteNo, float value) {
		setFloat(minuteNo*6 + 5, value);
	}

	private int getInt (int index) {
		return ib == null ? -1 : ib.get(index);
	}
	private float getFloat (int index) {
		return fb == null ? -1 : fb.get(index);
	}
	private void setInt (int index, int value) {
		if (bb == null) {
			bb = ByteBuffer.allocate(240*6*4);
			ib = bb.asIntBuffer();
			fb = bb.asFloatBuffer();
		}
		ib.put(index, value);
	}
	private void setFloat (int index, float value) {
		if (bb == null) {
			bb = ByteBuffer.allocate(240*6);
			ib = bb.asIntBuffer();
			fb = bb.asFloatBuffer();
		}
		fb.put(index, value);
	}
}