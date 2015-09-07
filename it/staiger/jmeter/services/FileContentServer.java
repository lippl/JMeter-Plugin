/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Only to be used with Files from {@link org.apache.jmeter.services.FileServer} class!
 */
public class FileContentServer {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Map to store file contents associated with their File object
     */
    private final Map<File, byte[]> files = new HashMap<>();

    private static final FileContentServer server = new FileContentServer();

    // Cannot be instantiated
    private FileContentServer() {
    }

    /**
     * @return the singleton instance of the server.
     */
    public static FileContentServer getServer() {
        return server;
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     * Access via String of path only to prevent multiple entries with different
     * File objects with same Path.
     *
     * @param path path relative (to base) or absolute file path (must not be null)
     */
    public void reserveFile(String path) {
        reserveFile(FileServer.getFileServer().getResolvedFile(path));
    }

    /**
     * Creates an association between a filename and a Files content,
     * and stores it for later use - unless it is already stored.
     * Access via File only with File from FileServer to prevent duplicates
     * of duplicate File objects with same Path.
     *
     * @param file File which is to be saved - coming from FileServer
     */
    private void reserveFile(File file) {
    	if(files.get(file)==null){
    		files.put(file, getFile(file));
    		log.debug("added " + file.getPath());
    	}else{
    		log.debug(file.getPath() + " already reserved");
    		
    	}
    }
    

    /**
     * Retrieves the content f a file as a byte array
     *
     * @param file File of which the content is to be returned
     */
	private byte[] getFile(File file){
		try {
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (IOException e1) {
			log.error("Could not read file " + file.getPath());
			e1.printStackTrace();
			return null;
		}
	}

    /**
     * Get Byte Array of content for provided file path,
     * resolve file location relative to base dir or script dir using FileServer
     * @param path original path to file, maybe relative
     * @return Byte Array of content
     */
    public byte[] getFileContent(String path) {
        return getFileContent(FileServer.getFileServer().getResolvedFile(path));
    }
    /**
     * Get byte[] for provided File instance,
     * @param path original path to file, maybe relative
     * @return Byte Array of content
     */
    private byte[] getFileContent(File file) {
        reserveFile(file);
        return files.get(file);
    }

    /**
     * Get String of SHA256 hash for provided file path,
     * @param path original path to file, maybe relative
     * @return String SHA256 hash
     */
    public String getSHA256(String path) {
        return getSHA256(FileServer.getFileServer().getResolvedFile(path));
    }

    /**
     * Get String of SHA256 hash for provided file path,
     * after makeing sure it has been reserved already
     * @param file File from FileServer
     * @return String SHA256 hash
     */
    private String getSHA256(File file) {
        reserveFile(file);
        return getSHA256(files.get(file));
    }

    /**
     * Get String of SHA256 hash for provided byte array,
     * @param content byte array
     * @return String SHA256 hash
     */
	private String getSHA256(byte[] content){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return new String(Hex.encodeHex(md.digest(content)));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
    public synchronized void clearFiles() {
        files.clear();
    }
}