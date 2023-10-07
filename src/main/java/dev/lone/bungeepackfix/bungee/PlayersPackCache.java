package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.bungee.packets.impl.ClientboundResourcePackPacket;
import dev.lone.bungeepackfix.generic.AbstractPlayersPackCache;
import net.md_5.bungee.UserConnection;

public class PlayersPackCache extends AbstractPlayersPackCache
{
    public ClientboundResourcePackPacket cachedPacket;
    public boolean installedSuccessfully;

    public PlayersPackCache(ClientboundResourcePackPacket cachedPacket)
    {
        this.cachedPacket = cachedPacket;
    }

    public boolean isSamePack(ClientboundResourcePackPacket packet, UserConnection conn, Settings settings)
    {
        // Check if the main server URL changed
        if (conn.getServer().getInfo().getName().equals(settings.main_server_name))
        {
            String urlHashtag = packet.getUrlHashtag();
            if(mainServerUrlHashtag != null)
            {
                // Check if the hashtag in main server URL changed
                if(!mainServerUrlHashtag.equals(urlHashtag))
                {
                    mainServerUrlHashtag = urlHashtag;
                    return false;
                }
            }

            mainServerUrlHashtag = urlHashtag;
        }

        return cachedPacket.isSamePack(
                packet,
                settings.ignore_hash_in_url,
                settings.equal_pack_attributes_hash,
                settings.equal_pack_attributes_forced,
                settings.equal_pack_attributes_prompt_message
        );
    }
}
