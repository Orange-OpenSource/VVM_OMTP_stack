package com.orange.labs.uk.omtp.greetings;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import android.util.Base64;
import android.util.Base64OutputStream;

import com.android.email.mail.Address;
import com.android.email.mail.Body;
import com.android.email.mail.Message.RecipientType;
import com.android.email.mail.MessagingException;
import com.android.email.mail.internet.MimeBodyPart;
import com.android.email.mail.internet.MimeHeader;
import com.android.email.mail.internet.MimeMessage;
import com.android.email.mail.internet.MimeMultipart;
import com.orange.labs.uk.omtp.logging.Logger;

public final class GreetingCreatorImpl implements GreetingCreator {
	private static final Logger logger = Logger.getLogger(GreetingCreatorImpl.class);
	private static final String GREETING_SUBJECT = "greeting";
	private static final String X_VOICE_GREETING_MESSAGE = "x-voice-greeting-message";
	private static final String VVM_CLIENT_ADDRESS = "vvmclient@omtp.org";
	private static final String USER_AGENT = "OMTP VVM client for Android";
	private static final String GREETING_TEXT_BODY = "greeting voicemail in attachment";
	private static final String GREETING_TO_FILED = "vvm@mail.com";
	
	/**
	 * Type of a greeting used to create a greeting message.
	 */
	private final GreetingType mGreetingType;
	private final GreetingsHelper mGreetingsHelper;

	/**
	 * Private constructor.
	 */
	private GreetingCreatorImpl(GreetingType greetingType, GreetingsHelper greetingsHelper) {
		mGreetingType = greetingType;
		mGreetingsHelper = greetingsHelper;
	}
	
	/**
	 * Creates new GreetingCreator instance.
	 * @param greetingType
	 * @return
	 */
	public static GreetingCreatorImpl newInstance(GreetingType greetingType, 
			GreetingsHelper greetingsHelper) {
		return new GreetingCreatorImpl(greetingType, greetingsHelper);
	}

	
	@Override
	public MimeMessage createMessage() throws MessagingException {
		logger.d("createMessage() called");
		MimeMessage message = new MimeMessage();
		message.setSentDate(new Date());
		message.setFrom(new Address(VVM_CLIENT_ADDRESS));
		message.setRecipient(RecipientType.TO, new Address(GREETING_TO_FILED));
		message.setHeader(MimeHeader.GREETING_TYPE, mGreetingType.getTypeString());
		message.setHeader("Message-Context", X_VOICE_GREETING_MESSAGE);
		message.setHeader("User-Agent", USER_AGENT);
		message.setSubject(GREETING_SUBJECT);

		// add attachment
		MimeMultipart mp = new MimeMultipart();
		PlainTextBody body = new PlainTextBody(GREETING_TEXT_BODY);
		MimeBodyPart mimeBodyPart = new MimeBodyPart(body, "text/plain");
		mimeBodyPart.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "7bit");
		mp.addBodyPart(mimeBodyPart);
		addAttachmentsToMessage(mp);
		message.setBody(mp);
		
		return message;
	}

	/**
	 * Add attachments as parts into a MimeMultipart container.
	 * 
	 * @param mp
	 *            MimeMultipart container in which to insert parts.
	 * @throws MessagingException
	 */
	private void addAttachmentsToMessage(final MimeMultipart mp) throws MessagingException {

		BinaryGreetingFileBody binaryFileBody = new BinaryGreetingFileBody();
		MimeBodyPart bp = new MimeBodyPart(binaryFileBody, "audio/amr");
		bp.addHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
		  
		 /*
         * TODO: Oh the joys of MIME...
         *
         * From RFC 2183 (The Content-Disposition Header Field):
         * "Parameter values longer than 78 characters, or which
         *  contain non-ASCII characters, MUST be encoded as specified
         *  in [RFC 2184]."
         *
         * Example:
         *
         * Content-Type: application/x-stuff
         *  title*1*=us-ascii'en'This%20is%20even%20more%20
         *  title*2*=%2A%2A%2Afun%2A%2A%2A%20
         *  title*3="isn't it!"
         */
		
		bp.addHeader(
				MimeHeader.HEADER_CONTENT_DISPOSITION,
				String.format("attachment; filename=\"%s\"",
						mGreetingType.getFileName()));

		mp.addBodyPart(bp);
		 
	}

	private class BinaryGreetingFileBody implements Body {
	    private File mFile;
	    
	    public InputStream getInputStream() throws MessagingException {
	        try {
	            return new BinaryTempFileBodyInputStream(new FileInputStream(mFile));
	        }
	        catch (IOException ioe) {
	            throw new MessagingException("Unable to open body", ioe);
	        }
	    }

	    public void writeTo(OutputStream out) throws IOException, MessagingException {
	        InputStream in = mGreetingsHelper.getGreetingsFileInputStream(mGreetingType);
	        Base64OutputStream base64Out = new Base64OutputStream(
	            out, Base64.CRLF | Base64.NO_CLOSE);
	        if (in != null & base64Out != null) {
	        	IOUtils.copy(in, base64Out);
	        	base64Out.close();
	        	in.close();
	        }
	    }

	    class BinaryTempFileBodyInputStream extends FilterInputStream {
	        public BinaryTempFileBodyInputStream(InputStream in) {
	            super(in);
	        }

	        @Override
	        public void close() throws IOException {
	            super.close();
	            mFile.delete();
	        }
	    }
	}
	
	
	private class PlainTextBody implements Body {
	    String mBody;

	    public PlainTextBody(String body) {
	        this.mBody = body;
	    }

	    public void writeTo(OutputStream out) throws IOException, MessagingException {
	    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
	        byte[] bytes = mBody.getBytes("UTF-8");
	        writer.write(new String(bytes));
	        writer.flush();
	    }

	    /**
	     * Returns an InputStream that reads this body's text in UTF-8 format.
	     */
	    public InputStream getInputStream() throws MessagingException {
	        try {
	            byte[] b = mBody.getBytes("UTF-8");
	            return new ByteArrayInputStream(b);
	        }
	        catch (UnsupportedEncodingException usee) {
	            return null;
	        }
	    }
	}
}
