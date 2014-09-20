package vietj.templatemailer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.stringtemplate.v4.ST;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Main {

  public static void main(String[] args) {

    Main main = new Main();
    new JCommander(main, args);
    main.execute();

  }

  @Parameter(names = {"--subject"}, description = "Le sujet du mail", required = true)
  private String subject;

  @Parameter(names = {"--from"}, description = "L'expediteur", required = true)
  private String from;

  @Parameter(names = {"--host"}, description = "L'hote du serveur smtp", required = true)
  private String host;

  @Parameter(names = {"--port"}, description = "Le port du serveur smtp", required = false)
  private Integer port = null;

  @Parameter(names = {"--secure"}, description = "utiliser TLS", required = false)
  private Boolean secure = null;

  @Parameter(names = {"--username"}, description = "le nom du compte pour envoyer les emails", required = false)
  private String username = null;

  @Parameter(names = {"--password"}, description = "le password du compte pour envoyer les emails", required = false)
  private String password = null;

  @Parameter(names = {"--template"}, description = "Le chemin du template", required = true)
  private String templatePath;

  @Parameter(names = {"--source"}, description = "Le chemin du csv", required = true)
  private String sourcePath;

  public Main() {
  }

  public void execute()  {

    Reader in;
    try {
      in = new InputStreamReader(new FileInputStream(sourcePath), "UTF-8");
    } catch (FileNotFoundException e) {
      System.err.println("Fichier source " + sourcePath + " non trouvé");
      e.printStackTrace(System.err);
      return;
    } catch (UnsupportedEncodingException e) {
      System.err.println("Encoding UTF-8 non supporté");
      e.printStackTrace(System.err);
      return;
    }

    Iterable<CSVRecord> source;
    try {
      source = CSVFormat.EXCEL.parse(in);
    } catch (IOException e) {
      System.err.println("Probleme de lecture du fichier source " + sourcePath);
      e.printStackTrace(System.err);
      return;
    }

    String template;
    try {
      in = new InputStreamReader(new FileInputStream(templatePath), "UTF-8");
      char[] buffer = new char[256];
      StringWriter out = new StringWriter();
      int i;
      while ((i = in.read(buffer)) != -1) {
        out.write(buffer, 0, i);
      }
      template = out.toString();
    } catch (FileNotFoundException e) {
      System.err.println("Fichier template " + sourcePath + " non trouvé");
      e.printStackTrace(System.err);
      return;
    } catch (IOException e) {
      System.err.println("Probleme de lecture du fichier template " + templatePath);
      e.printStackTrace(System.err);
      return;
    }

    //
    Properties props = new Properties();
    props.setProperty("mail.smtp.host", host);
    if (port != null) {
      props.setProperty("mail.smtp.port", Integer.toString(port));
    }
    Authenticator authenticator;
    if (username != null && password != null) {
      props.setProperty("mail.smtp.auth", "true");
      authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password);
        }
      };
    } else {
      authenticator = null;
    }
    if (secure == Boolean.TRUE) {
      props.setProperty("mail.smtp.starttls.enable", "true");
    }

    Session session = Session.getInstance(props, authenticator);
    for (CSVRecord record : source) {
      Iterator<String> i = record.iterator();
      if (i.hasNext()) {
        String recipient = i.next();
        ST st = new ST(template);
        st.add("bal_0", recipient);
        int index = 1;
        while (i.hasNext()) {
          st.add("bal_" + index, i.next());
          index++;
        }
        try {
          MimeMessage message = new MimeMessage(session);
          message.setFrom(new InternetAddress(from));
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
          message.setSubject(subject);
          message.setText(st.render());
          Transport.send(message);
          System.out.println("Message envoyé à " + recipient);
        } catch (MessagingException e) {
          System.err.println("Impossible d'envoyer un mail à ");
          e.printStackTrace(System.err);
        }
      }
    }
  }
}
