package michael.findata.util;

import java.io.IOException;
import java.util.Properties;

/**
 * 资源文件工具类
 *
 * @author Michael
 * @version 1.0 2020.10.27
 * @see ResourceUtil
 */
public final class ResourceUtil {
	// 资源文件
	private static Properties resource = null;

	static {
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * 获取资源文件
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
	 * 获取配置信息
	 * 根据key获取key对应的值，当值不存在时返回key
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
	 * 获取整型数据
	 *
	 * @param key
	 * @return
	 */
	public static int getInt(String key) {
		String value = getString(key);
		int intValue = Integer.valueOf(value);
		return intValue;
	}

	/**
	 * 私有构造函数
	 */
	private ResourceUtil() {
	}
}
