

package edu.myschool.android.notify;

import android.app.Activity;
import android.os.Bundle;

public class DownloadActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            new DownloadFragment()).commit();
        }
    }
}

