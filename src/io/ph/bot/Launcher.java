package io.ph.bot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.LoggerFactory;

import io.ph.bot.commands.CommandHandler;

/**
 * Main entry point
 * @author Paul
 * TODO: Refactor code to be more consistent (i.e. pass entire IGuild rather than getID)
 */
public class Launcher {
	public static void main(String[] args) {
		CommandHandler.initCommands();
		Bot.getInstance().start(args);
	}

	/*
	 * Load the Themes.moe SSL certificate and set it as trusted, since the default
	 * SSL store doesn't include Let's Encrypt yet (at least, on most Java8 versions)
	 * 
	 * Also, configure empty directories that might be filled later
	 */
	static {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			Path ksPath = Paths.get(System.getProperty("java.home"),
					"lib", "security", "cacerts");
			keyStore.load(Files.newInputStream(ksPath),
					"changeit".toCharArray());

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			try (InputStream caInput = new BufferedInputStream(
					(new FileInputStream
							(new File("resources/Themes.cer"))))) {
				Certificate crt = cf.generateCertificate(caInput);
				LoggerFactory.getLogger(Launcher.class).info("Added Cert for " + ((X509Certificate) crt)
						.getSubjectDN());

				keyStore.setCertificateEntry("DSTRootCAX3", crt);
			}

			if(false) {
				System.out.println("Truststore now trusting: ");
				PKIXParameters params = new PKIXParameters(keyStore);
				params.getTrustAnchors().stream()
					.map(TrustAnchor::getTrustedCert)
					.map(X509Certificate::getSubjectDN)
					.forEach(System.out::println);
				System.out.println();
			}

			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			SSLContext.setDefault(sslContext);
		} catch (Exception e) {
			LoggerFactory.getLogger(Launcher.class).warn("Error adding Themes.moe SSL cert to trusted set. "
					+ "You won't be able to use the theme command!");
		}
		new File("resources/tempdownloads").mkdirs();
	}

}
