package michael.findata.external;

public abstract class SecurityTradingDatum extends SecuritySnapshotDatum {
	@Override
	public String getName() {
		return "price";
	}
}
