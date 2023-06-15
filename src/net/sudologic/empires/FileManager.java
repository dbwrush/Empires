package net.sudologic.empires;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {
    private static File file;
    public FileManager() {
        file = new File("data.txt");
        createFile();
    }

    public static void createFile() {
        try {
            if(file.createNewFile()) {
                System.out.println("Found no existing save data. Creating new file");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An I/O error occurred.");
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readFile() {
        ArrayList<String> contents = new ArrayList<String>();
        try{
            Scanner sc = new Scanner(file);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                contents.add(line);
            }
        }catch(FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }
        return contents;
    }

    public static void writeFile(ArrayList<String> contents) {
        try{
            FileWriter writer = new FileWriter(file);
            for(String string : contents) {
                System.out.println("[FileWriter] Writing {" + string + "} to file.");
                writer.write(string);
                writer.append(System.getProperty("line.separator"));
            }
            writer.close();
        }catch(IOException e) {
            System.out.println("An I/O error occurred");
            e.printStackTrace();
        }
    }
}
