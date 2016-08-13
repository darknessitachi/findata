package michael.findata.external.tdx;

import michael.findata.external.FileBasedPriceHistory;
import michael.findata.external.SecurityTimeSeriesData;
import org.joda.time.DateTime;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static michael.findata.util.FinDataConstants.TDX_BASE_DIR;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class TDXFileBasedPriceHistory extends FileBasedPriceHistory implements SecurityTimeSeriesData {

	public TDXFileBasedPriceHistory(String code) {
		super(code);
	}

	protected int getHeaderSize () {
		return 0;
	}

	protected int getRecordSize () {
		return 32;
	}
	@Override
	protected String getDataFileName() {
		if (code.startsWith("9") || code.startsWith("6") || code.startsWith("51")) {
			return TDX_BASE_DIR+"vipdoc/sh/lday/sh"+code+".day";
		} else {
			return TDX_BASE_DIR+"vipdoc/sz/lday/sz"+code+".day";
		}
	}

	@Override
	protected DateTime calDateTime() {
		return new DateTime(intRec[0]/10000, intRec[0]%10000/100, intRec[0]%10000%100, 15, 02);
	}

	private int getCoeff() {
		return code.startsWith("9") ? 1 : 10;
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
		return (intRec[1] & 0x0fffffff)*getCoeff();
	}

	protected int calHigh () {
		return (intRec[2] & 0x0fffffff)*getCoeff();
	}

	protected int calLow () {
		return (intRec[3] & 0x0fffffff)*getCoeff();
	}

	protected int calClose () {
		return (intRec[4] & 0x0fffffff)*getCoeff();
	}

	@Override
	protected int calVolume() {
		return intRec[6];
	}

	@Override
	protected float calAmount() {
		return floatRec[5];
	}
}