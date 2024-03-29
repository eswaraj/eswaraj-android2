package com.next.eswaraj.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.next.eswaraj.R;
import com.next.eswaraj.base.BaseActivity;
import com.next.eswaraj.events.RevGeocodeEvent;
import com.next.eswaraj.fragments.GoogleMapFragment;
import com.next.eswaraj.helpers.GoogleAnalyticsTracker;
import com.next.eswaraj.helpers.ReverseGeocodingTask;
import com.next.eswaraj.util.GenericUtil;
import com.next.eswaraj.util.InternetServicesCheckUtil;
import com.next.eswaraj.util.LocationServicesCheckUtil;
import com.next.eswaraj.util.LocationUtil;
import com.next.eswaraj.util.UserSessionUtil;
import com.next.eswaraj.widgets.ProgressTextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback {

    @Inject
    EventBus eventBus;
    @Inject
    LocationUtil locationUtil;
    @Inject
    Context applicationContext;
    @Inject
    UserSessionUtil userSession;
    @Inject
    InternetServicesCheckUtil internetServicesCheckUtil;
    @Inject
    LocationServicesCheckUtil locationServicesCheckUtil;
    @Inject
    GoogleAnalyticsTracker googleAnalyticsTracker;

    private GoogleMapFragment googleMapFragment;
    private Boolean mapReady = false;
    private Location lastLocation;
    private ReverseGeocodingTask reverseGeocodingTask;

    private ImageView complaints;
    private ImageView leaders;
    private ImageView constituency;
    private ImageView profile;
    private ProgressTextView hRevGeocode;
    private ImageView hCreate;
    private LinearLayout sError;
    private Button sClose;

    private Boolean retryRevGeocoding = false;

    private final int REQUEST_MY_COMPLAINTS = 0;
    private final int REQUEST_MY_CONSTITUENCY = 1;
    private final int REQUEST_MY_LEADERS = 2;
    private final int REQUEST_MY_PROFILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        complaints = (ImageView) findViewById(R.id.hComplaints);
        leaders = (ImageView) findViewById(R.id.hLeaders);
        constituency = (ImageView) findViewById(R.id.hConstituency);
        profile = (ImageView) findViewById(R.id.hProfile);
        hRevGeocode = (ProgressTextView) findViewById(R.id.hRevGeocode);
        hCreate = (ImageView) findViewById(R.id.hCreate);

        hRevGeocode.setTextColor(Color.parseColor("#929292"));

        googleMapFragment = (GoogleMapFragment) getSupportFragmentManager().findFragmentById(R.id.hMap);
        googleMapFragment.setContext(this);

        hCreate.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsTracker.trackUIEvent(GoogleAnalyticsTracker.UIAction.CLICK, "Create Complaint");
                if(locationServicesCheckUtil.isServiceAvailable(v.getContext())) {
                    Intent i = new Intent(v.getContext(), SelectAmenityActivity.class);
                    startActivity(i);
                }
                else {
                    googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.NO_SERVICE, "Create Complaint: No Location Service");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                    builder.setMessage("You need location services enabled to use this feature")
                            .setCancelable(false)
                            .setTitle("Location Service needed")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        complaints.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsTracker.trackUIEvent(GoogleAnalyticsTracker.UIAction.CLICK, "My Complaints");
                if(internetServicesCheckUtil.isServiceAvailable(v.getContext())) {
                    if (userSession.isUserLoggedIn(v.getContext())) {
                        //Intent i = new Intent(v.getContext(), UserComplaintsActivity.class);
                        Intent i = new Intent(v.getContext(), UserSnapshotActivity.class);
                        startActivity(i);
                    } else {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Complaints: Not Logged-in");
                        Intent i = new Intent(v.getContext(), LoginActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_COMPLAINTS);
                    }
                }
                else {
                    googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.NO_SERVICE, "My Complaints: No Internet Service");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                    builder.setMessage("You need internet services enabled to use this feature")
                            .setCancelable(false)
                            .setTitle("Internet Connection needed")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        leaders.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsTracker.trackUIEvent(GoogleAnalyticsTracker.UIAction.CLICK, "My Leaders");
                if(internetServicesCheckUtil.isServiceAvailable(v.getContext())) {
                    if (userSession.isUserLoggedIn(v.getContext()) && userSession.isUserLocationKnown()) {
                        Intent i = new Intent(v.getContext(), LeaderListActivity.class);
                        startActivity(i);
                    } else if (userSession.isUserLoggedIn(v.getContext()) && !userSession.isUserLocationKnown()) {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Leaders: Location not marked");
                        Intent i = new Intent(v.getContext(), MarkLocationActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_LEADERS);
                    } else {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Leaders: Not Logged-in");
                        Intent i = new Intent(v.getContext(), LoginActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_LEADERS);
                    }
                }
                else {
                    googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.NO_SERVICE, "My Leaders: No Internet Service");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                    builder.setMessage("You need internet services enabled to use this feature")
                            .setCancelable(false)
                            .setTitle("Internet Connection needed")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        constituency.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsTracker.trackUIEvent(GoogleAnalyticsTracker.UIAction.CLICK, "My Constituency");
                if(internetServicesCheckUtil.isServiceAvailable(v.getContext())) {
                    if(userSession.isUserLoggedIn(v.getContext()) && userSession.isUserLocationKnown()) {
                        Intent i = new Intent(v.getContext(), LocationListActivity.class);
                        startActivity(i);
                    }
                    else if(userSession.isUserLoggedIn(v.getContext()) && !userSession.isUserLocationKnown()) {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Constituency: Location not marked");
                        Intent i = new Intent(v.getContext(), MarkLocationActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_CONSTITUENCY);
                    }
                    else {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Constituency: Not Logged-in");
                        Intent i = new Intent(v.getContext(), LoginActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_CONSTITUENCY);
                    }
                }
                else {
                    googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.NO_SERVICE, "My Constituency: No Internet Service");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                    builder.setMessage("You need internet services enabled to use this feature")
                            .setCancelable(false)
                            .setTitle("Internet Connection needed")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        profile.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsTracker.trackUIEvent(GoogleAnalyticsTracker.UIAction.CLICK, "My Profile");
                if(internetServicesCheckUtil.isServiceAvailable(v.getContext())) {
                    if(userSession.isUserLoggedIn(v.getContext())) {
                        Intent i = new Intent(v.getContext(), MyProfileActivity.class);
                        startActivity(i);
                    }
                    else {
                        googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.ACCESS_DENIED, "My Profile: Not Logged-in");
                        Intent i = new Intent(v.getContext(), LoginActivity.class);
                        i.putExtra("MODE", true);
                        startActivityForResult(i, REQUEST_MY_PROFILE);
                    }
                }
                else {
                    googleAnalyticsTracker.trackAppAction(GoogleAnalyticsTracker.AppAction.NO_SERVICE, "My Profile: No Internet Service");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                    builder.setMessage("You need internet services enabled to use this feature")
                            .setCancelable(false)
                            .setTitle("Internet Connection needed")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);
        locationUtil.subscribe(applicationContext, true);
        mapReady = false;
    }

    @Override
    protected void onStop() {
        locationUtil.unsubscribe();
        eventBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
    }

    public void onEventMainThread(Location location) {
        Double distance;
        Boolean doRevGeoCoding;

        if (mapReady) {
            googleMapFragment.updateMarkerLocation(location.getLatitude(), location.getLongitude());
        }

        if(lastLocation != null) {
            distance = GenericUtil.calculateDistance(location.getLatitude(), location.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
            if (distance > 100) {
                doRevGeoCoding = true;
            }
            else {
                doRevGeoCoding = false;
            }
        }
        else {
            doRevGeoCoding = true;
        }

        if(doRevGeoCoding || retryRevGeocoding) {
            lastLocation = location;
            if(reverseGeocodingTask != null) {
                if(reverseGeocodingTask.getStatus() == AsyncTask.Status.FINISHED) {
                    reverseGeocodingTask = new ReverseGeocodingTask(this, location);
                    reverseGeocodingTask.execute();
                }
            }
            else {
                reverseGeocodingTask = new ReverseGeocodingTask(this, location);
                reverseGeocodingTask.execute();
            }
        }
    }

    public void onEventMainThread(RevGeocodeEvent event) {
        if(event.getSuccess()) {
            hRevGeocode.setActualText(event.getRevGeocodedLocation());
            userSession.setUserRevGeocodedLocation(event.getRevGeocodedFullData());
            retryRevGeocoding = false;
        }
        else {
            retryRevGeocoding = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MY_COMPLAINTS:
                    complaints.performClick();
                    break;
                case REQUEST_MY_CONSTITUENCY:
                    constituency.performClick();
                    break;
                case REQUEST_MY_LEADERS:
                    leaders.performClick();
                    break;
                case REQUEST_MY_PROFILE:
                    profile.performClick();
                    break;
            }
        }
    }
}
