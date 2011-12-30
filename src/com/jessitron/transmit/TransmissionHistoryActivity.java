package com.jessitron.transmit;

import com.jessitron.transmit.database.Transmissions;
import com.jessitron.transmit.database.TransmitOpenHelper;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class TransmissionHistoryActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.history_row,
                new Transmissions().retrieveTransmissions(new TransmitOpenHelper(this).getReadableDatabase()),
                new String[] {Transmissions.PICTURE_URI, Transmissions.TRANSMISSION_DATE},
                new int[]   {R.id.imageUri, R.id.transmissionDateText}
        )  ;
        setListAdapter(adapter);
      //  setContentView(R.layout.history_row);
    }
}