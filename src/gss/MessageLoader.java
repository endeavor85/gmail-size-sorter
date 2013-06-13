package gss;

import java.security.Security;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class MessageLoader extends Thread
{
	private String					user;
	private Store					store;
	private String					imapHost	= "imap.gmail.com";
	private String					smtpHost	= "smtp.gmail.com";
	private ArrayList<MessageInfo>	allMessages;

	private Session					session;

	private long					totalSize	= 0;
	String							results		= "";

	private SizeSorter				sizeSorter;

	public MessageLoader(SizeSorter sizeSorter, final String user, final String pass)
	{
		this.sizeSorter = sizeSorter;
		this.user = user;

		sizeSorter.setInputEnabled(false);
		sizeSorter.setStatus("Authenticating...");

		allMessages = new ArrayList<MessageInfo>();

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", smtpHost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		try
		{
			// session = Session.getDefaultInstance(props, null);
			session = Session.getInstance(props, new javax.mail.Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(user, pass);
				}
			});
			store = session.getStore("imaps");
			store.connect(imapHost, user + "@gmail.com", pass);
		}
		catch(NoSuchProviderException e)
		{
			JOptionPane.showMessageDialog(sizeSorter, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch(MessagingException e)
		{
			JOptionPane.showMessageDialog(sizeSorter, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(sizeSorter, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		try
		{
			if(!store.isConnected())
			{
				sizeSorter.setInputEnabled(true);
				sizeSorter.setStatus("Start");
				return;
			}

			sizeSorter.setProgress(0);
			sizeSorter.showProgress(true);
			sizeSorter.setStatus("Loading...");

			Folder allMail = store.getFolder("[Gmail]").getFolder("All Mail");
			// Process All Mail messages
			if((allMail.getType() & Folder.HOLDS_MESSAGES) != 0)
			{
				allMail.open(Folder.READ_ONLY);

				Message[] messages = allMail.getMessages();

				sizeSorter.setTotalMessages(messages.length);

				results = allMail.getName() + ":<br>\n" + "<ul>\n" + " <li>Messages: " + messages.length + "\n";

				long folderSize = 0;
				int i = 0;

				for(Message message : messages)
				{
					sizeSorter.setProgress(((100 * ++i) / messages.length));
					sizeSorter.setCurrentMessage(i);
					String subject = message.getSubject();
					String from = (message.getFrom().length > 0) ? message.getFrom()[0].toString() : "Unknown";
					long size = message.getSize();
					folderSize += size;
					sizeSorter.setTotalSize(getSizeString(folderSize));

					allMessages.add(new MessageInfo(subject, from, size));
				}

				results += " <li>Size: " + getSizeString(folderSize) + "\n</ul>\n";

				totalSize += folderSize;

				allMail.close(false);
			}

			results += "<hr>\n" + "Total Size: " + getSizeString(totalSize) + "\n" + "<hr>\n" + "<table border=1 style=\"border-collapse: collapse;\">\n"
					+ "<tr><th>Subject</th><th>From</th><th>Size</th></tr>\n";

			sizeSorter.showProgress(false);
			sizeSorter.setStatus("Sorting...");

			Collections.sort(allMessages);

			sizeSorter.setStatus("Sending Results...");

			for(MessageInfo message : allMessages)
				results += message + "\n";

			results += "</table>\n";

			store.close();

			sendMail("Message Size Sorter", results, user + "@gmail.com", user + "@gmail.com");

			JOptionPane.showMessageDialog(sizeSorter, "Sorted info sent, check your inbox.", "Done", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(MessagingException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		sizeSorter.setStatus("Start");
		sizeSorter.setInputEnabled(true);
		return;
	}

	public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception
	{
		MimeMessage message = new MimeMessage(session);
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject);
		message.setContent(body, "text/html");
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

		Transport.send(message);
	}

	public static String getSizeString(long size)
	{
		double displayAmount = (double) size;
		int unitPowerOf1024 = 0;

		if(size <= 0)
			return "0 B";

		while(displayAmount >= 1024 && unitPowerOf1024 < 4)
		{
			displayAmount = displayAmount / 1024;
			unitPowerOf1024++;
		}

		final String[] units = { " B", " KB", " MB", " GB", " TB" };

		// ensure at least 2 significant digits (#.#) for small displayValues
		int fractionDigits = (displayAmount < 10) ? 1 : 0;
		NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
		f.setMaximumFractionDigits(fractionDigits);
		f.setMinimumFractionDigits(fractionDigits);
		return f.format(displayAmount) + " " + units[unitPowerOf1024];
	}

	private class MessageInfo implements Comparable<MessageInfo>
	{
		private String	subject;
		private String	from;
		private long	size;

		public MessageInfo(String subject, String from, long size)
		{
			this.setSubject(subject);
			this.setFrom(from);
			this.setSize(size);
		}

		@Override
		public String toString()
		{
			return "<tr>\n" + " <td><a href=\"https://mail.google.com/mail/?shva=1#search/subject%3A" + getSubject() + "+from%3A" + getFrom() + "\">" + getSubject() + "</td>\n" + " <td>" + getFrom()
					+ "</td>\n" + " <td>" + getSizeString(getSize()) + "</td>\n" + "</tr>\n";
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setFrom(String from)
		{
			this.from = from;
		}

		public String getFrom()
		{
			return from;
		}

		public void setSize(long size)
		{
			this.size = size;
		}

		public long getSize()
		{
			return size;
		}

		public int compareTo(MessageInfo o)
		{
			return (int) (o.size - this.size);
		}
	}
}
