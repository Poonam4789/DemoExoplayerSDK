package com.notification.group.demo.videoplayersdk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

public class VideoPlayerActivity extends AppCompatActivity implements Player.EventListener
{
    public static final String TAG = VideoPlayerActivity.class.getSimpleName();

    private SimpleExoPlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private ProgressBar _progressBar;
    private String videoURL;
    private String metaData;
    private HashMap<String, String> drmKeyHeaders;
    private MediaSource mediaSource;

    //uncomment code  to get exoplayer 2.10.3 and PlayerView
/*    private FrameworkMediaDrm mediaDrm;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
    private DataSource.Factory dataSourceFactory;
    private File downloadDirectory;
    private Cache downloadCache;
    private DatabaseProvider databaseProvider;
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        dataSourceFactory = buildDataSourceFactory();

        setContentView(R.layout.video_player);
        exoPlayerView = findViewById(R.id.exo_player_view);
        _progressBar = findViewById(R.id.spinnerVideoDetails);

        try
        {

            if (getIntent() != null)
            {
                videoURL = getIntent().getStringExtra("videourl");
                metaData = getIntent().getStringExtra("metadata");
                drmKeyHeaders = (HashMap<String, String>) getIntent().getSerializableExtra("drmkeyheaders");

                Uri videoURI = Uri.parse(videoURL);

                mediaSource = buildMediaSource(videoURI, metaData, drmKeyHeaders);
            }
        }
        catch (Exception e)
        {
            Log.e("MainAcvtivity", " exoplayer error " + e.toString());
        }

    }

    private void initializePlayer()
    {
        if (exoPlayer == null)
        {
            //  Create a default TrackSelector
            LoadControl loadControl = new DefaultLoadControl(
                    new DefaultAllocator(true, 16),
                    VideoPlayerConfig.MIN_BUFFER_DURATION,
                    VideoPlayerConfig.MAX_BUFFER_DURATION,
                    VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                    VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER, -1, true);

            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(videoTrackSelectionFactory);
            //  Create the player
            exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
            exoPlayer.addListener(this);
            exoPlayerView.setPlayer(exoPlayer);

            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
        }


    }

    /**
     * get MediaSource as per the streaming url and metadata works for exoplayer 2.7.2
     *
     * @param uri
     * @param metaData
     * @param drmKeyHeaders
     * @return
     */
    public MediaSource buildMediaSource(Uri uri, String metaData, HashMap<String, String> drmKeyHeaders)
    {

        Log.d(TAG, "buildMediaSource: " + metaData + " drm " + drmKeyHeaders);
        if (uri.getLastPathSegment().contains("mp3") || metaData.equalsIgnoreCase("mp3") || uri.getLastPathSegment().contains("mp4") || metaData.equalsIgnoreCase("mp4"))
        {
            return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getApplicationContext(), "SampleExoplayer"), new DefaultExtractorsFactory(),
                    null, null);
        }
        else if (uri.getLastPathSegment().contains("m3u8") || metaData.equalsIgnoreCase("hls"))
        {
            return new HlsMediaSource(uri, new DefaultDataSourceFactory(getApplicationContext(), "SampleExoplayer"), null, null);
        }
        else if (uri.getLastPathSegment().contains("mpd") || metaData.equalsIgnoreCase("mpd"))
        {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayer"));
            DashMediaSource dashMediaSource = new DashMediaSource(uri, dataSourceFactory,
                    new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
            return dashMediaSource;
        }
        else
        {
            return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getApplicationContext(), "SampleExoplayer"), new DefaultExtractorsFactory(),
                    null, null);
        }
    }

    private void releasePlayer()
    {
        if (exoPlayer != null)
        {
            exoPlayer.release();
            exoPlayer = null;
            mediaSource = null;
        }
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        releasePlayer();
        setIntent(intent);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (Util.SDK_INT > 23)
        {
            initializePlayer();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (Util.SDK_INT <= 23 || exoPlayer == null)
        {
            initializePlayer();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (Util.SDK_INT <= 23)
        {
            releasePlayer();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (Util.SDK_INT > 23)
        {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        releasePlayer();
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason)
    {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections)
    {

    }

    @Override
    public void onLoadingChanged(boolean isLoading)
    {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
    {
        switch (playbackState)
        {

            case Player.STATE_BUFFERING:
                _progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                _progressBar.setVisibility(View.GONE);
                break;
            case Player.STATE_IDLE:
                _progressBar.setVisibility(View.GONE);
                break;
            case Player.STATE_READY:
                _progressBar.setVisibility(View.GONE);
                break;
            default:
                _progressBar.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode)
    {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled)
    {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error)
    {

    }

    @Override
    public void onPositionDiscontinuity(int reason)
    {

    }


    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters)
    {

    }

    @Override
    public void onSeekProcessed()
    {

    }

    /**
     *  uncomment below code to get MediaSource as per the streaming url and metadata works for exoplayer 2.10.3
     */

//    public MediaSource buildMediaSource(Uri uri, String metaData, HashMap<String, String> drmKeyHeaders)
//    {
//
//        Log.d(TAG, "buildMediaSource: " + metaData + " drm " + drmKeyHeaders);
//        if (uri.getLastPathSegment().contains("mp3")|| metaData.equalsIgnoreCase("mp3") || uri.getLastPathSegment().contains("mp4")|| metaData.equalsIgnoreCase("mp4"))
//        {
//             return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
//        }
//        else if (uri.getLastPathSegment().contains("m3u8")|| metaData.equalsIgnoreCase("hls"))
//        {
//           return  new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
//        }
//        else if (uri.getLastPathSegment().contains("mpd")||metaData.equalsIgnoreCase("mpd"))
//        {
//           DashMediaSource dashMediaSource = new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
//            return dashMediaSource;
//        }
////        else if(metaData.equalsIgnoreCase("drm")){
////            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
////            if (drmKeyHeaders!=null && drmKeyHeaders.get("drm_scheme")!=null && drmKeyHeaders.get("drm_license_url")!=null){
////                String drmLicenseUrl = drmKeyHeaders.get("drm_license_url");
////
////                boolean multiSession = false;
////                int errorStringId = R.string.error_drm_unknown;
////                if (Util.SDK_INT < 18) {
////                    errorStringId = R.string.error_drm_not_supported;
////                } else {
////                    try {
////                        String drmSchemeExtra = drmKeyHeaders.get("drm_scheme");
////                        UUID drmSchemeUuid = Util.getDrmUuid(drmSchemeExtra);
////                        if (drmSchemeUuid == null) {
////                            errorStringId = R.string.error_drm_unsupported_scheme;
////                        } else {
////                            drmSessionManager =
////                                    buildDrmSessionManagerV18(
////                                            drmSchemeUuid, drmLicenseUrl, null, multiSession);
////                        }
////                    } catch (UnsupportedDrmException e) {
////                        errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
////                                ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
////                    }
////                }
////                if (drmSessionManager == null) {
////                    Toast.makeText(getApplicationContext(),errorStringId, Toast.LENGTH_SHORT).show();
////                    finish();
////                }
////            }
////            return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getApplicationContext(), "SampleExoplayer"), new DefaultExtractorsFactory(),
////                    null, null);
//////            return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
////        }
//        else
//        {
//            return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getApplicationContext(), "SampleExoplayer"), new DefaultExtractorsFactory(),
//                    null, null);
//        }
//    }

//    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
//            UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
//            throws UnsupportedDrmException {
//        HttpDataSource.Factory licenseDataSourceFactory =
//                buildHttpDataSourceFactory();
//        HttpMediaDrmCallback drmCallback =
//                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
//        if (keyRequestPropertiesArray != null) {
//            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
//                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
//                        keyRequestPropertiesArray[i + 1]);
//            }
//        }
//        releaseMediaDrm();
//        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
//        return new DefaultDrmSessionManager(uuid, mediaDrm, drmCallback, null,false,-1);
//    }

//    /** Returns a {@link HttpDataSource.Factory}. */
//    public HttpDataSource.Factory buildHttpDataSourceFactory() {
//        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerDemo"));
//    }
//
//    private void releaseMediaDrm() {
//        if (mediaDrm != null) {
//            mediaDrm.release();
//            mediaDrm = null;
//        }
//    }

//    /** Returns a {@link DataSource.Factory}. */
//    public DataSource.Factory buildDataSourceFactory() {
//        DefaultDataSourceFactory upstreamFactory =
//                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerDemo"));
//        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
//    }

//    protected static CacheDataSourceFactory buildReadOnlyCacheDataSource(
//            DataSource.Factory upstreamFactory, Cache cache) {
//        return new CacheDataSourceFactory(
//                cache,
//                upstreamFactory,
//                new FileDataSourceFactory(),
//                /* cacheWriteDataSinkFactory= */ null,
//                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
//                /* eventListener= */ null);
//    }

//    protected synchronized Cache getDownloadCache() {
//        if (downloadCache == null) {
//            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
//            downloadCache =
//                    new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider());
//        }
//        return downloadCache;
//    }

//    private File getDownloadDirectory() {
//        if (downloadDirectory == null) {
//            downloadDirectory = getExternalFilesDir(null);
//            if (downloadDirectory == null) {
//                downloadDirectory = getFilesDir();
//            }
//        }
//        return downloadDirectory;
//    }
//    private DatabaseProvider getDatabaseProvider() {
//        if (databaseProvider == null) {
//            databaseProvider = new ExoDatabaseProvider(this);
//        }
//        return databaseProvider;
//    }

}

