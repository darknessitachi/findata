package michael.findata.external.hexun2008;

import michael.findata.external.FinancialSheet;
import michael.findata.util.FinDataConstants;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2010-12-28
 * Time: 16:46:18
 * To change this template use File | Settings | File Templates.
 */
public class Hexun2008FinancialSheet extends FinancialSheet {
	public static final String[] FINANCIAL_SHEETNAMES = new String[]{michael.findata.util.FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET,
			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_PROFIT_AND_LOSS,
			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_CASH_FLOW,
			michael.findata.util.FinDataConstants.FINANCIAL_SHEET_PROVISION};
	private String st, ap;
	private Map<String, Number> data;
	private List<String> name;

	public Hexun2008FinancialSheet(String stockCode, String sheetType, int accountingYear, short season) {
		super(stockCode, sheetType, accountingYear, season);

		if (FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET.equals(sheetType)) {
			// 资产负债
			st = "zcfz";
		} else if (FinDataConstants.FINANCIAL_SHEET_PROFIT_AND_LOSS.equals(sheetType)) {
			// 利润
			st = "lr";
		} else if (FinDataConstants.FINANCIAL_SHEET_CASH_FLOW.equals(sheetType)) {
			// 现金流
			st = "xjll";
		} else if (FinDataConstants.FINANCIAL_SHEET_PROVISION.equals(sheetType)) {
			// 资产减值，拨备等
			st = "zcjz";
		} else {
			st = sheetType;
		}

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
			data = new HashMap<String, Number>();
			name = new ArrayList<String>();
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
		DecimalFormat df = new DecimalFormat("###,###.00");
		DOMParser parser = new DOMParser();
		try {
			parser.parse(getURL());
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
		Element nameElement, valueElement;
		Object o = tablePath.evaluate(doc);
		Number value;
		if (data == null) {
			data = new HashMap<String, Number>();
			name = new ArrayList<String>();
		}
		data.clear();
		name.clear();
		for (Element e : (List<Element>) o) {
			try {
				nameElement = (Element) fieldNamePath.evaluate(e);
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
			data.put(nameElement.getText(), value);
			name.add(nameElement.getText());
		}
	}
}
