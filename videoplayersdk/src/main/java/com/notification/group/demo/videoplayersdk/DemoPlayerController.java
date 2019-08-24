package com.notification.group.demo.videoplayersdk;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

public class DemoPlayerController
{
    public static DemoPlayerController getInstance()
    {
        return Holder.INSTANCE;
    }

    static class Holder
    {
        public static final DemoPlayerController INSTANCE = new DemoPlayerController();
    }

    /**
     * To direct play Exoplayer with required metadata and launch sdk player activity
     *
     * @param context
     * @param videoUrl      play url
     * @param metaData      type of streming url / not mendatory
     * @param drmKeyHeaders drm schema and licence key
     */
    public void GetPlayer(Context context, String videoUrl, String metaData, HashMap<String, String> drmKeyHeaders)
    {
        Intent i = new Intent(context, VideoPlayerActivity.class);
        i.putExtra("videourl", videoUrl);
        i.putExtra("metadata", metaData);
        i.putExtra("drmkeyheaders", drmKeyHeaders);
        context.startActivity(i);
    }
}
