package michael.findata.commandcenter.test;

import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.service.PairStrategyService;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.LocalTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CommandCenterTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");

		cc.setTargetSecurities(stockRepo.findByCodeIn("000568", "601009")); // todo
//		cc.setBroker(new HexinBroker());
		cc.setSzClient(new TDXClient(
				"221.236.13.218:7709",	// ����֤ȯ�ɶ����� - 9-33 ms
				"221.236.13.219:7709",	// ����֤ȯ�ɶ����� - 8-25 ms
				"125.71.28.133:7709",	// cd1010 - 8 ms
				"221.236.15.14:995",	// ����ɶ�����2.1----
				"124.161.97.84:7709",	// ��������ɶ���ͨ2
				"124.161.97.83:7709",	// ��������ɶ���ͨ1
				"125.64.39.62:7709",	// ��������ɶ�����2
				"125.71.28.133:443",	// cd1010 - 8 ms
				"221.236.15.14:7709",	// ����ɶ�����1.13---
				"119.6.204.139:7709",	// ����ɶ���ͨ5.135 -------
				"125.64.39.61:7709",	// ��������ɶ�����1
				"125.64.41.12:7709",	// �ɶ�����54
				"119.4.167.141:7709",	// ����L1
				"119.4.167.142:7709",	// ����L2
				"119.4.167.181:7709",	// ����L3
				"119.4.167.182:7709",	// ����L4
				"119.4.167.164:7709",	// ����L5
				"119.4.167.165:7709",	// ����L6
				"119.4.167.163:7709",	// ����L7
				"119.4.167.175:7709",	// ����L8
				"218.6.198.151:7709",	// ����L1
				"218.6.198.152:7709",	// ����L2
				"218.6.198.174:7709"	// ����L3
		));
		cc.setShClient(new TDXClient(
				"218.6.198.175:7709",	// ����L4----
				"218.6.198.155:7709",	// ����L5
				"218.6.198.156:7709",	// ����L6
				"218.6.198.157:7709",	// ����L7
				"218.6.198.158:7709",	// ����L8
				"182.131.7.141:7709",	// ����E1
				"182.131.7.142:7709",	// ����E2
				"182.131.7.143:7709",	// ����E3
				"182.131.7.144:7709",	// ����E4
				"182.131.7.145:7709",	// ����E5
				"182.131.7.146:7709",	// ����E6
				"182.131.7.147:7709",	// ����E7
				"182.131.7.148:7709",	// ����E8
				"182.131.3.245:7709"	// ��֤������J330 - 9ms)
		));
//		cc.setFirstHalfStart(new LocalTime(9, 28, 10));
		cc.setFirstHalfEnd(new LocalTime(10, 14, 10));
		cc.setSecondHalfStart(new LocalTime(10, 14, 30));
		cc.setSecondHalfEnd(new LocalTime(10, 14, 50));
		cc.start();
	}
}