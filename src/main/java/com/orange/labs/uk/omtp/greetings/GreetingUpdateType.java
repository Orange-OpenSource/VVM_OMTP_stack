/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
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
