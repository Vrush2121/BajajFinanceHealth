import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class DestinationHashGenerator {

    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <jsonFilePath>");
            System.exit(1);
        }

        String prn = args[0].toLowerCase().replaceAll("\\s+", ""); 
        String jsonFilePath = args[1];

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(new File(jsonFilePath));
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            System.exit(1);
            return;
        }

        String destinationValue = findDestination(rootNode);
        if (destinationValue == null) {
            System.err.println("No 'destination' key found in JSON file.");
            System.exit(1);
        }

        String randomString = generateRandomString(8);
        String concatenated = prn + destinationValue + randomString;
        String hash = md5(concatenated);

        System.out.println(hash + ";" + randomString);
    }

    private static String findDestination(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if ("destination".equals(entry.getKey())) {
                    return entry.getValue().asText();
                }
                String result = findDestination(entry.getValue());
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                String result = findDestination(element);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}