/*
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.
 *
 * Software: BungeePackFix
 * License: Apache 2.0
 * Licensor: LoneDev
 */

/*
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.
 *
 * Software: BungeePackFix
 * License: Apache 2.0
 * Licensor: LoneDev
 */

/*
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.
 *
 * Software: BungeePackFix
 * License: Apache 2.0
 * Licensor: LoneDev
 */
package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.impl.ClientboundResourcePackPacket;
import dev.lone.bungeepackfix.generic.AbstractPlayerPackCache;
import net.md_5.bungee.UserConnection;

public class BungeePlayerPackCache extends AbstractPlayerPackCache
{
    public ClientboundResourcePackPacket cachedPacket;
    public boolean installedSuccessfully;

    public BungeePlayerPackCache(ClientboundResourcePackPacket cachedPacket)
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
