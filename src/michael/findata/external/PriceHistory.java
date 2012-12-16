package michael.findata.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;

import static michael.findata.util.FinDataConstants.yyyyMMdd;

public abstract class PriceHistory implements SecurityTimeSeriesData{
	private int headerSize = 184, recordSize = 168;
	private int coeff = 1;
	protected int[] records = null;
	protected String code;
	protected File DataFile;
	protected FileChannel fc = null;
	protected IntBuffer lb;
	protected long count;
	protected ByteBuffer bb = null;

	public PriceHistory(String code) {
		headerSize = getHeaderSize();
		recordSize = getRecordSize();
		this.code = code;
		DataFile = new File(getDataFileName());
		coeff = getCoeff();
		if (!DataFile.exists()) {
			System.out.println("... No pricing data for stock " + code);
			return;
		}
		long fileSize = DataFile.length();
		bb = ByteBuffer.allocate(recordSize);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		lb = bb.asIntBuffer();
		records = new int [5];
		try {
			fc = new FileInputStream(DataFile).getChannel();
			fc.position(headerSize);
		} catch (FileNotFoundException e) {
			System.out.println("Exception caught when accessing stock price history file for " + code);
			e.printStackTrace();
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
			lb.get(records);
			java.sql.Date pricingDate;
			try {
				pricingDate = new java.sql.Date(yyyyMMdd.parse(records[0] + "").getTime());
			} catch (ParseException e) {
				System.out.println("Cannot parse a date in stock price history file for " + code + ": " + records[0]);
				e.printStackTrace();
				return null;
			}
			SecurityTimeSeriesDatum temp = new SecurityTimeSeriesDatum();
			temp.setDate(pricingDate);
			temp.setOpen((records[1] & 0x0fffffff)*coeff);
			temp.setHigh((records[2] & 0x0fffffff)*coeff);
			temp.setLow((records[3] & 0x0fffffff)*coeff);
			temp.setClose((records[4] & 0x0fffffff)*coeff);
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
	protected abstract int getCoeff ();
}
