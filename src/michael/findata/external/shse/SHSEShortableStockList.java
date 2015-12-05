package michael.findata.external.shse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicky on 2015/11/25.
 */
public class SHSEShortableStockList {
	public static String url = "http://biz.sse.com.cn/report/rzrq/dbp/zqdbp20151125.xls";
	private Set<String> shortables;

	public SHSEShortableStockList() throws IOException {
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
