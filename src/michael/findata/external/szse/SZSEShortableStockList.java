package michael.findata.external.szse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicky on 2015/11/24.
 */
public class SZSEShortableStockList {
	public static String url = "http://www.szse.cn/szseWeb/ShowReport.szse?SHOWTYPE=EXCEL&CATALOGID=1834_xxpl&tab1PAGENUM=1&ENCODE=1&TABKEY=tab1";
	private Set<String> shortables;

	public SZSEShortableStockList() throws IOException {
		FileInputStream is = new FileInputStream("szseShortables.xls");
		POIFSFileSystem fs = new POIFSFileSystem(is);
		Workbook wb = new HSSFWorkbook(fs);
		shortables = new HashSet<>();
		wb.getSheetAt(0).forEach(row -> shortables.add(row.getCell(0).getStringCellValue()));
		is.close();
		fs.close();
	}

	public Set<String> getShortables() {
		return shortables;
	}
}