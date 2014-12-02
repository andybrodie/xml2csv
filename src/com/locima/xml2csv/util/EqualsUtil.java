package com.locima.xml2csv.util;

/**
 * Collected methods which allow easy implementation of <code>equals</code>. Example use case in a class called Car:
 *
 * <pre>
 * public boolean equals(Object aThat) {
 * 	if (this == aThat)
 * 		return true;
 * 	if (!(aThat instanceof Car))
 * 		return false;
 * 	Car that = (Car) aThat;
 * 	return EqualsUtil.areEqual(this.fName, that.fName) &amp;&amp; EqualsUtil.areEqual(this.fNumDoors, that.fNumDoors)
 * 					&amp;&amp; EqualsUtil.areEqual(this.fGasMileage, that.fGasMileage) &amp;&amp; EqualsUtil.areEqual(this.fColor, that.fColor)
 * 					&amp;&amp; Arrays.equals(this.fMaintenanceChecks, that.fMaintenanceChecks); // array!
 * }
 * </pre>
 *
 * <em>Arrays are not handled by this class</em>. This is because the <code>Arrays.equals</code> methods should be used for array fields.
 */
public final class EqualsUtil {

	/**
	 * Determines equality of two booleans.
	 *
	 * @param aThis the first value to compare with the second.
	 * @param aThat the second value to compare with the first.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqual(boolean aThis, boolean aThat) {
		return aThis == aThat;
	}

	/**
	 * Determines equality of two chars.
	 *
	 * @param aThis the first value to compare with the second.
	 * @param aThat the second value to compare with the first.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqual(char aThis, char aThat) {
		return aThis == aThat;
	}

	/**
	 * Determines equality of two doubles.
	 *
	 * @param aThis the first value to compare with the second.
	 * @param aThat the second value to compare with the first.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqual(double aThis, double aThat) {
		return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
	}

	/**
	 * Determines equality of two floats.
	 *
	 * @param aThis the first value to compare with the second.
	 * @param aThat the second value to compare with the first.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqual(float aThis, float aThat) {
		return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
	}

	/**
	 * Determines equality of two longs.
	 *
	 * @param aThis the first value to compare with the second.
	 * @param aThat the second value to compare with the first.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqual(long aThis, long aThat) {
		/*
		 * Implementation Note Note that byte, short, and int are handled by this method, through implicit conversion.
		 */
		return aThis == aThat;
	}

	/**
	 * Determines equality of two objects.
	 *
	 * @param aThis the first object to compare with the second. May be null.
	 * @param aThat the second object to compare with the first. May be null.
	 * @return true if they are equal (including both null), false otherwise.
	 */
	public static boolean areEqual(Object aThis, Object aThat) {
		return aThis == null ? aThat == null : aThis.equals(aThat);
	}

	/**
	 * Prevents instantiation.
	 */
	private EqualsUtil() {
	}
}
