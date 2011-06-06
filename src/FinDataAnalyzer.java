import michael.findata.external.hexun2008.Hexun2008Constants;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;

public class FinDataAnalyzer {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
        FileWriter fw = new FileWriter("c:/test.txt");
        Connection con = jdbcConnection();
        Statement st = con.createStatement();
        Statement stCodes = con.createStatement();
        ResultSet rsCodes = stCodes.executeQuery("select code from stock where latest_year = '2011' and not is_financial order by code");
        ResultSet rsResult;
        CallableStatement cs;
        String param_stock_code;
        while (rsCodes.next()) {
            param_stock_code = rsCodes.getString(1);
            cs = con.prepareCall("CALL avg_free_cf ('" + param_stock_code + "')");
            rsResult = cs.executeQuery();
//			rsResult = st.executeQuery(
//					"select\n" +
//					"avg (((a.value + b.value + c.value + d.value - e.value)/g.value))\n" +
//					"from\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '?????' and fin_season = 4) a,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '????????' and fin_season = 4) b,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '???????????????????????' and fin_season = 4) c,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '?????????????????????????' and fin_season = 4) d,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '???????????????????????' and fin_season = 4) e,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '?????????????????' and fin_season = 4) f,\n" +
//					"(select fin_year, fin_season, if (value is null, 0, value) as value from  fin_data_"+param_stock_code+" where name = '??????????????' and fin_season = 4) g,\n" +
//					"(select number_of_shares from stock where code = "+param_stock_code+") h\n" +
//					"where\n" +
//					"a.fin_year = b.fin_year and a.fin_season = b.fin_season and\n" +
//					"b.fin_year = c.fin_year and b.fin_season = c.fin_season and\n" +
//					"c.fin_year = d.fin_year and c.fin_season = d.fin_season and\n" +
//					"d.fin_year = e.fin_year and d.fin_season = e.fin_season and\n" +
//					"e.fin_year = f.fin_year and e.fin_season = f.fin_season and\n" +
//					"f.fin_year = g.fin_year and f.fin_season = g.fin_season");
            if (rsResult.next()) {
                System.out.print(param_stock_code + "\t" +
                        rsResult.getString(2) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(3)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(4)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(5)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(6)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(7)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(8)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(9)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(10)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(11)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(12)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(13)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(14)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(15)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(16)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(17)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(18)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(19)) + "\t" +
                        Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(20)) + "\n");
            }
        }
        fw.flush();
        fw.close();
        con.close();
    }

    private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection con = DriverManager.getConnection(ResourceUtil.getString(FinDataConstants.JDBC_URL), ResourceUtil.getString(FinDataConstants.JDBC_USER), ResourceUtil.getString(FinDataConstants.JDBC_PASS));
        return con;
    }
}
