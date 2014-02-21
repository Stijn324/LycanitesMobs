package lycanite.lycanitesmobs;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lycanite.lycanitesmobs.api.IPacketReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
	
	// Packet Types:
    public static enum PacketType {
		NULL((byte)-1), TILEENTITY((byte)0), ENTITY((byte)1), PLAYER((byte)2);
		public byte id;
		private PacketType(byte i) { id = i; }
	}
	
	// Player packet Types:
    public static enum PlayerType {
		CONTROL((byte)0);
		public byte id;
		private PlayerType(byte i) { id = i; }
	}
	
	
    // ==================================================
    //                   Receive Packet
    // ==================================================
	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player) {
		try {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			EntityPlayer playerEntity = (EntityPlayer)player;
			World world = playerEntity.worldObj;
			
			if(packet.channel.equals(LycanitesMobs.modid)) {
				byte packetType = data.readByte();
				
				// ========== Tile Entity packet ==========
				if(packetType == PacketType.TILEENTITY.id) {
					if(world.isRemote) {
						int tileEntityX = data.readInt();
						int tileEntityY = data.readInt();
						int tileEntityZ = data.readInt();
						if(world != null) {
							TileEntity tileEntity = world.getBlockTileEntity(tileEntityX, tileEntityY, tileEntityZ);
							//if((tileEntity instanceof IPacketReceiver)) {
								//((IPacketReceiver)tileEntity).handlePacketData(network, packetType, packet, (EntityPlayer)player, data);
						}
					}
				}
				
				// ========== Entity Packet ==========
				else if(packetType == PacketType.ENTITY.id) {
					if(world.isRemote) {
						Entity entity = world.getEntityByID(data.readInt());
						if(entity instanceof IPacketReceiver)
							((IPacketReceiver)entity).receivePacketData(data);
					}
				}
				
				// ========== Player Packet ==========
				else if(packetType == PacketType.PLAYER.id) {
					if(!world.isRemote) {
						byte playerType = data.readByte();
						if(playerType == PlayerType.CONTROL.id) {
							byte states = data.readByte();
							PlayerControlHandler.updateStates((EntityPlayer)player, states);
						}
					}
				}
			}
		}
		catch(Exception e) {
			System.err.println("[WARNING] [LycanitesMobs] Invalid Packet Type was passed.");
			e.printStackTrace();
		}
	}
    
    
    // ==================================================
    //                   Write NBT
    // ==================================================
    public static void writeNBTTagCompound(NBTTagCompound tag, DataOutputStream stream) throws IOException {
		if (tag == null) {
			stream.writeShort(-1);
		}
		else {
			byte[] var2 = CompressedStreamTools.compress(tag);
			stream.writeShort((short)var2.length);
			stream.write(var2);
		}
    }
    
    
    // ==================================================
    //                   Create Packet
    // ==================================================
    public static Packet createPacket(PacketType packetType, Object... sendData) {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	DataOutputStream data = new DataOutputStream(bytes);
    	
    	try {
    		data.writeByte(packetType.id);
    		data = encodeDataStream(data, sendData);
    		
    		Packet250CustomPayload packet = new Packet250CustomPayload();
    		packet.channel = LycanitesMobs.modid;
    		packet.data = bytes.toByteArray();
    		packet.length = packet.data.length;
    		
    		return packet;
    	}
    	catch(IOException e) {
    		System.out.println("[WARNING] [LycanitesMobs] Failed to create packet.");
    		e.printStackTrace();
    	}
    	return null;
    }
    
    
    // ==================================================
    //                 Encode Data Stream
    // ==================================================
    public static DataOutputStream encodeDataStream(DataOutputStream data, Object[] sendData) {
    	try {
    		for (Object dataValue : sendData) {
    			if((dataValue instanceof Integer))
    				data.writeInt(((Integer)dataValue).intValue());
    			else if((dataValue instanceof Float))
    				data.writeFloat(((Float)dataValue).floatValue());
    			else if((dataValue instanceof Double))
    				data.writeDouble(((Double)dataValue).doubleValue());
    			else if((dataValue instanceof Byte))
    				data.writeByte(((Byte)dataValue).byteValue());
    			else if((dataValue instanceof Boolean))
    				data.writeBoolean(((Boolean)dataValue).booleanValue());
    			else if((dataValue instanceof String))
    				data.writeUTF((String)dataValue);
    			else if((dataValue instanceof Short))
    				data.writeShort(((Short)dataValue).shortValue());
    			else if((dataValue instanceof Long))
    				data.writeLong(((Long)dataValue).longValue());
    			else if((dataValue instanceof NBTTagCompound))
    				writeNBTTagCompound((NBTTagCompound)dataValue, data);
    		}
    		return data;
    	}
    	catch(IOException e) {
    		System.out.println("[MetalMech] Failed to encode Tile Entity packet data.");
    		e.printStackTrace();
    	}
    	return data;
    }
    
    
    // ==================================================
    //               Send packet to Clients
    // ==================================================
    public static void sendPacketToClients(Packet packet, World worldObj) {
    	try {
    		PacketDispatcher.sendPacketToAllInDimension(packet, worldObj.provider.dimensionId);
    	}
    	catch(Exception e) {
    		System.out.println("[WARNING] [LycanitesMobs] Sending packet to client failed.");
    		e.printStackTrace();
    	}
    }
    
    
    // ==================================================
    //               Send packet to Server
    // ==================================================
    public static void sendPacketToServer(Packet packet) {
    	try {
			if(Minecraft.getMinecraft().thePlayer != null) {
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(packet);
			}
    	}
    	catch(Exception e) {
    		System.out.println("[WARNING] [LycanitesMobs] Sending packet to server failed.");
    		e.printStackTrace();
    	}
    }
}