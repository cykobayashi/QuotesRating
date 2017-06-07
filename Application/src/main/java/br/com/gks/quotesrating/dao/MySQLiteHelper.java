package br.com.gks.quotesrating.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "quotesrating.db";
	private static final int DATABASE_VERSION = 1;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		StringBuilder sql = new StringBuilder();
		sql.append("create table ");
		sql.append("quotes ");
		sql.append("(");
		sql.append("id        integer primary key autoincrement, ");
		sql.append("author    text not null,    ");
		sql.append("quote     text not null,    ");
		sql.append("rating    integer not null);");

		database.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}