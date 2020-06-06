package com.skype.skype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCas;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.security.Permission;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.opentok.android.Session.*;

public class VideoCall extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener {


    private static String API_key="46772124";
    private static String SESSION_ID="2_MX40Njc3MjEyNH5-MTU5MTAzNTAwOTE4MH40aS8xSkVPVDk0SDVZSkN3LzRtdU9WR1p-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00Njc3MjEyNCZzaWc9MGFkNzgwMDYwZjc4ODljNzVkZTNjMTdlOWVlMDZlZjY3NmI2NTEzMzpzZXNzaW9uX2lkPTJfTVg0ME5qYzNNakV5Tkg1LU1UVTVNVEF6TlRBd09URTRNSDQwYVM4eFNrVlBWRGswU0RWWlNrTjNMelJ0ZFU5V1IxcC1mZyZjcmVhdGVfdGltZT0xNTkxMDM1MTgzJm5vbmNlPTAuNjU4MTA1MzI1ODMwNDUzNyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkxMDU2NzgxJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=VideoCall.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private ImageView closeVideoBtn;
    private DatabaseReference userRef;
    private String userID="";

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);


        userRef= FirebaseDatabase.getInstance().getReference().child("Users");


        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        closeVideoBtn=findViewById(R.id.close_video_btn);
        closeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            userRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCall.this,Registration.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("CallingActivity"))
                        {
                            userRef.child(userID).child("CallingActivity").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }


                            startActivity(new Intent(VideoCall.this,Registration.class));
                            finish();
                        }
                        else
                            {
                                if (mPublisher != null)
                                {
                                    mPublisher.destroy();
                                }
                                if (mSubscriber != null)
                                {
                                    mSubscriber.destroy();
                                }


                                startActivity(new Intent(VideoCall.this,Registration.class));
                                finish();
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults ,VideoCall.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(VideoCall.this,perms))
        {
            mPublisherViewController=findViewById(R.id.publisher_container);
            mSubscriberViewController=findViewById(R.id.subscriber_container);

            //1.initialize and connect to the Session
            mSession= new Session.Builder(this,API_key,SESSION_ID).build();
            mSession.setSessionListener((SessionListener) VideoCall.this);
            mSession.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(VideoCall.this,"Hey this App needs Mic and Camera , Please allow.",RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //2.Publishing a Stream to the session
    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG,"Session Connected");

        mPublisher=new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCall.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView)mPublisher.getView()).setZOrderOnTop(true);

            mSession.publish(mPublisher);
        }
    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG,"Stream Disconnected");

    }

    //3.Subscribing a Stream to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"Stream Received");

        if (mSubscriber == null)
        {
            mSubscriber=new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"Stream Dropped");

        if (mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG,"Stream Error");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.i(LOG_TAG,"Stream Dropped");

    }
}
