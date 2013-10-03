/*
 * Copyright (C) 2011 Google Inc. All Rights Reserved.
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
package com.orange.labs.uk.omtp.imap;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import android.content.Context;
import android.util.Base64;

import com.android.email.Email;
import com.android.email.mail.Address;
import com.android.email.mail.Body;
import com.android.email.mail.BodyPart;
import com.android.email.mail.FetchProfile;
import com.android.email.mail.Flag;
import com.android.email.mail.Folder;
import com.android.email.mail.Folder.OpenMode;
import com.android.email.mail.Message;
import com.android.email.mail.MessagingException;
import com.android.email.mail.Multipart;
import com.android.email.mail.Store;
import com.android.email.mail.internet.MimeMessage;
import com.android.email.mail.store.ImapStore;
import com.android.email.mail.store.imap.ImapConstants;
import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcher;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingCreator;
import com.orange.labs.uk.omtp.greetings.GreetingCreatorImpl;
import com.orange.labs.uk.omtp.greetings.GreetingImpl;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.DataChannelNotification;
import com.orange.labs.uk.omtp.notification.QuotaNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.proxies.FolderDelegate;
import com.orange.labs.uk.omtp.proxies.FolderProxy;
import com.orange.labs.uk.omtp.utils.CloseUtils;
import com.orange.labs.uk.omtp.utils.NetworkManager;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayload;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayloadImpl;

/**
 * One-shot synchronous fetcher for voicemail from an IMAP server.
 * <p>
 * A one-shot class, construct this and then call either {@link #fetchAllVoicemails(Callback)} or
 * {@link #fetchVoicemailPayload(String, Callback)}. Subsequent calls to
 * {@link #fetchAllVoicemails(Callback)} or {@link #fetchVoicemailPayload(String, Callback)} will
 * immediately fail.
 */
