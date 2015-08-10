package com.numericalmethod.suanshu.misc.license;

import com.numericalmethod.suanshu.misc.license.verifier.IkvmVerifier;
import com.numericalmethod.suanshu.misc.license.verifier.Version;
import com.numericalmethod.suanshu.zzz.con.prn.aux.nul.F;

import java.io.File;
import java.util.Date;

public final class License {

	private static final Version jdField_goto;

	public static final String DATE_FORMAT_STRING = "yyyy-MM-dd";

	public static final String LICENSE_FILE_PROPERTY = "suanshu.license";

	private static F jdField_new;

	private static LicenseSource jdField_false;

	public static final String[] DEFAULT_LICENSE_FILES = {"numericalmethod.lic", "suanshu.lic"};

	static boolean isDotNetLicensed() {
		return true;
	}

	public static void setLicenseFile(File licenseFile) {
	}

	static boolean isValidated(Package a) {
		return true;
	}

	public static void setLicenseKey(String key) {
	}

	public static String getVersion() {
		return jdField_goto.toString();
	}

	public static String getLicenseLocation() {
		return getLicenseSource().toString();
	}

	static void checkDotNetValidity() {
		if (isDotNetLicensed()) {
			IkvmVerifier a = new IkvmVerifier();

			a.verify();
		}
	}

	static boolean isValidated(Package a1, Version a2) {
		return true;
	}

	static boolean isValidated(Date a) {
		return true;
	}

	static {
		jdField_goto = new Version(3, 1, 3);

		jdField_false = LicenseSource.notLoaded();
	}

	static LicenseSource getLicenseSource() {
		return jdField_false;
	}
}