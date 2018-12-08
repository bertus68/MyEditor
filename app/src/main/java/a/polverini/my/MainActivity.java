package a.polverini.my;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import android.widget.RelativeLayout.*;
import java.io.*;
import java.util.*;
import android.content.pm.*;
import android.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        splash();
    }
	
	public void splash() {
		
		setContentView(R.layout.splash);
		
	    new AsyncTask() {
			
			@Override
			protected Object doInBackground(Object[] args) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				return null;
			}
			
			@Override
			public void onPostExecute(Object result) {
				main();
			}
			
		}.execute();
	}
	
	public void main() {
		setContentView(R.layout.log);
		new Logger((TextView)findViewById(R.id.text));
		System.out.println("MyEditor rc-0.1.0");
		isReadStoragePermissionGranted();
		isWriteStoragePermissionGranted();
	}
	
	public void edit() {
		setContentView(R.layout.edit);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
				case R.id.file_new:
					new New().execute();
					return true;
				case R.id.file_open:
					new Open().execute();
					return true;
				case R.id.file_save:
					new Save().execute();
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return true;
	}

	public static class New extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			System.out.println("new...");
			return null;
		}
	}

	public static class Open extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			System.out.println("open...");
			return null;
		}
	}

	public static class Save extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] args)
		{
			System.out.println("save...");
			return null;
		}
	}
	
	/*
	 compile "com.android.support:support-v4:27.0.2"
	 compile "com.android.support:support-v13:27.0.2"
	 compile "com.android.support:cardview-v7:27.0.2"
	 compile "com.android.support:appcompat-v7:27.0.2"
	 compile 'com.android.support:design:27.0.2'
	 
	*/
	public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("READ_EXTERNAL_STORAGE granted");
                return true;
            } else {
                System.out.println("READ_EXTERNAL_STORAGE denied");
              //  ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { 
            System.out.println("READ_EXTERNAL_STORAGE granted");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED) {
                System.out.println("WRITE_EXTERNAL_STORAGE granted");
                return true;
            } else {
                System.out.println("WRITE_EXTERNAL_STORAGE denied");
              //  ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else {
            System.out.println("WRITE_EXTERNAL_STORAGE granted");
            return true;
        }
    }
	
	@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                System.out.printf("WRITE_EXTERNAL_STORAGE %s\n",(grantResults[0]== PackageManager.PERMISSION_GRANTED?"granted":"denied"));
                break;

            case 3:
                System.out.printf("READ_EXTERNAL_STORAGE %s\n",(grantResults[0]== PackageManager.PERMISSION_GRANTED?"granted":"denied"));
                break;
        }
    }
	
	public static class FileChooser {

		private static final String PARENT = "..";

		private final Activity activity;
		private ListView list;
		private Dialog dialog;
		private File currentPath;

		private String extension = null;

		public void setExtension(String extension) {
			this.extension = (extension == null) ? null : extension.toLowerCase();
		}
		
		public interface FileSelectedListener {
			void fileSelected(File file);
		}

		private FileSelectedListener listener;

		public FileChooser setFileListener(FileSelectedListener listener) {
			this.listener = listener;
			return this;
		}

		public FileChooser(Activity activity) {
			this.activity = activity;
			dialog = new Dialog(activity);
			list = new ListView(activity);
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
						File file = getChosenFile( (String) list.getItemAtPosition(which));
						if (file.isDirectory()) {
							refresh(file);
						} else {
							if (listener != null) {
								listener.fileSelected(file);
							}
							dialog.dismiss();
						}
					}
				});
			dialog.setContentView(list);
			dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			refresh(Environment.getExternalStorageDirectory());
		}

		public void showDialog() {
			dialog.show();
		}
		
		private void refresh(File path) {
			this.currentPath = path;
			if (path.exists()) {
				File[] dirs = path.listFiles(new FileFilter() {
						@Override public boolean accept(File file) {
							return (file.isDirectory() && file.canRead());
						}
					});
				File[] files = path.listFiles(new FileFilter() {
						@Override public boolean accept(File file) {
							if (!file.isDirectory()) {
								if (!file.canRead()) {
									return false;
								} else if (extension == null) {
									return true;
								} else {
									return file.getName().toLowerCase().endsWith(extension);
								}
							} else {
								return false;
							}
						}
					});

				int i = 0;
				String[] fileList;
				if (path.getParentFile() == null) {
					fileList = new String[dirs.length + files.length];
				} else {
					fileList = new String[dirs.length + files.length + 1];
					fileList[i++] = PARENT;
				}
				Arrays.sort(dirs);
				Arrays.sort(files);
				for (File dir : dirs) { fileList[i++] = dir.getName(); }
				for (File file : files ) { fileList[i++] = file.getName(); }
				
				dialog.setTitle(currentPath.getPath());
				list.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, fileList) {
						@Override public View getView(int pos, View view, ViewGroup parent) {
							view = super.getView(pos, view, parent);
							((TextView) view).setSingleLine(true);
							return view;
						}
					});
			}
		}
		
		private File getChosenFile(String fileChosen) {
			if (fileChosen.equals(PARENT)) {
				return currentPath.getParentFile();
			} else {
				return new File(currentPath, fileChosen);
			}
		}
	}
	
	public static class Logger extends Handler {

		private static final int CLEAR = 100;
		private static final int PRINT = 101;
		private static final int ERROR = 102;

		private TextView text;

		public Logger(TextView text) {

			super(Looper.getMainLooper());
			this.text = text;
			this.text.setMovementMethod(new ScrollingMovementMethod());
			System.setOut(new PrintStream(System.out) {

					public void close() {
						obtainMessage(CLEAR).sendToTarget();
					}

					@Override
					public void println(String s) {
						obtainMessage(PRINT, s+"\n").sendToTarget();
					}

					@Override
					public PrintStream printf(String f, Object... o) {
						obtainMessage(PRINT, String.format(f, o)).sendToTarget();
						return this;
					}

				});
				System.setErr(new PrintStream(System.out) {

					public void close() {
						obtainMessage(CLEAR).sendToTarget();
					}

					@Override
					public void println(String s) {
						obtainMessage(ERROR, s+"\n").sendToTarget();
					}

					@Override
					public PrintStream printf(String f, Object... o) {
						obtainMessage(ERROR, String.format(f, o)).sendToTarget();
						return this;
					}

				});
			
		}

		@Override
		public void handleMessage(Message message) {
			switch(message.what) {
				case CLEAR:
					text.setText("");
					break;
				case PRINT:
					if(message.obj instanceof String) {
						text.append((String)message.obj);
						Layout layout = text.getLayout();
						if(layout!=null)  {
							int top = layout.getLineTop(text.getLineCount());
							int bottom = text.getBottom();
							if(top>bottom) {
								text.scrollTo(0, top-bottom);
							}
						}
					}
					break;
				case ERROR:
					if(message.obj instanceof String) {
						String s = (String)message.obj;
						text.append(s);
						((Spannable)text.getText()).setSpan(new ForegroundColorSpan(Color.RED), text.length()-s.length(), text.length(), 0);
						Layout layout = text.getLayout();
						if(layout!=null)  {
							int top = layout.getLineTop(text.getLineCount());
							int bottom = text.getBottom();
							if(top>bottom) {
								text.scrollTo(0, top-bottom);
							}
						}
					}
					break;
				default:
					break;
			}
		}
	}
}
