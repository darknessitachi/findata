package michael.findata.external.tdx;

import michael.findata.external.CommonPriceHistory;
import michael.findata.external.SecurityTimeSeriesData;

import static michael.findata.util.FinDataConstants.TDX_BASE_DIR;

public class TDXPriceHistory extends CommonPriceHistory implements SecurityTimeSeriesData {

	public TDXPriceHistory (String code)
	{
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
		if (code.startsWith("9") || code.startsWith("6")) {
			return TDX_BASE_DIR+"vipdoc/sh/lday/sh"+code+".day";
		} else {
			return TDX_BASE_DIR+"vipdoc/sz/lday/sz"+code+".day";
		}
	}

	@Override
	protected int getCoeff() {
		return code.startsWith("9") ? 1 : 10;
	}
}