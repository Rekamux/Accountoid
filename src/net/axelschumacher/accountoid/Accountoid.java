package net.axelschumacher.accountoid;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Accountoid
 */
public class Accountoid {
	/**
	 * Authority for tables storage
	 */
	public static final String AUTHORITY = "net.axelschumacher.accountoid";

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory.
	 */
	public static final String BASE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.net.axelschumacher.accountoid";

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single item.
	 */
	public static final String BASE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.net.axelschumacher.accountoid";

	/** Intent name of an id */
	public static final String INTENT_ID_NAME = "ID";

	/** Preferences file */
	public static final String PREFS_NAME = "accountoid_preferences";

	/** Timestamp field name */
	public static final String UPDATE_RATES_TIMESTAMP = "timestamp";
	/** Total currency field name */
	public static final String UPDATE_TOTAL_CURRENCY = "currency";

	/** Results code for start activity for result */
	public static final int RESULT_PICK_CATEGORY = 0;
	public static final int RESULT_PICK_CURRENCY = 1;

	// This class cannot be instantiated
	private Accountoid() {
	}

	/**
	 * Possible states for a spending
	 */
	public static enum States {
		F_CASH, F_CARD, CASH, CARD
	}

	public static final States DEFAULT_STATE = States.CARD;

	/**
	 * Categories table
	 */
	public static final class Categories implements BaseColumns {

		// This class cannot be instantiated
		private Categories() {
		}

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * categories.
		 */
		public static final String CONTENT_TYPE = BASE_CONTENT_TYPE
				+ ".categories";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * category.
		 */
		public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE
				+ ".categories";

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

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}

	/**
	 * Currencies table
	 */
	public static final class Currencies implements BaseColumns {

		// This class cannot be instanciated
		private Currencies() {
		}

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * currencies.
		 */
		public static final String CONTENT_TYPE = BASE_CONTENT_TYPE
				+ ".currencies";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * currency.
		 */
		public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE
				+ ".currencies";

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

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}

	/**
	 * Accounts table
	 */
	public static final class Account implements BaseColumns {

		// This class cannot be instantiated
		private Account() {
		}

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * accounts.
		 */
		public static final String CONTENT_TYPE = BASE_CONTENT_TYPE
				+ ".account";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * account.
		 */
		public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE
				+ ".account";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/account");

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
		public static final String AMOUNT = "spending";

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
		 * The spending state id
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String STATE = "state";

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
