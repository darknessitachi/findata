package michael.findata.service;

public enum EnumTypeCodeListFile {

	THS("THS"), // Tong Hua Shun

	TDX("TDX");  // Tong Da Xin

	private final String text;

	/**
	 * @param text
	 */
	private EnumTypeCodeListFile(final String text) {
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return text;
	}
}