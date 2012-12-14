package michael.findata.external.ths;

import michael.findata.external.CommonPriceHistory;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;

import static michael.findata.util.FinDataConstants.THS_BASE_DIR;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class THSPriceHistory extends CommonPriceHistory implements SecurityTimeSeriesData{

	public THSPriceHistory (String code) {
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
	protected int getCoeff() {
		return 1;
	}
}