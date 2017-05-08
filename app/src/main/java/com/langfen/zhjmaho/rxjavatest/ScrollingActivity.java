package com.langfen.zhjmaho.rxjavatest;

import android.Manifest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import kr.co.namee.permissiongen.PermissionGen;

public class ScrollingActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scrolling);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		PermissionGen.with(this)
				.addRequestCode(100)
				.permissions(
						Manifest.permission.READ_CONTACTS,
						Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.RECEIVE_SMS,
						Manifest.permission.WRITE_CONTACTS)
				.request();

		CompressImageTask compressImageTast = new CompressImageTask();
		compressImageTast.execute("asasas");
	}

	class CompressImageTask extends AsyncTask<String,Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			dosomeTest();
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			Log.d("ZZZ","at last we can do something now");
		}
	}

	private void dosomeTest() {
		File dir = new File("/sdcard/PixivSionGeter/【保护寒冷中你的脸】口罩特辑/");
		for(File file :dir.listFiles()){
			Log.d("orfileSize:",file.getName()+"ss"+file.length());
		}
		List<File> fileList = Arrays.asList(dir.listFiles());
		final Long currenTime = System.currentTimeMillis();

		MahoLuban.get(this).setCacheName("maho").load(fileList).setOnMahoCompressLister(new MahoLuban.OnMahoCompressListener() {
			@Override
			public void onStart() {
			}
			@Override
			public void onFinishOne(File file) {
				Log.d("ZZZZ","one Time"+(System.currentTimeMillis() - currenTime));
			}
			@Override
			public void onSuccess(List<File> files) {
				Log.d("ZZZZ","total Time"+(System.currentTimeMillis() - currenTime));
				for (File file:files) {
					Log.d("onsuccess",file.getName()+": fileSize:"+file.length()+" filePath:"+file.getAbsolutePath());
				}
			}
			@Override
			public void onError(Throwable e) {
				Log.d("ZZZ",e.toString());
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_scrolling, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
