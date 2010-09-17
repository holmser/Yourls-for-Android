package net.holmser.yourls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class yourls extends Activity {
    public boolean gotURL = false;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //initialize button and EditText elements
        final Button myButton = (Button)findViewById(R.id.myButton);
        final EditText myEditText = (EditText)findViewById(R.id.myEditText);
        final EditText CustomURL=(EditText)findByViewId(R.id.CustomURL);
        final Context context = getApplicationContext();
        final CharSequence text = "Short URL Copied to Clipboard";
        final int duration = Toast.LENGTH_SHORT;
        
        //grabs data from Share intent
        
        if (getIntent().getStringExtra(Intent.EXTRA_TEXT) != null)
        	myEditText.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        //Shorten URL Button Listener
        myButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View _view){
        		
        		if(gotURL == false){
        			Toast toast = Toast.makeText(context, "Contacting Holmser.net", duration);
        			toast.show();
        			myEditText.setText(CreateUrl(myEditText.getText().toString(), CustomURL.getText().toString()));
        			myButton.setText("Share URL");
        			toast = Toast.makeText(context, text, duration);
        			toast.show();
        			 ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
        			 clipboard.setText(myEditText.getText().toString());
        		}else if (gotURL == true){
        			Intent intent = new Intent(Intent.ACTION_SEND);
        			intent.setType("text/plain");
        			intent.putExtra(intent.EXTRA_TEXT, myEditText.getText().toString());
        			startActivity(Intent.createChooser(intent, getTitle()));
        		}
        	}
        	
        });
        
       /* shareURL.setOnClickListener(new OnClickListener(){
        	public void onClick(View _view){
        		
        		if(shortURL.getText()!= null){
        			
        		}else shortURL.setText("Please Enter URL");
        		
        	}
        });*/
    }


	private EditText findByViewId(int customurl) {
		// TODO Auto-generated method stub
		return null;
	}


	public static String CreateUrl(String original, String custom)
	{
		String tinyUrl = null;
		String urlTemplate;
		
		try {

			HttpClient client = new DefaultHttpClient();
			if (custom != null){
				urlTemplate = "http://holmser.net/i/yourls-api.php?username=holmser&password=elmoandme&action=shorturl&keyword="+custom+"&url=%s";
				System.out.println(urlTemplate);
			}
			else{
				urlTemplate = "http://holmser.net/i/yourls-api.php?username=holmser&password=elmoandme&action=shorturl&url=%s";
			}
			
			String uri = String.format(urlTemplate, URLEncoder.encode(original));
			
				
			HttpGet request = new HttpGet(uri);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			try {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == HttpStatus.SC_OK)
				{
				// TODO: Support other encodings
					String enc = "utf-8";
					Reader reader = new InputStreamReader(in, enc);
					BufferedReader bufferedReader = new BufferedReader(reader);
					tinyUrl = bufferedReader.readLine();
					if (tinyUrl != null)
					{
						System.out.println("Created Url-"+tinyUrl.substring( tinyUrl.indexOf("<shorturl>")+10, tinyUrl.indexOf("</shorturl>")));
					}
					else {
						throw new IOException("empty response");
					}
				}
				else {
					String errorTemplate = "unexpected response: %d";
					String msg = String.format(errorTemplate, statusCode);
					throw new IOException(msg);
				}
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			tinyUrl="ERROR";
			System.out.println("tiny url error="+e);
	}

		String sub = tinyUrl.substring( tinyUrl.indexOf("<shorturl>")+10, tinyUrl.indexOf("</shorturl>") );
		return sub;
	
	}
	
}

