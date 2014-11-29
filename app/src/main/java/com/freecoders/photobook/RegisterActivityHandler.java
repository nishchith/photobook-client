package com.freecoders.photobook;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.MultiPartRequest;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RegisterActivityHandler {
	private Context context;
	
	public RegisterActivityHandler(Context context){
		this.context = context;
	}

    public void sendAvatar(){
        File avatarImage = new File(context.getFilesDir(), Constants.FILENAME_AVATAR);
        MultiPartRequest avatarRequest = new MultiPartRequest(Constants.SERVER_URL+"/image", avatarImage,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject obj = new JSONObject( response);
                            String strUrl = obj.getJSONObject("data").getString("url");
                            UserProfile profile = new UserProfile();
                            profile.setNullFields();
                            profile.avatar = Constants.SERVER_URL + strUrl;
                            Preferences pref = new Preferences(context);
                            ServerInterface.updateProfileRequest(context, profile,
                                    pref.strUserID, null, null);
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                        Toast.makeText(context, "Avatar downloaded ",
                                Toast.LENGTH_LONG).show();
                        //((Activity) context).finish();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                    Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(avatarRequest);
    }

	public void doRegister(String strName, String strEmail, final Boolean boolUploadAvatar){
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("name", strName);
		params.put("email", strEmail);
		params.put("phone", "+82 111-2222-3333");
		
		final ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setMessage("Creating account...");
		pDialog.show();   
		
		JsonObjectRequest registerRequest = new JsonObjectRequest(Method.POST,
				Constants.SERVER_URL+"/user", new JSONObject(params),
				new Response.Listener<JSONObject>() {
		 
		                    @Override
		                    public void onResponse(JSONObject response) {
		                        Log.d(Constants.LOG_TAG, response.toString());
		                        pDialog.hide();
		                        String strID = "";
								try {
									String strResult = response.getString("result");
									if (strResult.equals("OK")) {
										String strData = response.getString("data");
										JSONObject obj = new JSONObject(strData);
										strID = obj.getString("id");
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
		                        Toast.makeText(context, "Registration success " + strID,
		                        		   Toast.LENGTH_LONG).show();
		                		Preferences prefs = new Preferences(context);
		                		prefs.strUserID = strID;
		                		prefs.savePreferences();

                                if (boolUploadAvatar) sendAvatar();

		                        ((Activity) context).finish();
		                        
		                    }
		                }, new Response.ErrorListener() {
		 
		                    @Override
		                    public void onErrorResponse(VolleyError error) {
		                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
		                        pDialog.hide();
		                    }
		                }
				);
		VolleySingleton.getInstance(context).addToRequestQueue(registerRequest);
	}

}