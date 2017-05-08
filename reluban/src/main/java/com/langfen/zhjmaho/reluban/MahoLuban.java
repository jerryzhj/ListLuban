package com.langfen.zhjmaho.reluban;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHJMaho on 2017/5/8.
 * Purpose:
 */

public class MahoLuban {

	private static final String TAG = "MahoLuban";
	private static String DEFAULT_DISK_CACHE_DIR = "maholuban_disk_cache";

	private static volatile MahoLuban INSTANCE;

	private final File mCacheDir;

	private static String mCacheName = "";
	private List<File> mFileList;

	private OnMahoCompressListener mOnMahoCompressLister;

	public MahoLuban(File mahoLuban) {
		mCacheDir = mahoLuban;
	}

	// 单例模式
	public static MahoLuban get(Context context) {
		if(INSTANCE == null) INSTANCE = new MahoLuban(MahoLuban.getMahoLuban(context));
		return INSTANCE;
	}

	public MahoLuban setCacheName (String cacheName){
		mCacheName = cacheName;
		return this;
	}

	private static File getMahoLuban(Context context) {
		if(mCacheName.equals("")){
			return getPhotoCacheDir(context,MahoLuban.DEFAULT_DISK_CACHE_DIR);
		}
		return getPhotoCacheDir(context,MahoLuban.mCacheName);
	}

	private static File getPhotoCacheDir(Context context, String cacheName) {
		File cacheDir = context.getCacheDir();
		if (cacheDir != null) {
			File result = new File(cacheDir, cacheName);
			if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
				// File wasn't able to create a directory, or the result exists but not a directory
				return null;
			}

			File noMedia = new File(cacheDir + "/.nomedia");
			if (!noMedia.mkdirs() && (!noMedia.exists() || !noMedia.isDirectory())) {
				return null;
			}

			return result;
		}
		if (Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, "default disk cache dir is null");
		}
		return null;
	}

	public MahoLuban setOnMahoCompressLister(OnMahoCompressListener onMahoCompressLister){
		mOnMahoCompressLister = onMahoCompressLister;
		return this;
	}

	public MahoLuban load(List<File> fileList) {
		mFileList = fileList;
		return this;
	}

	public void start() {
		mOnMahoCompressLister.onStart();
		List<File> compressedFiles = new ArrayList<>();
		try {
			for (File file : mFileList) {
				compressedFiles.add(doCompress(file));
				mOnMahoCompressLister.onFinishOne();
			}
			mOnMahoCompressLister.onSuccess(compressedFiles);
		}catch (Exception e){
			mOnMahoCompressLister.onError(e);
		}
	}

	private File doCompress(File file) {
		String thumb = mCacheDir.getAbsolutePath() + File.separator +
			System.currentTimeMillis()  + ".jpg";

		double size;
		String filePath = file.getAbsolutePath();
		int width = getImageSize(filePath)[0];
		int height = getImageSize(filePath)[1];
		int thumbW = width % 2 == 1 ? width + 1 : width;
		int thumbH = height % 2 == 1 ? height + 1 : height;

		width = thumbW > thumbH ? thumbH : thumbW;
		height = thumbW > thumbH ? thumbW : thumbH;

		double scale = ((double) width / height);

		if (scale <= 1 && scale > 0.5625) {
			if (height < 1664) {
				if (file.length() / 1024 < 150) return file;

				size = (width * height) / Math.pow(1664, 2) * 150;
				size = size < 60 ? 60 : size;
			} else if (height >= 1664 && height < 4990) {
				thumbW = width / 2;
				thumbH = height / 2;
				size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
				size = size < 60 ? 60 : size;
			} else if (height >= 4990 && height < 10240) {
				thumbW = width / 4;
				thumbH = height / 4;
				size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
				size = size < 100 ? 100 : size;
			} else {
				int multiple = height / 1280 == 0 ? 1 : height / 1280;
				thumbW = width / multiple;
				thumbH = height / multiple;
				size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
				size = size < 100 ? 100 : size;
			}
		} else if (scale <= 0.5625 && scale > 0.5) {
			if (height < 1280 && file.length() / 1024 < 200) return file;

			int multiple = height / 1280 == 0 ? 1 : height / 1280;
			thumbW = width / multiple;
			thumbH = height / multiple;
			size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
			size = size < 100 ? 100 : size;
		} else {
			int multiple = (int) Math.ceil(height / (1280.0 / scale));
			thumbW = width / multiple;
			thumbH = height / multiple;
			size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
			size = size < 100 ? 100 : size;
		}

		return compress(filePath, thumb, thumbW, thumbH, 0, (long) size);
	}

	private File compress(String filePath, String thumb, int width, int height, int angle, long size) {
		Bitmap thbBitmap = compress(filePath, width, height);
		return saveImage(thumb, thbBitmap, size);
	}
	private Bitmap compress(String imagePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		int outH = options.outHeight;
		int outW = options.outWidth;
		int inSampleSize = 1;

		if (outH > height || outW > width) {
			int halfH = outH / 2;
			int halfW = outW / 2;

			while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
				inSampleSize *= 2;
			}
		}

		options.inSampleSize = inSampleSize;

		options.inJustDecodeBounds = false;

		int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
		int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

		if (heightRatio > 1 || widthRatio > 1) {
			if (heightRatio > widthRatio) {
				options.inSampleSize = heightRatio;
			} else {
				options.inSampleSize = widthRatio;
			}
		}
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(imagePath, options);
	}
	public int[] getImageSize(String imagePath) {
		int[] res = new int[2];
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inSampleSize = 1;
		BitmapFactory.decodeFile(imagePath, options);

		res[0] = options.outWidth;
		res[1] = options.outHeight;

		return res;
	}

	private File saveImage(String filePath, Bitmap bitmap, long size) {
		checkNotNull(bitmap, TAG + "bitmap cannot be null");

		File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));

		if (!result.exists() && !result.mkdirs()) return null;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int options = 100;
		bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);

		while (stream.toByteArray().length / 1024 > size && options > 6) {
			stream.reset();
			options -= 6;
			bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
		}

		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(stream.toByteArray());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(filePath);
	}

	static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
		if (reference == null) {
			throw new NullPointerException(String.valueOf(errorMessage));
		}
		return reference;
	}
}
