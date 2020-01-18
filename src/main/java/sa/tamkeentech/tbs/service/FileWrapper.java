package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileWrapper {

    private final Logger log = LoggerFactory.getLogger(FileWrapper.class);
    @Value("${tbs.report.reports-folder}")
    private String outputFolder;

    /**
     * Save Attachment in path
     *
     * @param folder
     * @param fileName
     * @param bytes
     * @return
     */
    public String saveBytesToFile(String folder, String fileName, byte[] bytes) {
        FileOutputStream fileOuputStream = null;
        UUID uuid = UUID.randomUUID();
        String filePath = null;
        String dirPath = outputFolder + "/" + folder + "/";

        try {
            File attachmentTypeDir = new File(dirPath);

            // if the directory does not exist, create it
            if (!attachmentTypeDir.exists()) {
                try {
                    attachmentTypeDir.mkdir();
                } catch (SecurityException se) {
                    log.error("Could not save create sub directory {} exception:{}", attachmentTypeDir, se.getMessage());
                }
            }
            filePath = dirPath + fileName;
            fileOuputStream = new FileOutputStream(filePath);
            fileOuputStream.write(bytes);
            return filePath;
        } catch (FileNotFoundException e) {
            log.error("Could not save file {} exception:{}", fileName, e.getMessage());
        } catch (IOException e) {
            log.error("Could not save file {} exception:{}", fileName, e.getMessage());
        } finally {
            try {
                fileOuputStream.close();
                fileOuputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get Attachment data by file path
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public byte[] extractBytes(String filePath) throws IOException {

        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);

        return data;
    }

    /**
     * Delete file path
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public boolean deleteFile(String filePath) throws IOException {

        Path path = Paths.get(filePath);
        return Files.deleteIfExists(path);

    }
}
