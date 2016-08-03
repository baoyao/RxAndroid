package com.example.testrxandroid;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private Looper backgroundLooper;
    
    private TextView txt;
    private ImageView imageView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundLooper = backgroundThread.getLooper();

        txt=(TextView) this.findViewById(R.id.txt);
        imageView=(ImageView) this.findViewById(R.id.img);

        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });
        
        new LoadPngImg().start(this, (LinearLayout)findViewById(R.id.ext_img));
    }

    private void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // Run on a background thread
                .subscribeOn(AndroidSchedulers.from(backgroundLooper))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {
                        log("onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                    	log("onError() "+e);
                    }

                    @Override public void onNext(String string) {
                    	log("onNext(" + string + ")");
                    }
                });
    }

    private Observable<String> sampleObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override public Observable<String> call() {
                try {
                    // Do some long running operation
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    private class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
    
    
    public void onButtonClicked(View view){
		toast("start download...");
    	downloadImage("http://avatar.csdn.net/2/F/5/1_eastmoon502136.jpg");
    }
    
	private void downloadImage(final String path) {
		Observable.defer(new Func0<Observable<Bitmap>>() {
			@Override
			public Observable<Bitmap> call() {
				Bitmap bitmap = null;
				InputStream is = null;
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(15 * 1000);
					is = conn.getInputStream();
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						bitmap = BitmapFactory.decodeStream(is);
					}
				} catch (Exception e) {
					throw OnErrorThrowable.from(e);
				} finally {
					try {
						if (is != null) {
							is.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return Observable.just(bitmap);
			}
		}).subscribeOn(AndroidSchedulers.from(backgroundLooper)).observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Bitmap>() {
					@Override
					public void onCompleted() {
						toast("onCompleted");
					}

					@Override
					public void onError(Throwable e) {
						toast("onError "+e.toString());
					}

					@Override
					public void onNext(Bitmap bitmap) {
						toast("onNext");
						if(bitmap!=null){
							imageView.setImageBitmap(bitmap);
						}
					}
				});

	}
	private String recordStr=""; 
	private void log(String str){
		Log.v(TAG, str);
		recordStr+=str+"\n";
		txt.setText(recordStr);
	}
	
	private void toast(String text){
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}