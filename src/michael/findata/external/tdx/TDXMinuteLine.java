package michael.findata.external.tdx;

import michael.findata.external.PriceHistory;
import michael.findata.external.SecurityTimeSeriesData;
import org.joda.time.DateTime;

import java.sql.Date;

import static michael.findata.util.FinDataConstants.TDX_BASE_DIR;

public class TDXMinuteLine extends PriceHistory implements SecurityTimeSeriesData {

	public TDXMinuteLine (String code) {
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
			return TDX_BASE_DIR+"vipdoc/sh/minline/sh"+code+".lc1";
		} else {
			return TDX_BASE_DIR+"vipdoc/sz/minline/sz"+code+".lc1";
		}
	}

	@Override
	protected DateTime calDateTime() {
//		System.out.println("Old "+calDate()+":"+calMinute());
		DateTime dt = new DateTime(shortRec[0]/2048+2004, (shortRec[0]%2048)/100, (shortRec[0]%2048)%100, shortRec[1] / 60, shortRec[1] % 60);
//		System.out.println("New "+dt);
		return dt;
	}

	protected Date calDate () {
		return new Date(new java.util.Date(shortRec[0]/2048+2004-1900, (shortRec[0]%2048)/100 - 1, (shortRec[0]%2048)%100).getTime());
	}

	protected int calMinute () {
		return shortRec[1];
	}

	protected int calOpen () {
		return (int) (floatRec[1]*1000);
	}

	protected int calHigh () {
		return (int) (floatRec[2]*1000);
	}

	protected int calLow () {
		return (int) (floatRec[3]*1000);
	}

	protected int calClose () {
		return (int) (floatRec[4]*1000);
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