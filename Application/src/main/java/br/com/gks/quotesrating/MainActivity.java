package br.com.gks.quotesrating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.gks.quotesrating.dao.QuotesDAO;
import br.com.gks.quotesrating.model.Quote;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView quoteTextView;
    private TextView authorTextView;
    private Button mBadButton;
    private Button mSkipButton;
    private Button mGoodButton;
    private Button mRemoveButton;

    private ArrayList<Integer> lasts = new ArrayList<Integer>();

    private QuotesDAO quotesDAO;
    private List<Quote> quotes;
    private Random random;
    private int pos;

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

        System.out.println("create");
    }

    private void loadUI() {

        authorTextView = (TextView) findViewById(R.id.authorTextView);
        quoteTextView = (TextView) findViewById(R.id.quoteTextView);

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
        int count = 0;
        int total = 0;

        for (Quote quote : quotes) {

            if (quote.getRating() >= 10) {
                count++;
            }

            total++;
        }

        showToast("Great quotes: " + count + " Total: " + total);
    }

    private void badButton() {
        Quote quote = quotes.get(pos);
        int id = quote.getId();
        int rating = quote.getRating();

        if (rating <= -10) {
            showToast("Awful quote!");
            quotesDAO.deleteQuote(id);
        }

        if (rating > 5) {
            rating = 0;
        }

        quote.setRating(rating - 5);
        quotesDAO.updateQuoteRating(id, quote.getRating());

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

        if (rating >= 10) {
            showToast("Great quote!");
        }

        quote.setRating(rating + 5);
        quotesDAO.updateQuoteRating(id, quote.getRating());

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

}
