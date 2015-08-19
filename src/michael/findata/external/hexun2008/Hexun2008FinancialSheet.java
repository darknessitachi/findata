package michael.findata.external.hexun2008;

import michael.findata.external.FinancialSheet;
import michael.findata.util.FinDataConstants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.SheetType;

public class Hexun2008FinancialSheet extends FinancialSheet {
//	public static final String[] FINANCIAL_SHEETNAMES = new String[]{
//			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET,
//			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_PROFIT_AND_LOSS,
//			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_CASH_FLOW,
//			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_PROVISION};
	private String st, ap;
	private Map<String, Number> data;
	private List<String> name;
	private int earliestYear = 1989;

	public Hexun2008FinancialSheet(String stockCode, SheetType sheetType, int accountingYear, short season) {
		super(stockCode, sheetType, accountingYear, season);

		switch (sheetType) {
			case balance_sheet:
				st = "zcfz";
				break;
			case profit_and_loss:
				st = "lr";
				break;
			case cash_flow:
				st = "xjll";
				break;
			case provision:
				st = "zcjz";
				break;
		}

//		if (FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET.equals(sheetType)) {
//		} else if (FinDataConstants.FINANCIAL_SHEET_PROFIT_AND_LOSS.equals(sheetType)) {
//		} else if (FinDataConstants.FINANCIAL_SHEET_CASH_FLOW.equals(sheetType)) {
//		} else if (FinDataConstants.FINANCIAL_SHEET_PROVISION.equals(sheetType)) {
//		} else {
//		}

		switch (accountingSeason) {
			case 1:
				ap = accountingYear + ".03.15";
				break;
			case 2:
				ap = accountingYear + ".06.30";
				break;
			case 3:
				ap = accountingYear + ".09.30";
				break;
			case 4:
				ap = accountingYear + ".12.31";
				break;
			default:
				ap = String.valueOf(accountingYear);
		}
		if (data == null) {
			data = new HashMap<>();
			name = new ArrayList<>();
		}
		getData();
	}

	@Override
	public Iterator<String> getDatumNames() {
		return name.iterator();
	}

	@Override
	public Number getValue(String name) {
		return data.get(name);
	}

	@Override
	public String getURL() {
		return "http://stockdata.stock.hexun.com/2008/" + st + ".aspx?stockid=" + stockCode + "&accountdate=" + ap;
	}

	@Override
	public String getName() {
		return "hexun2008";
	}

	private void getData() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		DOMParser parser = new DOMParser();
		try {
			HttpGet get = new HttpGet(getURL());
			get.setHeader("Host", "stockdata.stock.hexun.com");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Cache-Control", "max-age=0");
			get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36");
			get.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			get.setHeader("Accept-Language", "en-US,en;q=0.8");
			CloseableHttpResponse response = httpClient.execute(get);
//			BufferedReader br = new BufferedReader();
			parser.parse(new InputSource(new InputStreamReader(response.getEntity().getContent(), "GB2312")));
//			parser.parse();
			httpClient.close();
		} catch (FileNotFoundException e) {
			// No such stock
			System.out.println("Error in getting data for " + stockCode + " " + this.accountingYear + " " + this.accountingSeason + " " + this.getSheetType());
			return;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DOMReader domReader = new DOMReader();
		Document doc = domReader.read(parser.getDocument());
//		XPath tablePath = doc.createXPath("//DIV[@id=\"zaiyaocontent\"]//TBODY/TR[position()>1 and position()<last()]");
		XPath tablePath = doc.createXPath("//DIV[@id=\"zaiyaocontent\"]//TBODY/TR");
		XPath fieldNamePath = doc.createXPath(".//STRONG[1]");
		XPath fieldValuePath = doc.createXPath("./TD[2]/DIV");

		try {
			Pattern p = Pattern.compile("\\['(\\d\\d\\d\\d)\\.\\d\\d.\\d\\d','\\d+.{1,5}度'\\]\\];$");
			XPath scriptPath = doc.createXPath("//DIV[@id=\"zaiyaocontent\"]/SCRIPT[1]");
			Matcher m = p.matcher(((Element)scriptPath.evaluate(doc)).getText());
			if (m.find()) {
				earliestYear = Integer.parseInt(m.group(1));
			}
		} catch (ClassCastException ex) {
		}

		Element valueElement;
		Object o = tablePath.evaluate(doc);
		Number value;
		String datumName;
		if (data == null) {
			data = new HashMap<>();
			name = new ArrayList<>();
		}
		data.clear();
		name.clear();
		DecimalFormat df = new DecimalFormat(Hexun2008Constants.NORMAL_DECIMAL_FORMAT);
		for (Element e : (List<Element>) o) {
			try {
//				nameElement = ;
				datumName = ((Element) fieldNamePath.evaluate(e)).getText();
				if ("会计年度".equals(datumName) || "备注".equals(datumName) || "报告年度".equals(datumName) || "发布日期".equals(datumName)) {
					continue;
				}
				valueElement = (Element) fieldValuePath.evaluate(e);
			} catch (ClassCastException ex) {
				continue;
			}
			try {
				value = df.parse(valueElement.getText());
			} catch (NumberFormatException ex) {
				value = null;
			} catch (ParseException e1) {
				value = null;
			}
			data.put(datumName, value);
			name.add(datumName);
		}
	}

	public int getEarliestYear () {
		return earliestYear;
	}
}