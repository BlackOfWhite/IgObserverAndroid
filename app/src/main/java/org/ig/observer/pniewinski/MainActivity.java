package org.ig.observer.pniewinski;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

  private ArrayList<String> quotes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.quotes = initQuotes();
    setContentView(org.ig.observer.pniewinski.R.layout.activity_main);
  }

  public void onClick(View v) {
    TextView textView = (TextView) this.findViewById(org.ig.observer.pniewinski.R.id.text_view);
    textView.setText(quotes.get(genNumber(0, quotes.size())));
  }

  private ArrayList<String> initQuotes() {
    ArrayList<String> q = new ArrayList<String>();
    q.add("“The pen that writes your life story must be held in your own hand.” —Irene C. Kassorla");
    q.add("“Every choice before you represents the universe inviting you to remember who you are and what you want.” —Alan Cohen");
    q.add("“Men weary as much of not doing the things they want to do as of doing the things they do not want to do.” —Eric Hoffer");
    q.add("“Is life not a hundred times too short for us to stifle ourselves?” —Friedrich Nietzsche");
    q.add("“You were born an original. Don’t die a copy.” —John Mason");
    q.add("“Enjoy the little things, for one day you may look back and realize they were the big things.” —Robert Brault");
    q.add("“Be ready when opportunity comes.... Luck is when preparation and opportunity meet.” —Roy D. Chapin Jr.");
    q.add("“Sooner or later, those who win are those who think they can.” —Richard Bach");
    q.add("“One important key to success is self-confidence. An important key to self-confidence is preparation.” —Arthur Ashe");
    q.add("“For myself I am an optimist - it does not seem to be much use being anything else.” —Sir Winston Churchill");
    return q;
  }

  private int genNumber(int from, int to) {
    Random r = new Random();
    return r.nextInt(to - from) + from;
  }
}
