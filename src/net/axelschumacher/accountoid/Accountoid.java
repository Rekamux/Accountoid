package net.axelschumacher.accountoid;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Accountoid
 */
public class Accountoid {
	public static final String AUTHORITY = "net.axelschumacher.accountoid";

	// This class cannot be instantiated
	private Accountoid() {
	}
	
	/**
	 * Categories table
	 */
	public static final class Categories implements BaseColumns {
		
		// This class cannot be instantiated
		private Categories() {
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/categories");

		/**
		 * The category name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";
	}
	
	/**
	 * Currencies table
	 */
	public static final class Currencies implements BaseColumns {
		
		// This class cannot be instanciated
		private Currencies() {
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/currencies");

		/**
		 * The currency code
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String CODE = "code";

		/**
		 * The last known value in USD
		 * <P>
		 * Type: FLOAT
		 * </P>
		 */
		public static final String VALUE = "value";
	}

	/**
	 * Accounts table
	 */
	public static final class Accounts implements BaseColumns {
		
		// This class cannot be instantiated
		private Accounts() {
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/accounts");

		/**
		 * The description of the spending
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DESCRIPTION = "description";

		/**
		 * The spending itself
		 * <P>
		 * Type: FLOAT
		 * </P>
		 */
		public static final String SPENDING = "spending";

		/**
		 * The spending Category ID
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String CATEGORY = "category";

		/**
		 * The spending currency id
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String CURRENCY = "currency";
		
		/**
		 * The timestamp of the spending
		 * <P>
		 * Type: INTEGER (long from System.curentTimeMillis())
		 * </P>
		 */
		public static final String DATE = "date";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = DATE + " DESC";
	}
}
