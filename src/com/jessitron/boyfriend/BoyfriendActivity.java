package com.jessitron.boyfriend;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;

public class BoyfriendActivity extends Activity
{
     private static final String LOG_PREFIX = "BoyfriendActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        EditText phoneView = (EditText) findViewById(R.id.phoneNumber);
  phoneView.addTextChangedListener(getTextChangedListener());
    }

    public TextWatcher getTextChangedListener() {
      return new TextWatcher() {
        public void afterTextChanged(Editable s) {
	  say("afterTextChanged " + s.getClass().getName());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	  say("before text changed " + s + count + after);
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
	  say("after text changed " + s + count + before);
        }
      };
    }

    private void say(String s)
    {
      Log.d(LOG_PREFIX, s);
    }

}
