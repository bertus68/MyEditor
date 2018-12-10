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
import a.polverini.my.MainActivity.*;
import java.util.regex.*;
import java.math.*;
import android.content.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class MainActivity extends Activity 
{
	public static void DEBUG(String format, Object ... arguments) {
		System.out.printf("DEBUG: "+format, arguments);
	}
	
	public static void ERROR(Exception e) {
		System.out.printf("ERROR: %s %s\n", e.getClass().getSimpleName(), e.getMessage());
	}
	
	private Map<String, Object> keywords = new HashMap<>();
	private Map<String, Object> classes = new HashMap<>();
	
	private void readXML(int id) {
		try {
			Context context = getApplicationContext();
			InputStream in = context.getResources().openRawResource(id);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();
			
			NodeList languages = doc.getElementsByTagName("language");
			for(int i=0; i<languages.getLength(); i++) {
				Node language = languages.item(i);
				if(language.getNodeType() == Node.ELEMENT_NODE) {
					Element languageElement = (Element) language;
					String languageName = languageElement.getAttribute("name");
					DEBUG("language name=%s\n", languageName);
					
				}
				NodeList children = language.getChildNodes();
				for(int j=0; j<children.getLength(); j++) {
					Node child = children.item(j);
					String childName = child.getNodeName();
					switch(childName) {
					case "instruction":
						if(child.getNodeType() == Node.ELEMENT_NODE) {
							Element instructionElement = (Element) child;
							String instructionName = instructionElement.getAttribute("name");
							DEBUG("  instruction name=%s\n", instructionName);
							keywords.put(instructionName, instructionElement);
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			ERROR(e);
		}
	}

	public class Item
	{

		private MainActivity.Item parent;
		
		public Item(Item parent) {
			this.parent = parent;
		}
		
		public Item() {
			
		}
		
		private Properties properties = new Properties();
		
		public Properties getProperties() {
			return properties;
		}
		
		public boolean contains(Object key) {
			return properties.containsKey(key);
		}
		
		public Object get(Object key) {
			return properties.get(key);
		}
		
		public void set(Object key, Object val) {
			properties.put(key, val);
		}
		
		private List<Item> children = new ArrayList<>();
		
		public List<Item> getChildren() {
			return children;
		}
		
	}
	
	private MainActivity.Logger logger;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main();
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
		logger = new Logger((TextView)findViewById(R.id.log));
		System.out.println("MyEditor rc-0.1.3");
		readXML(R.raw.java);
	}

	public void log() {
		setContentView(R.layout.log);
		logger.setText((TextView)findViewById(R.id.log));
		System.out.println("log...");
	}
	
	private static Map<File, String> openFiles = new HashMap<>();
	private static File selectedFile = null;
	
	boolean changed = true;
	
	
	String I = "I";
	String T = "T";
	String C = "C";
	
	public static boolean isNumber(String s){
		try {
			Integer.parseInt(s);
			return true;
		} catch(NumberFormatException e) {}
		try {
			if(s.startsWith("0x")) {
				Long.parseLong(s.substring(2), 16);
				return true;
			}
		} catch (NumberFormatException e) {

		}
		try {
			Float.parseFloat(s);
			return true;
		} catch(NumberFormatException e) {}
		return false;
	}
	
	void parse(final EditText edittext) {
		
		new Thread() {

			@Override
			public void run() {
				try {
					Item iFile;
					iFile = new Item();
					iFile.set("type", "file");

					Item iCurrent = null;
					Item iPackage = null;

					//while(true) 
					{
						//if(changed==true) 
						{
							changed=false;
							//System.out.close();
							//System.out.println("changed...");
							Editable editable = edittext.getText();
							String text = editable.toString();
							Integer start = null;

							for(int i=0; i<text.length(); i++) {
								char c = text.charAt(i);
								switch(c) {
									case ' ':
									case '\t':
									case '\r':
									case '\n':
									case ';':
									case ',':
									case '\"':
									case '%':
									case '$':
									case '\'':
									case '{':
									case '}':
									case '(':
									case ')':
									case '[':
									case ']':
									case '<':
									case '>':
									case '.':
									case '+':
									case '-':
									case '*':
									case '/':
									case '=':
									case '&':
									case '|':
									case ':':
									case '?':
									case '!':
										if(start!=null) {
											String s = text.substring(start, i);
											// System.out.println(""+s);
											if(iCurrent!=null) {
												switch((String)iCurrent.get("type")) {
													case "package":
														iCurrent.set("value", (iCurrent.contains("value")) ? ((String)iCurrent.get("value")+s) : s);
														if(!iCurrent.contains("valueStart")) {
															iCurrent.set("valueStart", start);
														}
														break;
													case "import":
														iCurrent.set("value", (iCurrent.contains("value")) ? ((String)iCurrent.get("value")+s) : s);
														if(!iCurrent.contains("valueStart")) {
															iCurrent.set("valueStart", start);
														}
														break;
													default:
														break;
												}
											} else {
												if(isNumber(s)) {
													highlight(editable, start, i, Color.rgb(180, 0, 0));
												} else if(keywords.containsKey(s)) {
													highlight(editable, start, i, Color.rgb(0, 0, 180));
													switch(s) {
														case "package":
															if(iCurrent==null) {
																iPackage = new Item(iFile);
																iPackage.set("type", "package");
																iCurrent = iPackage;
															}
															break;
														case "import":
															if(iCurrent==null) {
																iCurrent = new Item(iFile);
																iCurrent.set("type", "import");
															}
															break;
														default:
															break;
													}
												} else if(classes.containsKey(s)) {
													highlight(editable, start, i, Color.rgb(180, 90, 0));
												}
											}
											start = null;
										}
										// System.out.println(""+c);
										if(c=='.') {
											if(iCurrent!=null) {
												switch((String)iCurrent.get("type")) {
													case "package":
														iCurrent.set("value", (iCurrent.contains("value")) ? ((String)iCurrent.get("value")+".") : ".");
														break;
													case "import":
														iCurrent.set("value", (iCurrent.contains("value")) ? ((String)iCurrent.get("value")+".") : ".");
														break;
													default:
														break;
												}
											}
											continue;
										}
										if(c=='*') {
											if(iCurrent!=null) {
												switch((String)iCurrent.get("type")) {
													case "import":
														iCurrent.set("value", (iCurrent.contains("value")) ? ((String)iCurrent.get("value")+"*") : "*");
														break;
													default:
														break;
												}
											}
											continue;
										}
										if(c==';') {
											if(iCurrent!=null) {
												switch((String)iCurrent.get("type")) {
													case "package":
														System.out.printf("package \"%s\";\n", iCurrent.get("value"));
														break;
													case "import":
														try {
															String value = (String)iCurrent.get("value");
															//System.out.printf("import \"%s\";\n", value);

															if(value.endsWith(".*")) {
																String packageName = value.substring(0, value.length()-2);
																DEBUG("IMPORT packageName=\"%s\"\n", packageName);
																Package packageToInvestigate = Package.getPackage(packageName);
																
																Integer valueStart = (Integer)iCurrent.get("valueStart");
																highlight(editable, valueStart, valueStart+value.length(), Color.rgb(180, 90, 0));
															} else {
																String className = value;
																// DEBUG("IMPORT className=\"%s\"\n", className);
																Class classToInvestigate = Class.forName(className);
																classes.put(classToInvestigate.getSimpleName(), value);

																Integer valueStart = (Integer)iCurrent.get("valueStart");
																highlight(editable, valueStart, valueStart+value.length(), Color.rgb(180, 90, 0));

															}
														} catch(Exception e) {
															ERROR(e);
														}
														break;
													default:
														break;
												}
												iCurrent = null;
											}
											continue;
										}
										if(c=='"') {
											start = i;
											for(i+=1; i<text.length(); i++) {
												if(text.charAt(i)=='"' && text.charAt(i-1)!='\\') {
													highlight(editable, start, i+1, Color.rgb(180, 0, 0));
													start = null;
													break;
												}
											}
											continue;
										}
										if(c=='/') {
											if((i+1)<text.length()) {
												char c1 = text.charAt(i+1);
												switch(c1) {
													case '*':
														start = i;
														for(i+=2; (i+1)<text.length(); i++) {
															if(text.charAt(i)=='*' && text.charAt(i+1)=='/') {
																highlight(editable, start, i+2, Color.rgb(0, 180, 0));
																start = null;
																break;
															}
														}
														break;
													case '/':
														start = i;
														for(i+=1; i<text.length(); i++) {
															if(text.charAt(i)=='\n') {
																highlight(editable, start, i, Color.rgb(0,180,0));
																start = null;
																break;
															}
														}
														break;
													default:
														break;
												}

											}
											continue;
										}
										break;
									default:
										if(start==null) {
											start = i;
										}
										break;
								}
							}
							try { Thread.sleep(1000); } catch (InterruptedException e) {}
						}
					}
				} catch(Exception e) {
					System.err.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
				}
			}
		}.start();
	}
	
	public void edit() {
		
		setContentView(R.layout.edit);
		
		logger.setText((TextView)findViewById(R.id.log));
		System.out.println("editing...");
		
		final EditText edittext = findViewById(R.id.text);
		edittext.setHorizontallyScrolling(true);
		
		edittext.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
					//System.out.println("beforeTextChanged");
				}

				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
					//System.out.println("onTextChanged");
				}

				@Override
				public void afterTextChanged(Editable editable) {
					//System.out.println("afterTextChanged");
					changed = true;
				}
			
		});
		
		if(selectedFile!=null) {
			edittext.setText(this.openFiles.get(selectedFile));
			parse(edittext);
		}
		
	}
	
	private void highlight(Editable editable, int start, int end, int color) {
		editable.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
					try {
						FileChooser fileChooser = new FileChooser(this);
						fileChooser.setTitle("");
						fileChooser.setFileListener(new FileChooser.Listener() {
								@Override
								public void selected(File file) {
									new Open().execute(file);
								}
							});
						fileChooser.show();
					} catch(Exception e) {
						System.out.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
					}
					return true;
				case R.id.file_save:
					new Save().execute();
					return true;
				case R.id.view_edit:
					edit();
					return true;
				case R.id.view_log:
					log();
					return true;
				case R.id.view_parse:
					
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return true;
	}

	public static class New extends AsyncTask {
		@Override
		protected Object doInBackground(Object[] args) {
			System.out.println("new...");
			return null;
		}
	}

	public static class Open extends AsyncTask {
		
		@Override
		protected Object doInBackground(Object[] args) {
			System.out.println("open...");
			for(Object arg : args) {
				if(arg instanceof File) {
					open((File)arg);
				}
			}
			return null;
		}
		
		@Override
		public void onPostExecute(Object result) {
			
		}
		
		
		public void open(File file) {
			System.out.println(file.getAbsolutePath());
			try {
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while((line = reader.readLine())!=null) {
					sb.append(line+"\n");
				}
				openFiles.put(file, sb.toString());
				selectedFile = file;
			} catch (Exception e) {
				System.out.printf("%s %s\n", e.getClass().getSimpleName(), e.getMessage());
			}
		}
		
	}

	public static class Save extends AsyncTask {
		@Override
		protected Object doInBackground(Object[] args) {
			System.out.println("save...");
			return null;
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

		public interface Listener {
			void selected(File file);
		}

		private Listener listener;

		public FileChooser setFileListener(Listener listener) {
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
								listener.selected(file);
							}
							dialog.dismiss();
						}
					}
				});
			dialog.setContentView(list);
			dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			refresh(Environment.getExternalStorageDirectory());
		}

		public FileChooser setTitle(String title) {
			
			return this;
		}

		public FileChooser show() {
			dialog.show();
			return this;
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
				int n = 0;
				if(dirs != null) {
					n+=dirs.length;
				}
				if(files != null) {
					n+=files.length;
				}
				String[] fileList;
				if (path.getParentFile() == null) {
					fileList = new String[n];
				} else {
					fileList = new String[n+1];
					fileList[i++] = PARENT;
				}
				
				if(dirs != null) {
					Arrays.sort(dirs);
					for (File dir : dirs) { fileList[i++] = dir.getName(); }
				}
				if(files != null) {
					Arrays.sort(files);
					for (File file : files ) { fileList[i++] = file.getName(); }
				}
				
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
		
		public void setText(TextView text){
			this.text = text;
			this.text.setMovementMethod(new ScrollingMovementMethod());
		}
		
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
