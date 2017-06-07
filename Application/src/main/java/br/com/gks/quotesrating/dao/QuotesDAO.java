package br.com.gks.quotesrating.dao;

import java.util.ArrayList;
import java.util.List;

import br.com.gks.quotesrating.model.Quote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class QuotesDAO {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	public QuotesDAO(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void insertQuote(String quote, String author, int rating) {
		this.open();

		ContentValues values = new ContentValues();
		values.put("quote", quote);
		values.put("author", author);
		values.put("rating", rating);

		database.insert("quotes", null, values);

		this.close();
	}

	public void deleteQuote(int id) {
		this.open();

		database.delete("quotes", "id = " + id, null);

		this.close();
	}

	public void updateQuoteRating(int id, int rating) {
		this.open();

		ContentValues values = new ContentValues();
		values.put("rating", rating);

		database.update("quotes", values, "id = " + id, null);

		this.close();
	}

	public List<Quote> getFavorites() {

		String[] allColumns = { "id", "author", "quote", "rating" };

		this.open();

		List<Quote> favorites = new ArrayList<Quote>();

		Cursor cursor = database.query("quotes", allColumns, null, null, null,
				null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Quote quote = cursorToQuote(cursor);
			favorites.add(quote);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();

		this.close();

		return favorites;
	}

	private Quote cursorToQuote(Cursor cursor) {
		Quote quote = new Quote();
		quote.setId(cursor.getInt(0));
		quote.setAuthor(cursor.getString(1));
		quote.setQuote(cursor.getString(2));
		quote.setRating(cursor.getInt(3));

		return quote;
	}

}