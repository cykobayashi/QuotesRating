package br.com.gks.quotesrating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.gks.quotesrating.dao.QuotesDAO;
import br.com.gks.quotesrating.model.Quote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView authorTextView;
    private TextView quoteTextView;
    private TextView counterTextView;
    private Button mBadButton;
    private Button mSkipButton;
    private Button mGoodButton;
    private Button mRemoveButton;

    private ArrayList<Integer> lasts = new ArrayList<Integer>();

    private QuotesDAO quotesDAO;
    private List<Quote> quotes;
    private Random random;
    private int pos;
    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUI();

        quotesDAO = new QuotesDAO(this);
        quotes = quotesDAO.getFavorites();

        countGreatQuotes();

        random = new Random(System.nanoTime());
        nextButton();

        addQuotes();
        //importCSV();

        System.out.println("create");
    }

    private void loadUI() {

        authorTextView = (TextView) findViewById(R.id.authorTextView);
        quoteTextView = (TextView) findViewById(R.id.quoteTextView);
        counterTextView = (TextView) findViewById(R.id.counterTextView);

        mBadButton = (Button) findViewById(R.id.badButton);
        mBadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                badButton();
            }
        });

        mRemoveButton = (Button) findViewById(R.id.removeButton);
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removeButton();
            }
        });

        mSkipButton = (Button) findViewById(R.id.skipButton);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextButton();
            }
        });

        mGoodButton = (Button) findViewById(R.id.goodButton);
        mGoodButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goodButton();
            }
        });

    }

    private void importCSV() {

        try {
            InputStreamReader is = new InputStreamReader(getAssets().open("quotes.csv"));
            BufferedReader reader = new BufferedReader(is);
            String line;

            int count = 0;

            while ((line = reader.readLine()) != null) {
                String tokens[] = line.split(";");
                quotesDAO.insertQuote(tokens[0], tokens[1], 0);
                count++;
            }

            showToast("Lines: " + count);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);

        loadUI();

        Quote quote = quotes.get(pos);
        authorTextView.setText(quote.getAuthor());
        quoteTextView.setText(quote.getQuote());
    }

    private void addQuotes() {
    }

    private void add(String quote, String author, int rating) {
        quotesDAO.insertQuote(quote, author, rating);
    }

    private void add(String quote, String author) {
        quotesDAO.insertQuote(quote, author, 0);
    }

    private void countGreatQuotes() {
        int greats = 0;
        int total = 0;

        for (Quote quote : quotes) {

            if (quote.getRating() >= 10) {
                greats++;
            }

            total++;
        }

        showToast("Great quotes: " + greats + " Total: " + total);
    }

    private void badButton() {
        Quote quote = quotes.get(pos);
        int id = quote.getId();
        int rating = quote.getRating();

        if (rating > 5) { // 6 ~ 9
            rating = 0;
        }

        quote.setRating(rating - 5);
        quotesDAO.updateQuoteRating(id, quote.getRating());

        if (quote.getRating() <= -10) {
            showToast("Awful quote!");
            quotesDAO.deleteQuote(id);
        }

        nextButton();
    }

    private void removeButton() {
        Quote quote = quotes.get(pos);
        int id = quote.getId();

        showToast("Deleted Forever (but not)...");
        //quotesDAO.deleteQuote(id);
        //quote.setRating(-20);

        nextButton();
    }

    private void showToast(String text) {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    private void goodButton() {
        Quote quote = quotes.get(pos);
        int id = quote.getId();
        int rating = quote.getRating();

        quote.setRating(rating + 5);
        quotesDAO.updateQuoteRating(id, quote.getRating());

        if (quote.getRating() >= 10) {
            showToast("Great quote!");
        }

        nextButton();
    }

    private void nextButton() {
        if (quotes.isEmpty()) {
            return;
        }

        boolean found = false;
        Quote quote = null;
        int id = 0;

        while (found == false) {
            found = true;

            pos = random.nextInt(quotes.size());
            quote = quotes.get(pos);
            id = quote.getId();

            if (lasts.contains(id)) {
                found = false;
            }
            if (quote.getRating() <= -10) {
                found = false;
            }
            if (quote.getRating() >= 10) {
                int rating = quote.getRating();

                // you can see a good quote again
                quote.setRating(rating - 1);
                quotesDAO.updateQuoteRating(id, quote.getRating());

                found = false;
            }
        }

        authorTextView.setText(quote.getAuthor());
        quoteTextView.setText(quote.getQuote());

        count++;
        counterTextView.setText("Count: " + count);

        // Controla a fila
        while (lasts.size() > 40) {
            lasts.remove(0);
        }
        lasts.add(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                writeFileOnExternalStorage();
                Toast.makeText(this, "Quotes exported.", Toast.LENGTH_SHORT).show();
                return true;
        }
        return true;
    }

    public void writeFileOnExternalStorage(){

        // Storage Permissions
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            this.requestPermissions(
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        try{
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File gpxfile = new File(extStorageDirectory, "quotes.csv");
            FileWriter writer = new FileWriter(gpxfile);

            StringBuilder sb = new StringBuilder();

            for (Quote quote : quotes) {
                sb.append("\"");
                sb.append(quote.getQuote());
                sb.append("\"");
                sb.append(";");
                sb.append(quote.getAuthor());
                sb.append(";");
                sb.append(quote.getRating());
                sb.append("\r\n");
            }

            writer.append(sb.toString());
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();

        }
    }

}
