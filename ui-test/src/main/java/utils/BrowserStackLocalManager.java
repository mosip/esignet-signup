package utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.browserstack.local.Local;

public class BrowserStackLocalManager {

	private static Local bsLocal;

	public static void start() throws Exception {
		bsLocal = new Local();
		String accessKey = StringUtils.isBlank(EsignetConfigManager.getproperty("browserstack_access_key"))
				? BaseTestUtil.getKeyValueFromYaml("/browserstack.yml", "accessKey")
				: EsignetConfigManager.getproperty("browserstack_access_key");

		Map<String, String> args = new HashMap<>();
		args.put("key", accessKey);
		args.put("forceLocal", "true");
		args.put("onlyAutomate", "true");

		bsLocal.start(args);
		System.out.println("BrowserStack Local started");
	}

	public static void stop() throws Exception {
		if (bsLocal != null) {
			bsLocal.stop();
			System.out.println("BrowserStack Local stopped");
		}
	}
}