package com.notification.group.demo.demoexoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.notification.group.demo.videoplayersdk.DemoPlayerController;

import java.util.HashMap;

public class ChooseContentMainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener
{
    private static final String TAG = ChooseContentMainActivity.class.getSimpleName();
    private String _audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3";
    private RadioGroup _radioGroup;
    private Spinner _spinner;
    private boolean _isSpinnerSelectedFirstTime = false;
    int check = 0;

    /**
     * Exoplayer can handle these url formats
     */
    String[] _typeOfStremingUrl = {"Mp4", "Widevine DASH: MP4,H264", "Widevine DASH: WebM,VP9", "Widevine DASH: MP4,H265", "HLS", "Widevine DASH Policy Tests (GTS)", "Widevine HDCP Capabilities Tests", "Misc"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_content_main);

        _radioGroup = findViewById(R.id.audio_video_selector);
        _radioGroup.setOnCheckedChangeListener(this);

        _spinner = findViewById(R.id.track_selector);
        _spinner.setSelected(false);
        _spinner.setSelection(0, true);
        _spinner.setSelection(0, false);
        _spinner.setOnItemSelectedListener(this);
        _spinner.setVisibility(View.GONE);


        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, _typeOfStremingUrl);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        _spinner.setAdapter(aa);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        Log.d(TAG, "onCheckedChanged: " + checkedId + " " + group);
        if (checkedId == R.id.audio)
        {
            /**
             * pass data to video player sdk through single access point sdk controller
             */
            DemoPlayerController.getInstance().GetPlayer(this, _audioUrl, "audio", null);
        }
        else
        {
            _spinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
    {
        Log.d(TAG, "onItemSelected: " + check + " " + position + "  " + id);
        Toast.makeText(getApplicationContext(), _typeOfStremingUrl[position], Toast.LENGTH_SHORT).show();
        if (check >= 1 && _isSpinnerSelectedFirstTime)
        {
            setHeadersOfUrLSelectedToPlayerActivity(_typeOfStremingUrl[position]);
        }
    }

    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();
        check++;
        _isSpinnerSelectedFirstTime = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    /***
     * pass the headers and meta data and url to  sdk to play video ;
     * @param typeOfStremingUrl
     */
    public void setHeadersOfUrLSelectedToPlayerActivity(String typeOfStremingUrl)
    {
        HashMap<String, String> headers = new HashMap<>();
        String videoUrl;
        String metaData;


        switch (typeOfStremingUrl)
        {
            case "Mp4":
                videoUrl = "https://html5demos.com/assets/dizzy.mp4";
                metaData = "mp4";
                headers = null;
                break;
            case "Widevine DASH Policy Tests (GTS)":
                videoUrl = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd";
                metaData = "drm";
                headers.put("drm_scheme", "widevine");
                headers.put("drm_license_url", "https://proxy.uat.widevine.com/proxy?video_id=48fcc369939ac96c&provider=widevine_test");
                break;
            case "Widevine HDCP Capabilities Tests":
                videoUrl = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd";
                metaData = "drm";
                headers.put("drm_scheme", "widevine");
                headers.put("drm_license_url", "https://proxy.uat.widevine.com/proxy?video_id=HDCP_None&provider=widevine_test");
                break;
            case "Widevine DASH: MP4,H264":
                videoUrl = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd";
                metaData = "mpd";
                headers = null;
                break;
            case "Widevine DASH: WebM,VP9":
                videoUrl = "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd";
                metaData = "mpd";
                headers = null;
                break;
            case "Widevine DASH: MP4,H265":
                videoUrl = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd";
                metaData = "mpd";
                headers = null;
                break;
            case "HLS":
                videoUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";
                metaData = "m3u8";
                headers = null;
                break;
            default:
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";
                metaData = "mkv";
                headers = null;
                break;
        }
        /**
         * pass data to video player sdk through single access point sdk controller
         */
        DemoPlayerController.getInstance().GetPlayer(this, videoUrl, metaData, headers);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        _spinner.setVisibility(View.GONE);
        _radioGroup.clearCheck();
        _isSpinnerSelectedFirstTime = false;
    }

}
