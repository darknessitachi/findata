package michael.findata.external.ths;

import michael.findata.external.FileBasedPriceHistory;
import michael.findata.external.SecurityTimeSeriesData;
import org.joda.time.DateTime;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static michael.findata.util.FinDataConstants.THS_BASE_DIR;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class THSFileBasedPriceHistory extends FileBasedPriceHistory implements SecurityTimeSeriesData{

	public THSFileBasedPriceHistory(String code) {
		super(code);
	}

	protected int getHeaderSize () {
		return 184;
	}

	protected int getRecordSize () {
		return 168;
	}

	@Override
	protected String getDataFileName() {
		if (code.startsWith("9") || code.startsWith("6")) {
			return THS_BASE_DIR+"history/shase/day/"+code+".day";
		} else {
			return THS_BASE_DIR+"history/sznse/day/"+code+".day";
		}
	}

	@Override
	protected DateTime calDateTime() {
		return new DateTime(intRec[0]/10000, intRec[0]%10000/100, intRec[0]%10000%100, 23, 59);
	}

	protected Date calDate () {
		try {
			SimpleDateFormat format_yyyyMMdd = new SimpleDateFormat(yyyyMMdd);
			return new java.sql.Date(format_yyyyMMdd.parse(intRec[0] + "").getTime());
		} catch (ParseException e) {
			System.out.println("Cannot parse a date in stock price history file for " + code + ": " + intRec[0]);
			e.printStackTrace();
			return null;
		}
	}

	protected int calMinute () {
		return -1;
	}

	protected int calOpen () {
		return (intRec[1] & 0x0fffffff);
	}

	protected int calHigh () {
		return (intRec[2] & 0x0fffffff);
	}

	protected int calLow () {
		return (intRec[3] & 0x0fffffff);
	}

	protected int calClose () {
		return (intRec[4] & 0x0fffffff);
	}

	@Override
	protected int calVolume() {
		return 0; // todo
	}

	@Override
	protected float calAmount() {
		return 0; // todo
	}
}