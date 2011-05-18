package michael.findata.external;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: 5/16/11
 * Time: 8:25 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SecurityShareNumberDatum extends SecuritySnapshotDatum{
	@Override
	public String getName() {
		return "number of shares";
	}
}
