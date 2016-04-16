package michael.findata.external.shse;

import michael.findata.util.FinDataConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SHSEShortableStockList {

	private Set<String> shortables;

	public SHSEShortableStockList() {
		DateTime dt = new DateTime();
		do {
			try {
				init(dt.toDate());
				break;
			} catch (IOException e) {
				dt = dt.minusDays(1);
			}
		} while (true);
	}

	public void init (Date date) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat(FinDataConstants.yyyyMMdd);
		String url = "http://biz.sse.com.cn/report/rzrq/dbp/zqdbp"+sdf.format(date)+".xls";
		InputStream is = new URL(url).openStream();
		POIFSFileSystem fs = new POIFSFileSystem(is);
		Workbook wb = new HSSFWorkbook(fs);
		shortables = new HashSet<>();
		wb.getSheetAt(1).forEach(row -> shortables.add(row.getCell(0).getStringCellValue()));
		fs.close();
		is.close();
	}

	public Set<String> getShortables() {
		return shortables;
	}
}
