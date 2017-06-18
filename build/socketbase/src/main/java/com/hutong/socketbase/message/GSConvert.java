package com.hutong.socketbase.message;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class GSConvert {

	public static GeneratedMessage toObject(int opcode, byte[] objectBytes) throws InvalidProtocolBufferException {
		
		GeneratedMessage result = null;
		
		switch (opcode) {
			case InnerOpDefineDefault.GSRegistCode:
				result = GSMessage.GSRegist.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.SGRegistCode:
				result = GSMessage.SGRegist.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.GSPingCode:
				result = GSMessage.GSPing.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.SGPongCode:
				result = GSMessage.SGPong.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.SGPingCode:
				result = GSMessage.SGPing.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.GSPongCode:
				result = GSMessage.GSPong.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.GSPlayerDisconnect:
				result = GSMessage.GSPlayerDisconnect.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.SGPlayerDisconnect:
				result = GSMessage.SGPlayerDisconnect.parseFrom(objectBytes);
				break;
			case InnerOpDefineDefault.SGSceneToSceneMsg:
				result = GSMessage.SGSceneToSceneMsg.parseFrom(objectBytes);
				break;
			default:
				result = null;
				break;
		}
		
		return result;
	}
	
}
