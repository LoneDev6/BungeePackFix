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
package dev.lone.bungeepackfix.generic;

import java.util.List;

public abstract class AbstractSettings<T>
{
    public boolean equal_pack_attributes_hash;
    public boolean equal_pack_attributes_forced;
    public boolean equal_pack_attributes_prompt_message;

    public boolean ignore_hash_in_url;
    public String main_server_name;
    public List<String> ignored_servers;

    public boolean log_debug;
    public boolean log_ignored_respack;
    public boolean log_sent_respack;
    public boolean ignored_pack_msg_enabled;
    public T ignored_pack_msg;
}