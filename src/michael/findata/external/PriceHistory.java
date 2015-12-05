package michael.findata.external;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.sql.Date;

public abstract class PriceHistory implements SecurityTimeSeriesData{
	private int headerSize = 184, recordSize = 168;
	protected int[] intRec = null;
	protected String code;
	protected File DataFile;
	protected FileChannel fc = null;
	protected IntBuffer lb;
	protected ShortBuffer sb;
	protected short[] shortRec;
	protected FloatBuffer fb;
	protected float[] floatRec;

	protected long count;
	protected ByteBuffer bb = null;

	public PriceHistory(String code) {
		headerSize = getHeaderSize();
		recordSize = getRecordSize();
		this.code = code;
		DataFile = new File(getDataFileName());
		if (!DataFile.exists()) {
			System.out.println("... No pricing data for stock " + code);
			return;
		}
		long fileSize = DataFile.length();
		bb = ByteBuffer.allocate(recordSize);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		lb = bb.asIntBuffer();
		intRec = new int [8];
		sb = bb.asShortBuffer();
		shortRec = new short[16];
		fb = bb.asFloatBuffer();
		floatRec = new float[8];

		try {
			fc = new FileInputStream(DataFile).getChannel();
			fc.position(headerSize);
		} catch (IOException e) {
			System.out.println("Exception caught when accessing stock price history file for " + code);
			e.printStackTrace();
		}
		count = fileSize - recordSize;
	}

	@Override
	public SecurityTimeSeriesDatum next() {
		if (count > headerSize) {
			try {
				fc.position(count);
			} catch (IOException e) {
				System.out.println("Cannot read stock price history file for " + code);
				e.printStackTrace();
				return null;
			}
			try {
				if (fc.read(bb) == -1) {
					return null;
				}
			} catch (IOException e) {
				System.out.println("Cannot read stock price history file for " + code);
				e.printStackTrace();
				return null;
			}
			bb.flip();
			lb.position(0);
			lb.get(intRec);
			sb.position(0);
			sb.get(shortRec);
			fb.position(0);
			fb.get(floatRec);

//			java.sql.Date pricingDate;
//			try {
//				SimpleDateFormat format_yyyyMMdd = new SimpleDateFormat(yyyyMMdd);
//				pricingDate = new java.sql.Date(format_yyyyMMdd.parse(intRec[0] + "").getTime());
//			} catch (ParseException e) {
//				System.out.println("Cannot parse a date in stock price history file for " + code + ": " + intRec[0]);
//				e.printStackTrace();
//				return null;
//			}
			SecurityTimeSeriesDatum temp = new SecurityTimeSeriesDatum();
			temp.setDateTime(calDateTime());
			temp.setOpen(calOpen());
			temp.setHigh(calHigh());
			temp.setLow(calLow());
			temp.setClose(calClose());
			temp.setVolume(calVolume());
			temp.setAmount(calAmount());
			bb.clear();
			count -= recordSize;
			return temp;
		} else {
			return null;
		}
	}

	@Override
	public void close() {
		try {
			if (fc != null) {
				fc.close();
			}
		} catch (IOException e) {
			System.out.println("Cannot close stock price history file for " + code);
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		return count > headerSize;
	}

	protected abstract int getHeaderSize();
	protected abstract int getRecordSize();
	protected abstract String getDataFileName ();
	protected abstract DateTime calDateTime ();
//	protected abstract int calMinute ();
	protected abstract int calOpen ();
	protected abstract int calHigh ();
	protected abstract int calLow ();
	protected abstract int calClose ();
	protected abstract int calVolume ();
	protected abstract float calAmount ();
}