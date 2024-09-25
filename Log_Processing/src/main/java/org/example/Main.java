package org.example;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Objects;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
class ProtoKey {
    String dst;
    String proto;

    ProtoKey(String dst, String proto) {
        this.dst = dst.toLowerCase();
        this.proto = proto.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtoKey)) return false;
        ProtoKey protoKey = (ProtoKey) o;
        return Objects.equals(dst, protoKey.dst) && Objects.equals(proto, protoKey.proto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dst, proto);
    }
}


public class Main {

    public static HashMap<String, Integer> tagMap = new HashMap<>();
    public static HashMap<ProtoKey, Integer> portProtocolMap = new HashMap<>();
     public static HashMap protocolInfo(String filePath){
         HashMap<String, String> protocolInfoMap  = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length == 2) {
                        String column1 = values[0].trim();
                        String column2 = values[1].trim();
                        protocolInfoMap.put(column1,column2.toLowerCase());
                    }
                }
            }catch (NoSuchFileException e) {
                System.err.println("File not found: " + e.getMessage());
            }catch (IOException e) {
                System.err.println("IO error occurred: " + e.getMessage());
            }catch (Exception e) {
                e.printStackTrace();
            }
            return protocolInfoMap;
        }

    public static HashMap createProtocolMap(String filePath) {
        HashMap<ProtoKey, String> ProtoTagMap  = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    String dstport = values[0].trim();
                    String proto = values[1].trim().toLowerCase();
                    String tag = values[2].trim().toLowerCase();
                    ProtoTagMap.put(new ProtoKey(dstport, proto), tag);
                }
            }
        }catch (NoSuchFileException e) {
            System.err.println("File not found: " + e.getMessage());
        }catch (IOException e) {
            System.err.println("IO error occurred: " + e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ProtoTagMap;
    }


    public static void logParse(String logFilePath,int dstCol, int protoCol,  HashMap<String, String> protocolInfoMap , HashMap<ProtoKey, String> ProtoTagMap) {
            try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split("\\s+");
                    if (values.length >= Math.max(dstCol,protoCol)) {
                        String dstport = values[dstCol-1]; // zero based indexing
                        String protoNum = values[protoCol-1];
                        ProtoKey key = new ProtoKey(dstport, protocolInfoMap.getOrDefault(protoNum, "unknown"));
                        String tag = ProtoTagMap.getOrDefault(key,"untagged");
                        tagMap.put(tag, tagMap.getOrDefault(tag, 0) + 1);
                        portProtocolMap.put(key, portProtocolMap.getOrDefault(key, 0) + 1);
                    }
                }
            }catch (NoSuchFileException e) {
                System.err.println("File not found: " + e.getMessage());
            }catch (IOException e) {
                System.err.println("IO error occurred: " + e.getMessage());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    private static void writeOutputToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/tag_frequency_output.txt"))) {
            writer.write("Tag Frequency Map:\n");
            for (String tag : tagMap.keySet()) {
                writer.write(tag + ", " + tagMap.get(tag) + "\n");
            }
            System.out.println("Tag Frequency output written to data/tag_frequency_output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/Port_Protocol_Frequency_output.txt"))) {
            writer.write("\nPort-Protocol Frequency Map:\n");
            for (ProtoKey key : portProtocolMap.keySet()) {
                writer.write(key.dst +" ," + key.proto+ ", " + portProtocolMap.get(key) + "\n");
            }
            System.out.println("Port-Protocol Frequency output written to data/tag_frequency_output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args)
    {
        HashMap<String, String> protocolInfoMap = protocolInfo("data/Protocol.csv");

        System.out.println("Created mapping between protocol name and number ");

        HashMap<ProtoKey, String> ProtoTagMap = createProtocolMap("data/tag_mappings.csv");

        System.out.println("Created mappings between <dstport,protocol> and tag ");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the column number for destination port in log file : ");
        int dstColumn = scanner.nextInt();
        System.out.print("Enter the column number for protocol in log file : ");
        int protoColumn = scanner.nextInt();

        logParse("data/log.txt",dstColumn,protoColumn,protocolInfoMap,ProtoTagMap);

        writeOutputToFile();

    }
}