@ThreadSafe
/* package */class OneshotSyncImapVoicemailFetcher implements VoicemailFetcher {
	private static final Logger logger = Logger.getLogger(OneshotSyncImapVoicemailFetcher.class);

	private final Context mContext;
	private final OmtpAccountInfo mAccountDetails;

	private final AtomicBoolean mStarted;
	private final AtomicBoolean mFinished;

	private volatile FolderProxy mFolder;

	private boolean mExpunge;
	private SourceNotifier mNotifier;

	/** Used to determine current network status */
	private final NetworkManager mNetworkManager;
	
	/**
	 * Duration of the message is fixed to 22s instead to be null during initial insertions in order
	 * to try to overcome an issue on Samsung s4 device.
	 */
	private long FIXED_DURATION_FOR_RETREIVED_MESSAGES = 22;

	public OneshotSyncImapVoicemailFetcher(final Context context,
			final OmtpAccountInfo accountDetails, final SourceNotifier notifier) {
		mContext = context;
		mAccountDetails = accountDetails;
		mNotifier = notifier;
		mNetworkManager = new NetworkManager(context);

		mStarted = new AtomicBoolean(false);
		mFinished = new AtomicBoolean(false);
		mExpunge = false;
		mFolder = null;
	}

	@Override
	public void fetchAllVoicemails(final Callback<List<Voicemail>> callback) {
		executeFetchWithFolder(new Callable<Void>() {
			@Override
			public Void call() throws MessagingException {
				checkFolderQuota();

				// Now retrieve the voicemails.
				List<Voicemail> voicemails = new ArrayList<Voicemail>();
				Message[] messages = mFolder.getMessages(null);
				for (Message message : messages) {
					Voicemail voicemail = fetchVoicemail(message, callback);
					if (voicemail != null) {
						voicemails.add(voicemail);
					}
				}
				if (!mFinished.getAndSet(true)) {
					callback.onSuccess(voicemails);
				}
				return null;
			}
		}, callback, StackStaticConfiguration.INBOX_FOLDER_NAME);
	}

	@Override
	public void fetchVoicemailPayload(final String uid, final Callback<VoicemailPayload> callback) {
		executeFetchWithFolder(new Callable<Void>() {
			@Override
			public Void call() throws MessagingException {
				checkFolderQuota();
				logger.d("About to execute getMessage() in fetchVoicemailPayload");
				Message message = mFolder.getMessage(uid);
				VoicemailPayload voicemailPayload = fetchVoicemailPayload(message, callback);
				if (!mFinished.getAndSet(true)) {
					callback.onSuccess(voicemailPayload);
				}
				return null;
			}

		}, callback, StackStaticConfiguration.INBOX_FOLDER_NAME);
	}
	
	private void checkFolderQuota() throws MessagingException {
		int quota = mFolder.getQuotaRootInformation();
		mNotifier.sendNotification(QuotaNotification.quotaValue(quota));
	}

	/** The caller thread will block until the method returns. */
	@Override
	public void markVoicemailsAsRead(Callback<Void> callback, Voicemail... voicemails) {
		setFlags(voicemails, callback, Flag.SEEN);
	}

	/** The caller thread will block until the method returns. */
	@Override
	public void markVoicemailsAsDeleted(Callback<Void> callback, Voicemail... voicemails) {
		mExpunge = true; // Expunge is required after a deletion.
		setFlags(voicemails, callback, Flag.DELETED);
	}

	private void setFlags(final Voicemail[] voicemails, final Callback<Void> callback,
			final Flag... flags) {

		if (voicemails.length == 0) {
			callback.onFailure(new IllegalArgumentException("No voicemails to apply operation on."));
		}

		executeWithFolder(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				mFolder.setFlags(convertToImapMessages(voicemails), flags, true);

				if (!mFinished.getAndSet(true)) {
					callback.onSuccess(null);
				}

				return null;
			}

		}, callback, OpenMode.READ_WRITE, StackStaticConfiguration.INBOX_FOLDER_NAME);
	}

	/**
	 * Execute a fetch operation with the folder. These operations are in READ
	 * ONLY mode and do not require an expunge (no writing operations involved).
	 * 
	 * @param callable
	 *            the code to run while the folder is open
	 * @param failureCallback
	 *            the callback to notify the first time a failure occurs
	 * @param folderName
	 *            name of the folder used to retrieve IMAP messages (usually
	 *            inbox or greetings)
	 */
	private void executeFetchWithFolder(Callable<Void> callable, Callback<?> failureCallback,
			String folderName) {
		executeWithFolder(callable, failureCallback, OpenMode.READ_ONLY, folderName);
	}

	/**
	 * Executes the given runnable while the given folder (inbox/greetings) is open. This method
	 * also has the responsibility to notify of the success or failure of the
	 * connection to the OMTP IMAP platform through the {@link SourceNotifier}
	 * provided through the constructor.
	 * 
	 * <p>
	 * It takes care of handling failures with opening the folder and closing
	 * the folder after the operation completed.
	 * 
	 * @param callable
	 *            the code to run while the folder is open
	 * @param failureCallback
	 *            the callback to notify the first time a failure occurs
	 * @param mode
	 *            the mode the folder should be opened into, READ_ONLY or
	 *            READ_WRITE
	 * @param folderName
	 *            name of the folder used to retrieve IMAP messages (usually
	 *            inbox or greetings)
	 */
	private void executeWithFolder(Callable<Void> callable, Callback<?> failureCallback,
			OpenMode mode, String folderName) {
		if (mStarted.getAndSet(true)) {
			throw new IllegalStateException("Already have an operation in progress");
		}

		// If HIPRI on Wi-Fi, then let the connectivity manager handle the setup if required.
		if (StackStaticConfiguration.HIPRI_ON_WIFI) {
			String serverAddress = mAccountDetails.getImapServer();
			mNetworkManager.setUpNetworkIfRequired(serverAddress);
		}

		try {
			mFolder = openFolder(folderName);
			mFolder.open(mode, null);
			callable.call();
			logger.d("Closing mailbox");
			closeMailbox();

			// Notify of IMAP Connectivity Success
			mNotifier.sendNotification(DataChannelNotification.connectivityOk());
		} catch (Exception e) {
			logger.w(String.format("Exception occured. Cause: %s, Type: %s", e.getCause(), e
					.getClass().toString()));
			handleFailure(e, failureCallback);
		}
	}

	/**
	 * Close the mailbox. By default, it does not expunge before closing, the mExpunge attribute
	 * should be set to true if that is required. However, a LOGOUT command will be automatically
	 * sent.
	 */
	private void closeMailbox() {
		FolderProxy folder = mFolder;
		mFolder = null;

		if (folder != null) {
			try {
				folder.close(mExpunge, true); // logout
			} catch (MessagingException e) {
				logger.e("failure while closing folder", e);
			}
		}
	}

	// Visible for testing.
	protected FolderProxy openFolder(String name) throws MessagingException {
		Email.setTempDirectory(mContext);
		Store store = ImapStore.newInstance(mAccountDetails.getUriString(), mContext, null);
		return new FolderDelegate(store.getFolder(name));
	}

	private void handleFailure(Exception e, Callback<?> callback) {
		if (!mFinished.getAndSet(true)) {
			closeMailbox();
			callback.onFailure(e);
		}
	}

	private Voicemail getVoicemailFromMessage(Message message) throws MessagingException {
		if (!message.getMimeType().startsWith("multipart/")) {
			logger.w("Ignored non multi-part message");
			return null;
			// the subsequent check is used both for voicemails and greetings messages
			// GREETING_MESSAGE is used in case of greetings selected from GREETINGS folder,
			// this type is returned by Acision and Unisys servers, however Comverse is not 
			// fully compliant to OMTP standard and in this case returns multipart/mixed
			// type which has been added to this check...
		} else if ((StackStaticConfiguration.VOICE_MESSAGES_ONLY)
				&& (!(message.getContentType().contains(ImapConstants.VOICE_MESSAGE) || message
						.getContentType().contains(ImapConstants.GREETING_MESSAGE) ||
						message.getContentType().contains(ImapConstants.MIXED)))) {
			logger.w("Ignored non voice-message or non greeting-message");
			return null;
		}
		Multipart multipart = (Multipart) message.getBody();
		logger.d("Num body parts: " + multipart.getCount());
		logger.d("Content type: " + multipart.getContentType());
		for (int i = 0; i < multipart.getCount(); ++i) {
			BodyPart bodyPart = multipart.getBodyPart(i);
			String bodyPartMimeType = bodyPart.getMimeType().toLowerCase(Locale.US);
			logger.d("bodyPart mime type: " + bodyPartMimeType);
			if (bodyPartMimeType.startsWith("audio/")) {
				// Found an audio attachment, this is a valid voicemail.
				VoicemailImpl.Builder voicemailBuilder = VoicemailImpl.createEmptyBuilder()
						.setTimestamp(message.getSentDate().getTime())
						.setSourceData(message.getUid())
						.setDuration(FIXED_DURATION_FOR_RETREIVED_MESSAGES );
				setSender(voicemailBuilder, message.getFrom());
				setMailBoxAndReadStatus(voicemailBuilder, message.getFlags());
				return voicemailBuilder.build();
			}
		}
		// No attachment found, this is not a voicemail.
		return null;
	}

	private VoicemailPayload getVoicemailPayloadFromMessage(Message message)
			throws MessagingException, IOException {
		Multipart multipart = (Multipart) message.getBody();
		logger.d("Num body parts: " + multipart.getCount());
		for (int i = 0; i < multipart.getCount(); ++i) {
			BodyPart bodyPart = multipart.getBodyPart(i);
			String bodyPartMimeType = bodyPart.getMimeType().toLowerCase(Locale.US);
			logger.d("bodyPart mime type: " + bodyPartMimeType);
			if (bodyPartMimeType.startsWith("audio/")) {
				byte[] bytes = getAudioDataFromBody(bodyPart.getBody());
				logger.d(String.format("Fetched %s bytes of data", bytes.length));
				return new VoicemailPayloadImpl(bodyPartMimeType, bytes);
			}
		}
		throw new MessagingException("No audio attachment found on this voicemail");
	}

	/** Sets the mailbox and read status */
	private void setMailBoxAndReadStatus(VoicemailImpl.Builder voicemailBuilder, Flag[] flags) {
		List<Flag> flagList = Arrays.asList(flags);
		voicemailBuilder.setIsRead(flagList.contains(Flag.SEEN));
	}

	private void setSender(VoicemailImpl.Builder voicemailBuilder, Address[] fromAddresses) {
		if (fromAddresses != null && fromAddresses.length > 0) {
			if (fromAddresses.length != 1) {
				logger.w("More than one from addresses found. Using the first one.");
			}
			String sender = fromAddresses[0].getAddress();
			int atPos = sender.indexOf('@');
			if (atPos != -1) {
				// Strip domain part of the address.
				sender = sender.substring(0, atPos);
			}
			voicemailBuilder.setNumber(sender);
		}
	}

	private String debugStringForMessage(Message message) {
		return new StringBuilder().append("## Message Details: \n")
				.append("UID: " + message.getUid() + "\n")
				.append("FLAGS: " + Arrays.toString(message.getFlags()) + "\n")
				.append(debugStringForHeader(message, "From")).append("\n")
				.append(debugStringForHeader(message, "To")).append("\n")
				.append(debugStringForHeader(message, "Content-Type")).append("\n")
				.append(debugStringForHeader(message, "X-CNS-Greeting-Type")).append("\n")
				.append(debugStringForHeader(message, "Date")).append("\n")
				.append(debugStringForHeader(message, "Message-Id")).toString();
	}

	private String debugStringForHeader(Message message, String name) {
		try {
			return name + ": " + Arrays.toString(message.getHeader(name));
		} catch (MessagingException e) {
			return name + ": null";
		}
	}

	private byte[] getAudioDataFromBody(Body body) throws IOException, MessagingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
		
		try {
			body.writeTo(bufferedOut);
		} finally {
			CloseUtils.closeQuietly(bufferedOut);
			CloseUtils.closeQuietly(out); // no effect
		}

		return Base64.decode(out.toByteArray(), Base64.DEFAULT);
	}

	/**
	 * Fetches the structure of the given message and returns the voicemail parsed from it.
	 * 
	 * @throws MessagingException
	 *             if fetching the structure of the message fails
	 */
	private Voicemail fetchVoicemail(Message message, Callback<?> failureCallback)
			throws MessagingException {
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.addAll(Arrays.asList(FetchProfile.Item.FLAGS, FetchProfile.Item.ENVELOPE,
				FetchProfile.Item.STRUCTURE));
		logger.d("Fetching message structure for " + message.getUid());
		MessageStructureFetchedListener listener = new MessageStructureFetchedListener(
				failureCallback);
		mFolder.fetch(new Message[] { message }, fetchProfile, listener);
		return listener.getVoicemail();
	}

	/**
	 * Fetches the body of the given message and returns the parsed voicemail payload.
	 * 
	 * @throws MessagingException
	 *             if fetching the body of the message fails
	 */
	private VoicemailPayload fetchVoicemailPayload(Message message, Callback<?> failureCallback)
			throws MessagingException {
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.BODY);
		logger.d("Fetching message body for " + message.getUid());
		MessageBodyFetchedListener listener = new MessageBodyFetchedListener(failureCallback);
		mFolder.fetch(new Message[] { message }, fetchProfile, listener);
		return listener.getVoicemailPayload();
	}

	/** Opens the said folder for specified imap account. */
	/* package for testing */FolderProxy openImapFolder(OmtpAccountInfo accountDetails,
			String folderName, Folder.OpenMode openMode) throws MessagingException {
		FolderDelegate folder = new FolderDelegate(ImapStore.newInstance(
				accountDetails.getUriString(), mContext, null).getFolder(folderName));
		folder.open(openMode, null);
		return folder;
	}

	/**
	 * Converts an array of {@link Voicemail} objects to Imap {@link Message} objects.
	 */
	private Message[] convertToImapMessages(Voicemail[] voicemails) {
		Message[] messages = new Message[voicemails.length];
		for (int i = 0; i < voicemails.length; ++i) {
			messages[i] = new MimeMessage();
			messages[i].setUid(voicemails[i].getSourceData());
		}
		return messages;
	}
	
	/**
	 * Listener for the message structure being fetched.
	 * <p>
	 * In case of failure, it calls {@link #handleFailure(Exception, Callback)}.
	 */
	private final class MessageStructureFetchedListener implements Folder.MessageRetrievalListener {
		private final Callback<?> mFailureCallback;

		private Voicemail mVoicemail;

		public MessageStructureFetchedListener(Callback<?> failureCallback) {
			mFailureCallback = failureCallback;
		}

		public Voicemail getVoicemail() {
			return mVoicemail;
		}

		@Override
		public void messageRetrieved(Message message) {
			logger.d("Fetched message structure for " + message.getUid());
			logger.d("Message retrieved: " + message);
			// TODO: Get rid of the detailed message logging when we are done
			// with testing.
			logger.d(debugStringForMessage(message));
			try {
				mVoicemail = getVoicemailFromMessage(message);
				if (mVoicemail == null) {
					logger.d("This voicemail does not have an attachment...");
					return;
				}
			} catch (MessagingException e) {
				handleFailure(e, mFailureCallback);
			}
		}
	}

	/**
	 * Listener for the message body being fetched.
	 * <p>
	 * In case of failure, it calls {@link #handleFailure(Exception, Callback)}.
	 */
	private final class MessageBodyFetchedListener implements Folder.MessageRetrievalListener {
		private final Callback<?> mFailureCallback;

		private VoicemailPayload mVoicemailPayload;

		public MessageBodyFetchedListener(Callback<?> failureCallback) {
			mFailureCallback = failureCallback;
		}

		/** Returns the fetch voicemail payload. */
		public VoicemailPayload getVoicemailPayload() {
			return mVoicemailPayload;
		}

		@Override
		public void messageRetrieved(Message message) {
			logger.d("Fetched message body for " + message.getUid());
			logger.d("Message retrieved: " + message);
			// TODO: Get rid of the detailed message logging when we are done
			// with testing.
			logger.d(debugStringForMessage(message));
			if (mFinished.get()) {
				// Once we've finished, i.e. reported a callback, we ignore
				// further messages.
				return;
			}
			try {
				mVoicemailPayload = getVoicemailPayloadFromMessage(message);
			} catch (MessagingException e) {
				handleFailure(e, mFailureCallback);
			} catch (IOException e) {
				handleFailure(e, mFailureCallback);
			}
		}
	}

	/**
	 * Creates new Greeting Message based on its type.
	 * @param greetingType
	 * @return Messages containing the new greeting
	 * @throws MessagingException
	 */
	@Nullable
	private Message[] createAndSendNewGreetingMessage(GreetingType greetingType, 
			GreetingsHelper greetingsHelper) throws MessagingException {
		GreetingCreator greetingCreator = GreetingCreatorImpl.newInstance(greetingType,
				greetingsHelper);
		Message newGreetingMessage = greetingCreator.createMessage();
		Message[] newMessages = new Message[] { newGreetingMessage };
		
		if (newGreetingMessage != null && newMessages.length > 0) {
			// send new greeting
			mFolder.appendMessages(newMessages);
			// activate new greeting
			mFolder.setFlags(newMessages, new Flag[] { Flag.GREETING_ON }, true);
		} else {
			logger.e("Error creating and sending new greeting message");
		}
		return newMessages;
	}

	@Override
	public void uploadGreetings(final Callback<Greeting> callback,
			final GreetingUpdateType operationType, final GreetingType greetingToUpdateType,
			final GreetingsHelper greetingsHelper) {
		
		executeWithFolder(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				// Variable that will store the messages after upload (with updated IDs from the
				// IMAP server
				Message[] messagesAfterUpload = null;
				// check if we have received all that's required
				if (operationType == null || greetingToUpdateType == null) {
					logger.e("uploadGreetings() called without required information");
					return null;
				}
				
				logger.d(String.format("uploadGreetings() called with greeting to update/activate type:%s operation:%s", 
						greetingToUpdateType.getTypeString(), operationType.getTypeString()));
				// first get all greeting Messages from the server
				Message[] messages = mFolder.getMessages(null);
				
				// now trigger an action depending on the number of messages on the server
				switch (messages.length) {
				case 0: // no old greetings available in GREETINGS mailbox, just
						// upload the new one
					messagesAfterUpload = createAndSendNewGreetingMessage(greetingToUpdateType,
							greetingsHelper);
					break;
					
				case 1: // just one old greeting, deactivate or delete it and upload the new one
				{
					Message message = messages[0];
					// fetch message structure (FLAGS etc..)
					Voicemail fetchedGreeting = fetchVoicemail(message, callback);
					if (fetchedGreeting != null) {
						String oldGreetingType = message.getGreetingType();
						if (greetingToUpdateType.getTypeString().equalsIgnoreCase(oldGreetingType)) {
							// the same type as before, it needs to be deleted first
							logger.d(String.format("marking greeting type:%s as deleted",
									oldGreetingType));
							mFolder.setFlags(messages, new Flag[] { Flag.DELETED }, true);
							mFolder.expunge();
						} else {
							// different greeting type than before, needs to be
							// flagged
							logger.d(String.format("marking greeting type:%s as inactive",
									oldGreetingType));
							mFolder.setFlags(messages, new Flag[] { Flag.GREETING_ON }, false);
						}
						messagesAfterUpload = createAndSendNewGreetingMessage(greetingToUpdateType,
								greetingsHelper);

					} else {
						createGreetingUpdateError(callback);
						return null;
					}
					break;
				}
					
				default: // more than 1 old greeting present on the server
					for (Message message : messages) {
						// fetch message structure (FLAGS etc..)
						Voicemail fetchedGreeting = fetchVoicemail(message, callback);
						if (fetchedGreeting != null) {
							String oldGreetingType = message.getGreetingType();
							if (greetingToUpdateType.getTypeString().equalsIgnoreCase(oldGreetingType)) {
								// actions for the same type of greeting as the new one
								switch (operationType) {
								case ONLY_CHANGE_REQUIRED: // switch greeting to inactive
									logger.d(String.format("marking greeting type:%s as active",
											oldGreetingType));
									mFolder.setFlags(new Message[] { message },
											new Flag[] { Flag.GREETING_ON }, true);
									messagesAfterUpload = new Message[] { message };
									break;
								case UPLOAD_REQUIRED: // delete current message,
									logger.d(String.format("marking greeting type:%s as deleted",
											oldGreetingType));
									mFolder.setFlags(new Message[] { message },
											new Flag[] { Flag.DELETED }, true);
									mFolder.expunge();
									// send new greeting content
									messagesAfterUpload = createAndSendNewGreetingMessage(
											greetingToUpdateType, greetingsHelper);
									break;
								default:
									// this is not normal, something is wrong, call failure
									createGreetingUpdateError(callback);
									return null;
								}
								
							} else {// actions for different type of greeting as the new one
								
								// check if the greeting to be uploaded has the same type as
								// the currently active greeting, if not deactivate the other type
								if (isItActiveGreeting(message)) {
									logger.d(String.format("marking greeting type:%s as inactive",
											oldGreetingType));
									mFolder.setFlags(new Message[] { message },
											new Flag[] { Flag.GREETING_ON }, false);
								} else {
									logger.d("Considered greeting was already inactive, "
											+ "skipping flags modification");
								}
							}
						} else {
							createGreetingUpdateError(callback);
							return null;
						}
					}
					break;
				}
				
				if (!mFinished.getAndSet(true)) {
					// get messages again 
					Greeting fetchedGreetingToReturn = null;
					if (messagesAfterUpload != null) {
						for (Message message : messagesAfterUpload) {
							// fetch message structure as a Voicemail
							Voicemail voicemail = getVoicemailFromMessage(message);
							if (voicemail != null) {
								fetchedGreetingToReturn = GreetingImpl.createFromFetch(
										message.getGreetingType(), voicemail, true, true).build();
							}
						}
					}

					logger.d(String.format(
							"calling onSucess callback in uploadGreetings() with Greeting: %s",
							fetchedGreetingToReturn));
					callback.onSuccess(fetchedGreetingToReturn);
				}
				return null;
			}

			/**
			 * Method propagates greetings update error.
			 * @param callback
			 */
			private void createGreetingUpdateError(final Callback<Greeting> callback) {
				logger.e("Unalbe to update greetings failure.");
				callback.onFailure(new Exception("Greetings update error"));
			}
			
		}, callback, OpenMode.READ_WRITE, StackStaticConfiguration.GREETINGS_FOLDER_NAME);
	}
	
	/**
	 * Checks the received message flags and returns true if GREETING_ON {@link Flag} is 
	 * set for this {@link Message}.
	 * @param message
	 * @return true if flag is present, false otherwise
	 */
	private boolean isItActiveGreeting(Message message) {
		Flag[] flagsReceived = message.getFlags();
		for (Flag flag : flagsReceived) {
			if (flag.equals(Flag.GREETING_ON)) {
				return true;
			} 
		}
		return false;
	}

	@Override
	public void fetchAllGreetings(final Callback<List<Greeting>> callback) {
		executeFetchWithFolder(new Callable<Void>() {

			@Override
			public Void call() throws MessagingException {
				logger.d("fetchAllGreetings() called.");
				// initialise fetched Greetings list
				List<Greeting> greetingsList = new ArrayList<Greeting>();
				
				// first fetch all greeting Messages structure
				Message[] messages = mFolder.getMessages(null);
				for (Message message : messages) {
					// fetch message structure as a Voicemail
					Voicemail voicemail = fetchVoicemail(message, callback);
					if (voicemail != null) {
						Greeting fetchedGreeting = GreetingImpl.createFromFetch(message.getGreetingType(),
								voicemail, isItActiveGreeting(message), false).build();
						logger.d(String.format("Fetched greeting type:%s, active:%s", fetchedGreeting
								.getGreetingType().getTypeString(), fetchedGreeting.isActive()));

						// save received greeting to a List
						greetingsList.add(fetchedGreeting);
					}
				}
				
				if (!mFinished.getAndSet(true)) {
					logger.d("calling onSucess callback in fetchAllGreetings()");
					callback.onSuccess(greetingsList);
				}
				return null;
			}

		}, callback, StackStaticConfiguration.GREETINGS_FOLDER_NAME);
	}
	
	/**
	 * Fetch greeting payload and return it.
	 * @param callback
	 * @param greeting
	 * @param greetingType
	 */
	@Override
	public void fetchGreetingPayload(final Callback<VoicemailPayload> callback,
			final Greeting greeting) {

		executeFetchWithFolder(new Callable<Void>() {

			@Override
			public Void call() throws MessagingException {
				String greetingUid = greeting.getVoicemail().getSourceData();
				logger.d(String.format("Fetching Greetings Payload type:%s, Uid:%s",
						greeting.getGreetingType().getTypeString(), greetingUid));

				// convert greeting to a message (just Uid is important)
				Message message = mFolder.getMessage(greetingUid);
				
				// fetch payload 
				VoicemailPayload fetchedPayload = fetchVoicemailPayload(message, callback);
				
				if (!mFinished.getAndSet(true)) {
					logger.d("calling onSucess callback in fetchGreetingPayload()");
					callback.onSuccess(fetchedPayload);
				}
				return null;
			}

		}, callback, StackStaticConfiguration.GREETINGS_FOLDER_NAME);

	}
	
}
