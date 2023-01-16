package dev.lone.bungeepackfix.generic;

import org.jetbrains.annotations.Nullable;

public class PackUtility
{
    public static final long DELAY_MS_FAKE_SUCCESS_PACKET = 200L;

    public static String removeHashtag(String url)
    {
        return url.split("#")[0];
    }

    public static String removeHashtag(boolean remove, String url)
    {
        if(remove)
            return url.split("#")[0];
        return url;
    }

    @Nullable
    public static String getUrlHashtag(String url)
    {
        final String[] split = url.split("#");
        if(split.length == 2)
            return split[1];
        return null;
    }
}
