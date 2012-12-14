package michael.findata.external;

public abstract class SecurityShareNumberDatum extends SecuritySnapshotDatum{
	@Override
	public String getName() {
		return "number of shares";
	}
}
