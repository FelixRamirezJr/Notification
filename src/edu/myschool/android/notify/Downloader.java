
package edu.myschool.android.notify;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Downloader extends IntentService {
  public static final String ACTION_COMPLETE=
      "edu.myschool.android.notify.action.COMPLETE";
  private static int NOTIFY_ID=1337;
  private static int FOREGROUND_ID=1338;

  public Downloader() {
    super("Downloader");
  }

  @Override
  public void onHandleIntent(Intent i) {
    try {
      String filename=i.getData().getLastPathSegment();

      startForeground(FOREGROUND_ID,
                      buildForegroundNotification(filename));

      File root=
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

      root.mkdirs();

      File output=new File(root, filename);

      if (output.exists()) {
        output.delete();
      }

      URL url=new URL(i.getData().toString());
      HttpURLConnection c=(HttpURLConnection)url.openConnection();
      FileOutputStream fos=new FileOutputStream(output.getPath());
      BufferedOutputStream out=new BufferedOutputStream(fos);

      try {
        InputStream in=c.getInputStream();
        byte[] buffer=new byte[8192];
        int len=0;

        while ((len=in.read(buffer)) >= 0) {
          out.write(buffer, 0, len);
        }

        out.flush();
      }
      finally {
        fos.getFD().sync();
        out.close();
        c.disconnect();
      }

      stopForeground(true);
      raiseNotification(i, output, null);
    }
    catch (IOException e2) {
      stopForeground(true);
      raiseNotification(i, null, e2);
    }
  }

  private void raiseNotification(Intent inbound, File output,
                                 Exception e) {
    NotificationCompat.Builder b=new NotificationCompat.Builder(this);

    b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
     .setWhen(System.currentTimeMillis());

    if (e == null) {
      b.setContentTitle(getString(R.string.download_complete))
       .setContentText(getString(R.string.fun))
       .setSmallIcon(android.R.drawable.stat_sys_download_done)
       .setTicker(getString(R.string.download_complete)).build();

      Intent outbound=new Intent(Intent.ACTION_VIEW);

      outbound.setDataAndType(Uri.fromFile(output), inbound.getType());

      b.setContentIntent(PendingIntent.getActivity(this, 0, outbound, 0));
    }
    else {
      b.setContentTitle(getString(R.string.exception))
       .setContentText(e.getMessage())
       .setSmallIcon(android.R.drawable.stat_notify_error)
       .setTicker(getString(R.string.exception));
    }

    // TO DO

    NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mgr.notify(NOTIFY_ID,b.build());
    

  }

  private Notification buildForegroundNotification(String filename)
  {
    NotificationCompat.Builder b=new NotificationCompat.Builder(this);

    b.setOngoing(true);

    b.setContentTitle(getString(R.string.downloading))
     .setContentText(filename)
     .setSmallIcon(android.R.drawable.stat_sys_download)
     .setTicker(getString(R.string.downloading));

    return(b.build());
  }
}
