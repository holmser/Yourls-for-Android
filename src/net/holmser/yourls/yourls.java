package net.holmser.yourls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class yourls extends Activity {
	SharedPreferences preferences;    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button myButton = (Button)findViewById(R.id.myButton);
        final Button myShorten = (Button)findViewById(R.id.myShareButton);
        
        Button myShareButton = (Button)findViewById(R.id.myShareButton);
        final EditText myEditText = (EditText)findViewById(R.id.myEditText);
        
        if (getIntent().getStringExtra(Intent.EXTRA_TEXT) != null)
        	myEditText.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        //Shorten URL Button List
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        
        
        myButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View _view){
        		
        		Context context = getApplicationContext();
                  int duration = Toast.LENGTH_SHORT;
                 
                if (myEditText.getText().length() != 0)
                {	
                Toast toast = Toast.makeText(context, "Contacting Holmser.net", duration);
    			toast.show();
    			
    			String mServer = preferences.getString("yourlsServer", "http://holmser.net/i/");
    			String mUsername = preferences.getString("username", "holmser");
    			String mPassword = preferences.getString("password", "*********");
    			if (validateSettings(mServer, mUsername, mPassword, context)){
    				myEditText.setText(CreateUrl(myEditText.getText().toString(), mServer, mUsername, mPassword, null));
    				//Copy text to clipboard
        			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
    			 	clipboard.setText(myEditText.getText().toString());
    			 	//toast = Toast.makeText(context, text, duration);
     				//toast.show();
     			
    			//hide Shorten Button
    			 myButton.getHandler().post(new Runnable() {
    				    public void run() {
    				        myButton.setVisibility(View.GONE);
    				    }
    				});
    			//Show share button
    			 myShorten.getHandler().post(new Runnable() {
 				    public void run() {
 				        myShorten.setVisibility(View.VISIBLE);
 				    }
    			 
 				
    			 });
    			}
        	}else {
            	Toast toast = Toast.makeText(getApplicationContext(), "Please Enter a valid URL", Toast.LENGTH_SHORT);
    			toast.show();
            	}	
                
                
        	}
        
        });
        
       myShareButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View _view){
        		Intent intent = new Intent(Intent.ACTION_SEND);
        		intent.setType("text/plain");
        		intent.putExtra(Intent.EXTRA_TEXT, myEditText.getText().toString());
        		startActivity(Intent.createChooser(intent, getTitle()));
        		
        		
        	}
        });
       
    }


	public static String CreateUrl(String original, String yourlsLocation, String username, String password, String customURL)
	{
		String tinyUrl = null;
		try {
			
			HttpClient client = new DefaultHttpClient();
			String urlTemplate =yourlsLocation+ "yourls-api.php?username="+username+"&password="+
				password+"&action=shorturl&url=%s";
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
						System.out.println("Created Url-"+tinyUrl);
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
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	MenuInflater inflater = getMenuInflater();
	    	inflater.inflate(R.menu.menu, menu);
	    	return true;
	    }
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			// We have only one menu option
			case R.id.preferences:
				// Launch Preference activity
				Intent i = new Intent(yourls.this, Preferences.class);
				startActivity(i);
				// A toast is a view containing a quick little message for the user.
				Toast.makeText(yourls.this,
						"Enter your YOURLS settings here",
						Toast.LENGTH_LONG).show();
				break;

			}
			return true;
		}
	 public boolean validateSettings(String server, String username, String password, Context context){
		 if (server == "" || username == "" || password==""){
			 Toast toast = Toast.makeText(context, "Your settings are invalid, please change them", Toast.LENGTH_LONG);
 			toast.show();
			 //Toast.makeText(yourls.this,
				//		"Your settings are invalid, please change them",
					//	Toast.LENGTH_LONG).show();
			 return false;
		 }else return true;
	 }
	
}


