package michael.findata.external.cninfo;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Michael Tang on 2014/12/20.
 */
public class CnInfoReportPublicationList implements ReportPublicationList{
	// This is only useful if you have missed no more than 5 seasons of report publication dates.
	public static Pattern p = Pattern.compile(".*([\\d,O]{4})\\s*Äê?(.*)±¨¸æ.*(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
	public CnInfoReportPublicationList (String code) throws IOException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		Stream<String []> urls = Stream.of(
				new String[]{"http://www.cninfo.com.cn/disclosure/1qreport/stocks/1qr1y/cninfo/${code}.js", "1"},
				new String[]{"http://www.cninfo.com.cn/disclosure/seannualreport/stocks/sar1y/cninfo/${code}.js", "2"},
				new String[]{"http://www.cninfo.com.cn/disclosure/3qreport/stocks/3qr1y/cninfo/${code}.js", "3"},
				new String[]{"http://www.cninfo.com.cn/disclosure/annualreport/stocks/ar1y/cninfo/${code}.js", "4"}
		);
		List result = urls.map(s -> {
			BufferedReader br = null;
			try {
				String url;
				if (FinDataConstants.BAShareCodeRef.containsKey(code)) {
					url = s[0].replace("${code}", FinDataConstants.BAShareCodeRef.get(code));
				} else {
					url = s[0].replace("${code}", code);
				}
				br = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			} catch (IOException e) {
				return new ReportPublication [] {}; // meaningless data
			}
			String input = null;
			try {
				input = br.readLine();
				br.close();
			} catch (IOException e) {
				input = "";
			}
			return Stream.of(input.split("\\],\\[")).map(line -> {
				Matcher m = p.matcher(line);
				if (m.find()) {
					try {
						return new ReportPublication(
								FORMAT_yyyyDashMMDashdd.parse(m.group(3)),
								code, null, Integer.parseInt(m.group(1)),
								Integer.parseInt(s[1]));
					} catch (ParseException e) {
						return new ReportPublication(null, null, null, -1, -1); // meaningless data
					}
				} else {
					return new ReportPublication(null, null, null, -1, -1); // meaningless data
				}
			}).toArray();
		}).flatMap(ss -> (Stream.of((Object[]) ss))).collect(Collectors.toList());
		pbs = result;
	}

	private Collection<ReportPublication> pbs = new ArrayList<>();

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;
	}
}