package com.example.testrxandroid;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @author houen.bao
 * @date Aug 1, 2016 9:33:35 AM
 */
public class LoadPngImg {

	public void start(final Context context,final LinearLayout layout) {
		File[] folders = new File[1];
		folders[0] = new File("/sdcard/png");
		if (folders[0].exists()&&folders[0].isDirectory()) {
			from(context,layout,folders);
		}
	}
	
	private void create(final Context context,final LinearLayout layout,File[] folders){
		Observable.create(new Observable.OnSubscribe<String>(){
			@Override
			public void call(Subscriber<? super String> t) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void just(final Context context,final LinearLayout layout,File[] folders){
		Observable.just("").subscribe(new Subscriber<String>(){
			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onError(Throwable e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onNext(String t) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void from(final Context context,final LinearLayout layout,File[] folders){
		Observable.from(folders).flatMap(new Func1<File, Observable<File>>() {
			@Override
			public Observable<File> call(File file) {
				Log.v("tt", "flatmap "+Thread.currentThread().getId());
				return Observable.from(file.listFiles());
			}
		}).filter(new Func1<File, Boolean>() {
			@Override
			public Boolean call(File file) {
				Log.v("tt", "filter "+Thread.currentThread().getId());
				return file.getName().endsWith(".png");
			}
		}).map(new Func1<File, Bitmap>() {
			@Override
			public Bitmap call(File file) {
				Log.v("tt", "map "+Thread.currentThread().getId());
				return getBitmapFromFile(file);
			}
		})
		.subscribeOn(Schedulers.io())
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe(new Action1<Bitmap>() {
			@Override
			public void call(Bitmap bitmap) {
				Log.v("tt", "subscribe() "+Thread.currentThread().getId());
				addImage(context,layout,bitmap);
			}
		});
	}

	private Bitmap getBitmapFromFile(File file) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			options.inJustDecodeBounds = false;
			options.inSampleSize = computeSampleSize(options, -1, 128 * 128); /* 图片长宽方向缩小倍数 */
			// 另外，为了节约内存我们还可以使用下面的几个字段：
			options.inDither = false; /* 不进行图片抖动处理 */
			options.inPreferredConfig = null; /* 设置让解码器以最佳方式解码 */
			/* 下面两个字段需要组合使用 */
			options.inPurgeable = true;
			options.inInputShareable = true;
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = 0;
		int roundedSize = 0;
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
				Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			initialSize = lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			initialSize = 1;
		} else if (minSideLength == -1) {
			initialSize = lowerBound;
		} else {
			initialSize = upperBound;
		}

		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private void addImage(Context context, LinearLayout layout, Bitmap bitmap) {
		Log.v("tt", "add img " + bitmap);
		ImageView img = new ImageView(context);
		img.setAdjustViewBounds(true);
		img.setImageBitmap(bitmap);
		layout.addView(img);
	}

}
