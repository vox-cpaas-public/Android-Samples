package com.ca.clicktocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

public class MyApp extends com.ca.app.App {

	@Override
	public void onCreate() {
		super.onCreate();

		MyReceiver MyReceiverObj = new MyReceiver();

		try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MyReceiverObj);
        } catch (Exception ex) {
			//ex.printStackTrace();
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
		filter.addAction(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
		filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MyReceiverObj, filter);


		System.out.println("connectsdk MyApp onCreate");
		if (Utils.isNetworkAvailable(getApplicationContext())) {
			CSAppDetails csAppDetails = new CSAppDetails(ClickToCallConstants.projectname, ClickToCallConstants.projectid);
			new CSClient().initialize(ClickToCallConstants.server, ClickToCallConstants.port, csAppDetails);
		}
	}

	/**
	 * This broadcast receiver will, catch the events coming from SDK
	 */
	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				System.out.println("connectsdk MyReceiver onReceive");
				if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
					//Toast.makeText(getApplicationContext(), getString(R.string.network_error_message), Toast.LENGTH_SHORT).show();
				}
				else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {

					// if Initialization to server is success call SignUp API and call contacts loading API
					// if initialization to server not success dismiss the progressbar and show the error response to user
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.G729);
                        new CSCall().enableDefaultlocalVideoPreviewUX(true);
                        new CSClient().registerForPSTNCalls();
							// this API will enable contacts loading method in SDK
							//CSClientObj.enableNativeContacts(true, 91);
							// This will call SignUp API to generate OTP

						new CSClient().login(ClickToCallConstants.loginid, ClickToCallConstants.password);

					} else {
						int returnCode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
						if (returnCode == CSConstants.E_409_NOINTERNET) {
							//Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
						} else {
							//Toast.makeText(getApplicationContext(), getString(R.string.initialization_failure_message), Toast.LENGTH_SHORT).show();
						}
					}
				}


				else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
					// if Login is success dismiss the progressbar show message to user
					// if Login failure show the error message to user
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						//Toast.makeText(getApplicationContext(), getString(R.string.login_success_message), Toast.LENGTH_SHORT).show();
					} else {
						//Toast.makeText(getApplicationContext(), getString(R.string.login_failure_message), Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


}