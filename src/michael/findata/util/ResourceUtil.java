package michael.findata.util;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Michael
 * @version 1.0 2020.10.27
 * @see ResourceUtil
 */
public final class ResourceUtil {
	private static Properties resource = null;

	static {
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	public synchronized static Properties init() throws IOException {
		if (null == resource) {
			resource = new Properties();
			resource.load(ResourceUtil.class.getResourceAsStream(FinDataConstants.BASE_NAME));
		}
		return resource;
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		String value = resource.getProperty(key);

		if (null == value) {
			return key;
		}

		return value;
	}

	/**
	 * @param key
	 * @return
	 */
	public static int getInt(String key) {
		String value = getString(key);
		int intValue = Integer.valueOf(value);
		return intValue;
	}
}
