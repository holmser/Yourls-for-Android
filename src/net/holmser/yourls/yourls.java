package net.holmser.yourls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;


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
import android.view.inputmethod.InputMethodManager;
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
        		// Hide keyboard
        		InputMethodManager imm = (InputMethodManager)getSystemService(
      			      Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
        		
        		
        		Context context = getApplicationContext();
                  int duration = Toast.LENGTH_SHORT;
                 
                if (myEditText.getText().length() != 0)
                {	
                Toast toast = Toast.makeText(context, "Contacting Website", duration);
    			toast.show();
    			
    			String mServer = preferences.getString("yourlsServer", "http://domain.net/i/");
    			String mPassword = preferences.getString("password", "*********");
    			
    			
    			// TODO: Get hashed signature working
    			//String mTimestamp = String.valueOf(System.currentTimeMillis() / 1000L);
    			//String mSignature = md5digest( mTimestamp + mPassword ); 
    			
    			
    			if (validateSettings(mServer, mPassword, context)){
    				//myEditText.setText(CreateUrl(myEditText.getText().toString(), mServer, mSignature, mTimestamp, null));
    				myEditText.setText(CreateUrl(myEditText.getText().toString(), mServer, mPassword, null));
    				//Copy text to clipboard
        			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
    			 	clipboard.setText(myEditText.getText().toString());
    			 	//toast = Toast.makeText(context, text, duration);
     				//toast.show();
     			
	    			if(! myEditText.getText().toString().equals(""))
	    			{
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

    // Based on: http://l.umrysh.com/86t
    public static String md5digest(String original){
    		MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(original.getBytes());
	    		byte[] digest = md.digest();
	    		StringBuffer sb = new StringBuffer();
	    		for (byte b : digest) {
	    			sb.append(Integer.toHexString((int) (b & 0xff)));
	    		}

	    		return sb.toString();
			} catch (NoSuchAlgorithmException e) {
				//e.printStackTrace();
				return "fail";
			}
    		
    	}

	//public String CreateUrl(String original, String yourlsLocation, String mSignature, String mTimestamp, String customURL)
	public String CreateUrl(String original, String yourlsLocation, String mSignature, String customURL)
	{
		String tinyUrl = null;
		try {
			
			HttpClient client = new DefaultHttpClient();
			//String urlTemplate =yourlsLocation+ "yourls-api.php?timestamp="+mTimestamp+"&signature="+mSignature+"&action=shorturl&url=%s";
			String urlTemplate =yourlsLocation+ "yourls-api.php?signature="+
					mSignature+"&action=shorturl&url=%s";
			String uri = String.format(urlTemplate, URLEncoder.encode(original));
			
			// For Debugging
			//System.out.println("Created Url-"+uri);
			
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
					
					// For Debugging
					//System.out.println("Print Url-"+tinyUrl);
					
					if(tinyUrl.indexOf("<errorCode>") == -1)
					{
						System.out.println("Created Url-"+tinyUrl);
					}else{
						throw new IOException(tinyUrl.substring( tinyUrl.indexOf("<message>")+9, tinyUrl.indexOf("</message>") ));
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
			System.out.println("Error="+e);
			Toast.makeText(this, "Error="+e, Toast.LENGTH_LONG).show();
			return "";
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
			//switch (item.getItemId()) {
			// We have only one menu option
			//case R.id.preferences:
				// Launch Preference activity
				Intent i = new Intent(yourls.this, Preferences.class);
				startActivity(i);
				// A toast is a view containing a quick little message for the user.
				Toast.makeText(yourls.this,
						"Enter your YOURLS settings here",
						Toast.LENGTH_LONG).show();
				//break;

			//}
			return true;
		}
	 public boolean validateSettings(String server, String password, Context context){
		 if (server == "" || password==""){
			 Toast toast = Toast.makeText(context, "Your settings are invalid, please change them", Toast.LENGTH_LONG);
 			toast.show();
			 //Toast.makeText(yourls.this,
				//		"Your settings are invalid, please change them",
					//	Toast.LENGTH_LONG).show();
			 return false;
		 }else return true;
	 }
	
}


