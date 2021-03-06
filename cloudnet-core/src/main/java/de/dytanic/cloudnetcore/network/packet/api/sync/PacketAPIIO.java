/*
 * Copyright (c) Tarek Hosni El Alaoui 2017
 */

package de.dytanic.cloudnetcore.network.packet.api.sync;

import de.dytanic.cloudnet.lib.network.protocol.packet.Packet;
import de.dytanic.cloudnet.lib.network.protocol.packet.PacketInHandler;
import de.dytanic.cloudnet.lib.utility.document.Document;

/**
 * Created by Tareko on 18.08.2017.
 */
public abstract class PacketAPIIO extends PacketInHandler{

    protected abstract Packet getResult(Document value);

}