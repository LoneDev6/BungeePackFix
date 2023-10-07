package dev.lone.bungeepackfix.velocity;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import dev.lone.bungeepackfix.generic.AbstractPlayersPackCache;
import dev.lone.bungeepackfix.generic.PackUtility;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayersPackCache extends AbstractPlayersPackCache
{
    public static HashMap<UUID, PlayersPackCache> playersCache = new HashMap<>();

    public static boolean isSamePack(Settings settings,
                                     ServerConnection connection,
                                     ResourcePackInfo playerPack,
                                     ResourcePackInfo newPack,
                                     boolean ignoreHashtagInUrl,
                                     boolean checkHash,
                                     boolean checkForced,
                                     boolean checkMsg)
    {
        if (playerPack == newPack)
            return true;

        if (newPack == null)
            return false;

        if (connection.getServerInfo().getName().equals(settings.main_server_name))
        {
            String urlHashtag = PackUtility.getUrlHashtag(newPack.getUrl());
            PlayersPackCache playerCache = playersCache.get(connection.getPlayer().getUniqueId());
            if (playerCache != null)
            {
                // Check if the hashtag in main server URL changed
                if (!Objects.equals(playerCache.mainServerUrlHashtag, urlHashtag))
                {
                    playerCache.mainServerUrlHashtag = urlHashtag;
                    return false;
                }
            }
            else
            {
                playerCache = new PlayersPackCache();
                playersCache.put(connection.getPlayer().getUniqueId(), playerCache);
            }

            playerCache.mainServerUrlHashtag = urlHashtag;
        }

        final String newUrl = PackUtility.removeHashtag(ignoreHashtagInUrl, newPack.getUrl());
        final String prevUrl = PackUtility.removeHashtag(ignoreHashtagInUrl, playerPack.getUrl());

        return (Objects.equals(prevUrl, newUrl)) &&
                (!checkHash || Objects.equals(playerPack.getHash(), newPack.getHash())) &&
                (!checkForced || Objects.equals(playerPack.getShouldForce(), newPack.getShouldForce())) &&
                (!checkMsg || Objects.equals(playerPack.getPrompt(), newPack.getPrompt()))
                ;
    }
}
