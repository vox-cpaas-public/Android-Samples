package com.ca.groupmanagementsample;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.adapaters.ManageGroupAppContactsAdapter;
import com.ca.adapaters.SimpleTextAdapter;
import com.ca.chatsample.R;
import com.ca.utils.Constants;
import com.ca.utils.utils;
import com.ca.wrapper.CSDataProvider;
import com.ca.wrapper.CSGroups;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class ManageGroupActivity extends AppCompatActivity {
	private static String grpid = "";
	private static int is_grp_active = 0;
	static String owner = CSConstants.GROUPADMINSOMEONEELSE;
	private String MyRole = "";
	private String grpname = "";
	private String grpdescription = "";
	private String grppicid = "";

	private ManageGroupAppContactsAdapter mGroupContactsAdapter;
	private ListView mListView;
	private ImageView grpimg;
	private NestedScrollView scrollView;
	private TextView groupcountview;
	private Toolbar toolbar;
	private FloatingActionButton fab;
	private TextView subtitle;
	private CSGroups IAmLiveCoreSendObj = new CSGroups();
	private Button exitordeletegrp;
	private TextView addmember;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_scrolling);
		try {
			AppBarLayout appBarLayout = findViewById(R.id.app_bar);
			fab = findViewById(R.id.fab);
			exitordeletegrp = findViewById(R.id.exit_grp);
			RelativeLayout golive = findViewById(R.id.ivContactItem6);
			grpimg = (ImageView) findViewById(R.id.grpimg);
			groupcountview = (TextView) findViewById(R.id.group_count);
			addmember = (TextView) findViewById(R.id.addmember);
			mListView = (ListView) findViewById(R.id.appcontacts);
			scrollView = (NestedScrollView) findViewById(R.id.scrollView);
			addmember.setVisibility(View.GONE);
			ImageView goLiveTV = (ImageView) findViewById(R.id.ivContactItem5);
			subtitle = (TextView) findViewById(R.id.subtitle);
			toolbar = (Toolbar) findViewById(R.id.toolbar);

			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			// it will get group ID from intent
			grpid = getIntent().getStringExtra("grpid");


			Constants.phoneNumber = CSDataProvider.getLoginID();

			// below logic will get the group admin name with group ID
			Cursor cur = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
			if (cur.getCount() > 0) {
				cur.moveToNext();
				owner = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_ADMIN));
			}
			cur.close();


			appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
				@Override
				public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
					if (verticalOffset == 0) {
						////LOG.info("Offset 0");
						subtitle.setPadding(72, 0, 0, 0);
					} else {
						subtitle.setPadding(112, 0, 0, 0);
						////LOG.info("Offset 1");
					}
				}
			});
			// this method will get the group name and group image
			updateGrpNameAndImage();

			setCountView();


			Cursor cur1 = CSDataProvider.getGroupContactsCursorFilteredByGroupIdandNumber(grpid, Constants.phoneNumber);
			if (cur1.getCount() > 0) {

				cur1.moveToNext();

				MyRole = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_ROLE));

			}

			cur1.close();

			// below logic will get the is group is active or not with group ID
			Cursor cor = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
			if (cor.getCount() > 0) {
				cor.moveToNext();
				is_grp_active = cor.getInt(cor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_IS_ACTIVE));
				System.out.println("TEST IS GROUP ACTIVE MANAGE GROUP:" + is_grp_active);
			}
			cor.close();
			// if group is active means you are the member of group with this EXIT group option will show
			// if group is not active means you are no longer in this group with this DELETE group option will show
			if (is_grp_active == 1) {
				if (MyRole.equals(CSConstants.ADMIN)) {

					MyRole = "SuperAdmin";
					exitordeletegrp.setText("DELETE GROUP");
					addmember.setVisibility(View.VISIBLE);

				} else if (MyRole.equals(CSConstants.MEMBER)) {
					fab.setVisibility(View.GONE);
					addmember.setVisibility(View.GONE);
					exitordeletegrp.setText("EXIT GROUP");
				}
			} else {
				disableUI();
			}

			// toolbar navigation icon click listener
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();

				}
			});

			golive.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						//goLive(grpid);
					} catch (Exception ex) {

					}
				}
			});
			// add member textView click listener
			addmember.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						// by clicking this textView it will open contact screen to add members inb this group
						Intent intentt = new Intent(MainActivity.context, ShowAppContactsMultiSelectActivity.class);
						intentt.putExtra("uiaction", Constants.UIACTION_ADDCONTACTSTOGROUP);
						startActivityForResult(intentt, 891);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			// edit group info button click listener
			fab.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						// by click this button it will open edit group info screen
						Intent intentt = new Intent(MainActivity.context, EditGroupInfoActivity.class);
						intentt.putExtra("grpid", grpid);
						startActivityForResult(intentt, Constants.EDIT_GROUP_INTNET);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			// exist group button click listener
			exitordeletegrp.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						// by clicking this button first it will check user has the member in this group or not
						// if user present in this group it will show options to delete or exit from group
						// if user not present in group it will directly delete the group
						if (is_grp_active == 1) {
							if (MyRole.equals("SuperAdmin")) {
								showalert(0);
							} else {
								showalert(1);
							}
						} else {
							CSDataProvider.deleteGroup(grpid);
							CSDataProvider.deleteGroupContacts(grpid);
							onBackPressed();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			// group contacts list item click listener
			mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					// if the user is active in this group it will show multiple
					if (is_grp_active == 1) {
						Cursor cursor = CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid);
						cursor.moveToPosition(position);
						String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_CONTACT));
						String contactrole = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_ROLE));

						if (cursor.getCount() > 0) {
							if (MyRole.equals("SuperAdmin")) {
								showOptions(0, number, contactrole);
							} else if (MyRole.equals(CSConstants.ADMIN)) {
								showOptions(1, number, contactrole);
							} else if (MyRole.equals(CSConstants.MEMBER)) {
								showOptions(2, number, contactrole);
							}
						}
						cursor.close();
					} else {
						shownomemberalert();
					}


				}
			});
			goLiveTV.requestFocus();


		} catch (Exception ex) {
			utils.logStacktrace(ex);
		}
	}

	/**
	 * This will handle the
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.SELECT_CONTACT_INTENT && resultCode == Activity.RESULT_OK && data != null) {
			List<String> numbers = data.getStringArrayListExtra("contactnumbers");
			IAmLiveCoreSendObj.addMembersToGroup(grpid, numbers);
			numbers.clear();


		} else if (requestCode == Constants.EDIT_GROUP_INTNET) {
			recreate();
		}
	}

	/**
	 * This method will set layout dimensions based on contacts count in group
	 */
	public void setListviewheight() {

		try {
			Cursor cur = CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid);
			int count = cur.getCount();

			LinearLayout.LayoutParams list = (LinearLayout.LayoutParams) mListView.getLayoutParams();
			list.height = count * 187;//like int  200
			mListView.setLayoutParams(list);
			mListView.requestLayout();


			cur.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method will update the group name and group image
	 */
	public void updateGrpNameAndImage() {

		try {
			Cursor cur = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
			if (cur.getCount() > 0) {

				cur.moveToNext();
				grpname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
				grpdescription = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_DESC));
				grppicid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_PROFILE_PIC));
				try {

					getSupportActionBar().setTitle(grpname);
					subtitle.setText(grpdescription);

				} catch (Exception ex) {
					utils.logStacktrace(ex);
				}
				if (grppicid != null && !grppicid.equals("")) {
					new ImageDownloaderTask(grpimg).execute(grppicid);
				}
			}
			cur.close();

		} catch (Exception ex) {
			utils.logStacktrace(ex);
		}
	}

	/**
	 * This method update group name and image with group ID
	 *
	 * @param imageid
	 */
	public void updateGrpNameAndImage1(String imageid) {

		try {
			Cursor cur = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, grpid);
			if (cur.getCount() > 0) {

				cur.moveToNext();
				grpname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));
				grpdescription = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_DESC));
				grppicid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_PROFILE_PIC));
				if (grppicid.equals(imageid)) {

					try {
						getSupportActionBar().setTitle(grpname);
					} catch (Exception ex) {
						utils.logStacktrace(ex);
					}

					if (grppicid != null && !grppicid.equals("")) {
						new ImageDownloaderTask(grpimg).execute(grppicid);
					}

				}
			}
			cur.close();

		} catch (Exception ex) {
			utils.logStacktrace(ex);
		}
	}

	/**
	 * This method will get the total number of members in group
	 */
	public void setCountView() {

		try {
			int groupcontactscount = 0;
			Cursor cr = CSDataProvider.getContactsCursor();
			int totalcount = cr.getCount();
			cr.close();
			Cursor cur2 = CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid);
			groupcontactscount = cur2.getCount();
			cur2.close();
			groupcountview.setText(groupcontactscount + " Participants");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method will update the UI based on SDK events
	 *
	 * @param str
	 */
	public void updateUI(String str) {

		try {
			if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
				// this block will handle network events
			} else if (str.equals(CSEvents.CSGROUPS_EXITGROUP_RESPONSE)) {
				// This block will disable UI if user got exit group response
				disableUI();

			} else if (str.equals(CSEvents.CSGROUPS_DELETEGROUP_RESPONSE)) {

				// this will finish screen once got delete group response
				CSDataProvider.deleteGroup(grpid);
				CSDataProvider.deleteGroupContacts(grpid);
				onBackPressed();

			} else if (str.equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
				// this block will get the all group details from cursor
				is_grp_active = 1;
				mGroupContactsAdapter = new ManageGroupAppContactsAdapter(ManageGroupActivity.this, CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid), 0);
				setListviewheight();
				mListView.setAdapter(mGroupContactsAdapter);
				setCountView();

				Cursor ccr = CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid);
				if (ccr.getCount() > 0) {
					while (ccr.moveToNext()) {
						String number = ccr.getString(ccr.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_CONTACT));
						if (number.equals(Constants.phoneNumber)) {
							String role = ccr.getString(ccr.getColumnIndexOrThrow(CSDbFields.KEY_GROUPCONTACTS_ROLE));
							if (role.equals(CSConstants.ADMIN)) {
								if (fab != null) {
									fab.setVisibility(View.VISIBLE);
								}

								MyRole = "SuperAdmin";
								exitordeletegrp.setText("DELETE GROUP");
								addmember.setVisibility(View.VISIBLE);
							} else {
								if (fab != null) {
									fab.setVisibility(View.GONE);
								}
								addmember.setVisibility(View.GONE);
								exitordeletegrp.setText("EXIT GROUP");
							}
							break;
						}
					}
				}
				ccr.close();
			} else if (str.equals(CSEvents.CSGROUPS_DELETEDFROMGROUP)) {
				// this block will disable UI if user removed from group by Admin
				disableUI();
			} else if (str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
				// this block will handle if any group image change updates
			} else if (str.equals(CSEvents.CSGROUPS_GROUPINFO_UPDATED)) {
				// this block will handle group info change update
			} else if (str.equals("imageanddes")) {

				updateGrpNameAndImage();

			} else if (str.equals(CSEvents.CSGROUPS_GROUPDELETEDTOGROUP)) {


			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This broadcast receiver will receiver all SDK events
	 */
	public class MainActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {

				if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
					// this block will handle network events
					updateUI(CSEvents.CSCLIENT_NETWORKERROR);
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_EXITGROUP_RESPONSE)) {
					// This block will disable UI if user got exit group response
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_EXITGROUP_RESPONSE);
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_DELETEGROUP_RESPONSE)) {
					// this will finish screen once got delete group response
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_DELETEGROUP_RESPONSE);
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
					// this block will get the all group details from cursor
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						if (intent.getStringExtra("groupid").equals(grpid)) {
							updateUI(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
						}
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_DELETEDFROMGROUP)) {
					// this block will disable UI if user removed from group by Admin
					if (intent.getStringExtra("groupid").equals(grpid)) {
						updateUI(CSEvents.CSGROUPS_DELETEDFROMGROUP);
					}
				} else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
					// this block will handle if any group image change updates
					updateGrpNameAndImage1(intent.getStringExtra("imageid"));
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_GROUPINFO_UPDATED)) {
					// this block will handle group info change update
					recreate();
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_ADDADMINSTOGROUP_RESPONSE)) {
					// this block will handle when user becomes admin to this group
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_ADDADMINSTOGROUP_RESPONSE);
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_DELADMINSTOGROUP_RESPONSE)) {
					// this block will handle if user have removed as Admin to group
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_DELADMINSTOGROUP_RESPONSE);
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_GROUPDELETEDTOGROUP)) {
					if (intent != null) {
						if (intent.getStringExtra("groupid").equals(grpid)) {
							//onBackPressed();
							disableUI();
						}
					}

					updateUI(CSEvents.CSGROUPS_GROUPDELETEDTOGROUP);
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_DELETEUSERFROMGROUP_RESPONSE)) {
					// this block will handle if any users deleted from group
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_DELETEUSERFROMGROUP_RESPONSE);
					}
				} else if (intent.getAction().equals(CSEvents.CSGROUPS_ADDMEMBERS_TOGROUP_RESPONSE)) {
					// this block will handle any members added to this group
					if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
						updateUI(CSEvents.CSGROUPS_ADDMEMBERS_TOGROUP_RESPONSE);
						String groupid = intent.getStringExtra("groupid");
						if (groupid.equals(grpid)) {
							//IAmLiveCoreSendObj.pullGroupDetails(grpid);
						}
					} else {
						Toast.makeText(ManageGroupActivity.this, "Add contacts failed", Toast.LENGTH_SHORT).show();

					}
				}


			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

	@Override
	public void onResume() {
		super.onResume();
		try {

			// this will get the contacts list by group ID and show it in ListView
			mGroupContactsAdapter = new ManageGroupAppContactsAdapter(ManageGroupActivity.this, CSDataProvider.getGroupContactsCursorByFilter(CSDbFields.KEY_GROUPCONTACTS_GROUP_ID, grpid), 0);
			setListviewheight();
			mListView.setAdapter(mGroupContactsAdapter);
			// Register the receivers for SDK events
			MainActivityReceiverObj = new MainActivityReceiver();
			IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
			IntentFilter filter1 = new IntentFilter(CSEvents.CSGROUPS_EXITGROUP_RESPONSE);
			IntentFilter filter2 = new IntentFilter(CSEvents.CSGROUPS_DELETEGROUP_RESPONSE);
			IntentFilter filter3 = new IntentFilter(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);
			IntentFilter filter4 = new IntentFilter(CSEvents.CSGROUPS_DELETEDFROMGROUP);
			IntentFilter filter5 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
			IntentFilter filter6 = new IntentFilter(CSEvents.CSGROUPS_GROUPINFO_UPDATED);

			IntentFilter filter7 = new IntentFilter(CSEvents.CSGROUPS_ADDADMINSTOGROUP_RESPONSE);
			//IntentFilter filter8 = new IntentFilter(CSEvents.CSGROUPS_ADDADMINSTOGROUP_RESPONSE);
			IntentFilter filter9 = new IntentFilter(CSEvents.CSGROUPS_GROUPDELETEDTOGROUP);
			IntentFilter filter10 = new IntentFilter(CSEvents.CSGROUPS_DELETEUSERFROMGROUP_RESPONSE);
			IntentFilter filter11 = new IntentFilter(CSEvents.CSGROUPS_ADDMEMBERS_TOGROUP_RESPONSE);
			//IntentFilter filter12 = new IntentFilter(CSEvents.addcontactstogroupfailure);


			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);
			//LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter8);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter9);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter10);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter11);
			//LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj,filter12);

			// This API will request for get Group details with group ID
			IAmLiveCoreSendObj.pullGroupDetails(grpid);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		try {
			// this line will unregister the registered receivers
			LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.exitmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		try {
			// this will close the screen
			finish();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	/**
	 * This method will show multiple options to user while clicking the member name in list view
	 *
	 * @param action
	 * @param number
	 * @param contactrole
	 * @return
	 */
	public boolean showOptions(final int action, final String number, final String contactrole) {
		try {
			final ArrayList<String> grpoptions = new ArrayList<>();

			android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ManageGroupActivity.this);
			successfullyLogin.setTitle("Options");
			successfullyLogin.setCancelable(true);
			grpoptions.clear();

			// based on below action values it will show the options to user
			//0-->superadmin
			//1--->Admin
			//2-->Member

			switch (action) {
				case 0:

					if (contactrole.equals(CSConstants.ADMIN) && !number.equals(Constants.phoneNumber)) {
						grpoptions.add("Remove as Admin");
						grpoptions.add("Remove Member");
					} else if (contactrole.equals(CSConstants.MEMBER) && !number.equals(Constants.phoneNumber)) {
						grpoptions.add("Assign as Admin");
						grpoptions.add("Remove Member");
					} else if (contactrole.equals(CSConstants.ADMIN) && number.equals(Constants.phoneNumber)) {
						grpoptions.add("Exit Group");
					}
					grpoptions.add("View Member Details");
					break;
				case 1:
					if (contactrole.equals(CSConstants.ADMIN)) {
						//grpoptions.add("Remove as Admin");
						//grpoptions.add("Remove Member");
					} else if (contactrole.equals(CSConstants.MEMBER)) {
						//grpoptions.add("Assign as Admin");
						grpoptions.add("Remove Member");
					}

					grpoptions.add("View Member Details");

					break;
				case 2:
					grpoptions.add("View Member Details");
					break;
			}


			SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(MainActivity.context, grpoptions);
			successfullyLogin.setAdapter(simpleTextAdapter,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
											int which) {

							String finalaction = grpoptions.get(which);

							if (finalaction.equals("Assign as Admin")) {
								// this will add user to addmin role
								List<String> numbers = new ArrayList<String>();
								numbers.add(number);
								IAmLiveCoreSendObj.addAdminsToGroup(grpid, numbers);
								numbers.clear();
							} else if (finalaction.equals("Remove as Admin")) {
								// this will remove user as Admin Role
								IAmLiveCoreSendObj.deleteAdminFromGroup(grpid, number);
							} else if (finalaction.equals("Remove Member")) {
								// this will remove member from group
								IAmLiveCoreSendObj.deleteMemberFromGroup(grpid, number);
							} else if (finalaction.equals("View Member Details")) {
								// this will show member details
								showMemberDetails(number);
							} else if (finalaction.equals("Exit Group")) {
								// this will help to exist from group
								IAmLiveCoreSendObj.exitFromGroup(grpid);
							}

						}
					});
			successfullyLogin.show();

			return true;
		} catch (Exception ex) {
			utils.logStacktrace(ex);
			return false;
		}

	}

	/**
	 * This method will show the group member details as PopUp message
	 *
	 * @param number
	 * @return
	 */
	public boolean showMemberDetails(final String number) {
		try {


			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String userName = "";
					String Status = "";
					String Mobile = number;
					AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(ManageGroupActivity.this);

					Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);
					if (cur.getCount() > 0) {
						cur.moveToNext();
						userName = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_USERNAME));
						Status = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
					}
					cur.close();
					if (userName.equals("")) {
						userName = "Not Available";
					}
					if (Status.equals("")) {
						Status = "Not Available";
					}
					if (Mobile.equals("")) {
						Mobile = "Not Available";
					}
					Log.i("ManageGroupActivity","userName:" + userName + "\n" + "Status:" + Status + "\n" + "Mobile:" + Mobile + "\n");
					successfullyLogin.setMessage("userName:" + userName + "\n" + "Status:" + Status + "\n" + "Mobile:" + Mobile + "\n");

					successfullyLogin.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {

								}
							});

					successfullyLogin.show();
				}
			});
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}


	/**
	 * This method will show popup to user for delete and exit group options
	 *
	 * @param action
	 * @return
	 */
	public boolean showalert(final int action) {
		try {
			android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ManageGroupActivity.this);

			successfullyLogin.setCancelable(true);
			switch (action) {
				case 0:
					successfullyLogin.setTitle("Delete Group?");
					break;
				case 1:
					successfullyLogin.setTitle("Exit From Group?");
					break;
			}

			//0-->DeleteGroup
			//1-->ExitFromGroup
			successfullyLogin.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
											int which) {
							switch (action) {
								case 0:
									// if user selects delete it will delete group by below API
									IAmLiveCoreSendObj.deleteGroup(grpid);
									break;
								case 1:
									// if user selects exit it will remove user from group by below API
									IAmLiveCoreSendObj.exitFromGroup(grpid);
									break;
							}
						}
					});

			successfullyLogin.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
											int which) {

						}
					});
			successfullyLogin.show();

			return true;
		} catch (Exception ex) {
			return false;
		}

	}

	/**
	 * This class will downland the group profile image
	 */
	class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public ImageDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			Bitmap photo = null;
			try {

				photo = CSDataProvider.getImageBitmap(params[0]);
				if (photo == null) {
					photo = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.defaultgrouphighres);
				}
			} catch (Exception e) {
				utils.logStacktrace(e);
			}
			return photo;
		}


		@Override
		protected void onPostExecute(Bitmap bitmap) {
			try {

				if (isCancelled()) {
					bitmap = null;
				}
				if (imageViewReference != null) {
					ImageView imageView = imageViewReference.get();
					if (imageView != null) {
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						}
					}
				}

			} catch (Exception ex) {
				utils.logStacktrace(ex);
			}
		}
	}

	/**
	 * This method will disable all options once user removed from group or exited from group
	 */
	public void disableUI() {
		try {
			is_grp_active = 0;
			fab.setVisibility(View.GONE);
			addmember.setVisibility(View.GONE);
			exitordeletegrp.setText("DELETE GROUP");


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method will show popup message to user if user no longer available in group
	 *
	 * @return
	 */
	public boolean shownomemberalert() {
		try {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ManageGroupActivity.this);
					successfullyLogin.setMessage("No longer a member of this group");

					successfullyLogin.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {
								}
							});
					successfullyLogin.show();
				}
			});

			return true;
		} catch (Exception ex) {
			return false;
		}

	}
}
