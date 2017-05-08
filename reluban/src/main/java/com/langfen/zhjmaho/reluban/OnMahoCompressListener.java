package com.langfen.zhjmaho.reluban;

import java.io.File;
import java.util.List;

/**
 * Created by ZHJMaho on 2017/5/8.
 * Purpose:
 */

public interface OnMahoCompressListener {
	/**
	 * Fired when the compression is started, override to handle in your own code
	 */
	void onStart();

	/**
	 * Fired when one of the image is Compressed complete.
	 */
	void onFinishOne();
	/**
	 * Fired when a compression returns successfully, override to handle in your own code
	 */
	void onSuccess(List<File> files);

	/**
	 * Fired when a compression fails to complete, override to handle in your own code
	 */
	void onError(Throwable e);
}
