package com.next.eswaraj.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.next.eswaraj.R;
import com.next.eswaraj.base.BaseFragment;
import com.next.eswaraj.events.GetLeaderEvent;
import com.next.eswaraj.events.StartAnotherActivityEvent;
import com.next.eswaraj.middleware.MiddlewareServiceImpl;
import com.next.eswaraj.models.PoliticalBodyAdminDto;
import com.next.eswaraj.widgets.CustomNetworkImageView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;


public class LeaderFragment extends BaseFragment {

    @Inject
    EventBus eventBus;
    @Inject
    MiddlewareServiceImpl middlewareService;

    private CustomNetworkImageView lPhoto;
    private TextView lName;
    private TextView lPost;
    private TextView lAge;
    private TextView lAddress;
    private TextView lParty;
    private TextView lEducation;
    private WebView lDetails;
    private Button lConstituency;

    private PoliticalBodyAdminDto politicalBodyAdminDto;

    public LeaderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus.register(this);
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leader, container, false);
        lPhoto = (CustomNetworkImageView) rootView.findViewById(R.id.lPhoto);
        lName = (TextView) rootView.findViewById(R.id.lName);
        lPost = (TextView) rootView.findViewById(R.id.lPost);
        lDetails = (WebView) rootView.findViewById(R.id.lDetails);
        lAge = (TextView) rootView.findViewById(R.id.lAge);
        lParty = (TextView) rootView.findViewById(R.id.lParty);
        lAddress = (TextView) rootView.findViewById(R.id.lAddress);
        lEducation = (TextView) rootView.findViewById(R.id.lEducation);
        lConstituency = (Button) rootView.findViewById(R.id.lConstituency);

        politicalBodyAdminDto = (PoliticalBodyAdminDto) getActivity().getIntent().getSerializableExtra("LEADER");

        if(politicalBodyAdminDto == null) {
            middlewareService.loadLeaderById(getActivity(), getActivity().getIntent().getLongExtra("ID", 0));
        }
        else {
            setFields();
        }
        lConstituency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartAnotherActivityEvent event = new StartAnotherActivityEvent();
                event.setId(politicalBodyAdminDto.getLocation().getId());
                eventBus.post(event);
            }
        });
        return rootView;
    }

    private void setFields() {
        Picasso.with(getActivity()).load(politicalBodyAdminDto.getProfilePhoto().replace("http", "https")).error(R.drawable.anon).placeholder(R.drawable.anon).into(lPhoto);
        //lPhoto.loadProfileImage(politicalBodyAdminDto.getProfilePhoto(), politicalBodyAdminDto.getId());
        lName.setText(politicalBodyAdminDto.getName());
        lPost.setText(politicalBodyAdminDto.getPoliticalAdminTypeDto().getShortName() + ", " + politicalBodyAdminDto.getLocation().getName());
        lParty.setText(politicalBodyAdminDto.getParty().getName());
        lAddress.setText("");
        lAge.setText("");
        lEducation.setText("");
        lConstituency.setText("Go to " + politicalBodyAdminDto.getLocation().getName());
        if (politicalBodyAdminDto.getBiodata() != null) {
            lDetails.loadData(politicalBodyAdminDto.getBiodata(), "text/html", null);
        }
    }

    public void onEventMainThread(GetLeaderEvent event) {
        if(event.getSuccess()) {
            politicalBodyAdminDto = event.getPoliticalBodyAdminDto();
            setFields();
        }
        else {
            Toast.makeText(getActivity(), "Could not fetch leader details. Error = " + event.getError(), Toast.LENGTH_LONG).show();
        }
    }
}
