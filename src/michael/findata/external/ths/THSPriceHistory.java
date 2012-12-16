package michael.findata.external.ths;

import michael.findata.external.PriceHistory;
import michael.findata.external.SecurityTimeSeriesData;

import static michael.findata.util.FinDataConstants.THS_BASE_DIR;

public class THSPriceHistory extends PriceHistory implements SecurityTimeSeriesData{

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