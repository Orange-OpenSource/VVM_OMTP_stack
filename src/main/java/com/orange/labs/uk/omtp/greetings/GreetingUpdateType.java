package com.orange.labs.uk.omtp.greetings;

/**
 * Enum used to exchange information between source application and OMTP stack
 * about the need of upload or change of active greeting.
 * 
 *  This information is sent from source application to the stack in a form of {@code EnumSet}.
 */
public enum GreetingUpdateType {
	/** it is required to upload and activate this greeting */
	UPLOAD_REQUIRED("upload_required"),
	/** it is required only to change active greeting to a specified */
	ONLY_CHANGE_REQUIRED("only_change_required"), 
	/** fetch content of the greetings is required */
	FETCH_GREETINGS_CONTENT("fetch_greetings_content"),
	/** types of greetings */
	NORMAL("normal"), 
	VOICE_SIGNATURE("voice_signature"), 
	UNKNOWN("unknown");
	
	private final String mType;
	
	private GreetingUpdateType(String type){
		mType = type;
	}
	
	public String getTypeString(){
		return mType;
	}
}
