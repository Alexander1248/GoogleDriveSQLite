package ru.alexander;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDrivePipeline {

    private static final String APPLICATION_NAME = "Google Drive SQLite";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


//    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    private static Drive service;
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleDrivePipeline.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static void authorize() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
    }

   public static String start() throws GeneralSecurityException, IOException {
       authorize();
       String rootID;
       if (!exists(APPLICATION_NAME)) rootID = createFolder(null, APPLICATION_NAME);
       else rootID = findFile(APPLICATION_NAME);
       return rootID;
   }
   public static void close() {
        service = null;
   }



    public static boolean exists(String name) {
        if (service != null) {
            try {
                List<File> files = service.files().list().execute().getFiles();
                for (File file : files) if (file.getName().equals(name)) return true;
            } catch (IOException e) {
                System.err.println("Unable to complete test: " + e.getMessage());
            }
        }
        return false;
    }
    public static String findFile(String name) {
        if (service != null) {
            try {
                List<File> files = service.files().list().execute().getFiles();
                for (File file : files) if (file.getName().equals(name)) return file.getId();
            } catch (IOException e) {
                System.err.println("Unable to find file: " + e.getMessage());
            }
        }
        return null;
    }

    public static String createFile(String parentID, String name, AbstractInputStreamContent uploadStreamContent) throws IOException {
        if (service != null) {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            if (parentID != null && parentID.length() > 0)
                fileMetadata.setParents(List.of(parentID));

            try {
                File file = service.files().create(fileMetadata, uploadStreamContent)
                        .setFields("id")
                        .execute();
                return file.getId();
            } catch (GoogleJsonResponseException e) {
                System.err.println("Unable to create file: " + e.getDetails());
                throw e;
            }
        }
        return null;
    }

    public static ByteArrayOutputStream loadFile(String id) throws IOException {
        if (service != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            service.files().get(id).executeMediaAndDownloadTo(outputStream);
            return outputStream;
        }
        return null;
    }

    public static String createFolder(String parentID, String name) throws IOException {
        if (service != null) {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (parentID != null && parentID.length() > 0)
                fileMetadata.setParents(List.of(parentID));

            try {
                File file = service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                return file.getId();
            } catch (GoogleJsonResponseException e) {
                System.err.println("Unable to create folder: " + e.getDetails());
                throw e;
            }
        }
        return null;
    }

    public static void removeFile(String id) {
        if (service != null) {
            try {
                service.files().delete(id).execute();
            } catch (IOException e) {
                System.err.println("Unable to delete: " + e.getMessage());
            }
        }
    }
    public static List<File> getFileList() throws IOException {
        if (service != null)
            return service.files().list().execute().getFiles();
        return null;
    }

}
