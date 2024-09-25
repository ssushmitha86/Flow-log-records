package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import static org.example.Main.*;
import static org.junit.Assert.*;


public class MainTest {

    private File tempFile;
    private File tempTagFile;
    private File tempTagFileInvalid;
    private File logFile;
    private HashMap<String, String> protocolInfoMap;
    private HashMap<ProtoKey, String> protoTagMap;

    @Before
    public void setUpProtocolInfo() throws IOException {
        tempFile = File.createTempFile("Protocol", ".csv", new File("src/test/TestData/"));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            System.out.println("temp" + tempFile.getAbsolutePath());
            writer.write("TCP,11\n");
            writer.write("UDP,22\n");
            writer.write("FTP,34\n");
            writer.write("SFTP,45\n");
        }
    }

    @After
    public void tearDownProtocolInfo() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testProtocolInfo() {
        HashMap<String, String> map = protocolInfo(tempFile.getAbsolutePath());
        System.out.println(map + tempFile.getAbsolutePath());
        assertEquals("Expected value for TCP", "11", map.get("TCP"));
        assertEquals("Expected value for UDP", "22", map.get("UDP"));
        assertEquals("Expected value for FTP", "34", map.get("FTP"));
        assertEquals("Expected value for SFTP", "45", map.get("SFTP"));
    }


    @Before
    public void setUpProtocolMapping() throws IOException {
        tempTagFile = File.createTempFile("ProtocolTest", ".csv", new File("src/test/TestData/"));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempTagFile))) {
            writer.write("8080,TCP,tag1\n");
            writer.write("9090,UDP,tag2\n");
            writer.write("443,HTTPS,tag3\n");
        }
    }

    @After
    public void tearDownProtocolMapping() {
        if (tempTagFile != null && tempTagFile.exists()) {
            tempTagFile.delete();
        }
    }

    @Test
    public void testCreateProtocolMap_Valid() {
        HashMap<ProtoKey, String> result = createProtocolMap(tempTagFile.getAbsolutePath());
        assertEquals("tag1", result.get(new ProtoKey("8080", "tcp")));
        assertEquals("tag2", result.get(new ProtoKey("9090", "udp")));
        assertEquals("tag3", result.get(new ProtoKey("443", "https")));
    }


    @Test
    public void testCreateProtocolMap_FileNotFound() {
        HashMap<ProtoKey, String> result = createProtocolMap("file.csv");
        assertTrue("The map should be empty when the file is not found", result.isEmpty());
    }

    @Before
    public void setUpProtocolMappingInvalid() throws IOException {
        tempTagFileInvalid = File.createTempFile("ProtocolTest", ".csv", new File("src/test/TestData/"));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempTagFileInvalid))) {
            writer.write("8080,TCP\n");
            writer.write("443,HTTPS,tag3\n");
        }
    }

    @After
    public void tearDownProtocolMappingInvalid() {
        if (tempTagFileInvalid != null && tempTagFileInvalid.exists()) {
            tempTagFileInvalid.delete();
        }
    }

    @Test
    public void testCreateProtocolMap_Invalid(){
        HashMap<ProtoKey, String> result = createProtocolMap(tempTagFileInvalid.getAbsolutePath());
        assertEquals("The map should contain only the valid data", 1, result.size());
        assertNull("Invalid format lines must be ignored", result.get(new ProtoKey("8080", "tcp")));
        assertEquals("Valid entry must be present", "tag3", result.get(new ProtoKey("443", "https")));
    }


    @Before
    public void setUp() throws IOException {
        logFile = File.createTempFile("testLog", ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("80 6\n");
            writer.write("443 6\n");
            writer.write("53 17\n");
            writer.write("196 17\n");
        }
        protocolInfoMap = new HashMap<>();
        protocolInfoMap.put("6", "tcp");
        protocolInfoMap.put("17", "udp");

        protoTagMap = new HashMap<>();
        protoTagMap.put(new ProtoKey("80", "tcp"), "tag1");
        protoTagMap.put(new ProtoKey("443", "tcp"), "tag5");
        protoTagMap.put(new ProtoKey("53", "udp"), "tag1");

    }

    @After
    public void tearDown() {
        if (logFile != null && logFile.exists()) {
            logFile.delete();
        }
    }

    @Test
    public void testLogParse() {
        int dstCol = 1;
        int protoCol = 2;
        logParse(logFile.getAbsolutePath(), dstCol, protoCol, protocolInfoMap, protoTagMap);
        assertEquals("Tag count for tag1 should be 2", Integer.valueOf(2), Main.tagMap.get("tag1"));
        assertEquals("Tag count for tag5 should be 1", Integer.valueOf(1), Main.tagMap.get("tag5"));
        assertEquals("Untagged should be 1", Integer.valueOf(1), Main.tagMap.get("untagged"));
        assertEquals("PortProtocolMap should have 1 entry for 80-tcp", Integer.valueOf(1), Main.portProtocolMap.get(new ProtoKey("80", "tcp")));
        assertEquals("PortProtocolMap should have 1 entry for 443-tcp", Integer.valueOf(1), Main.portProtocolMap.get(new ProtoKey("443", "tcp")));
        assertEquals("PortProtocolMap should have 1 entry for 53-udp", Integer.valueOf(1), Main.portProtocolMap.get(new ProtoKey("53", "udp")));
    }
}

