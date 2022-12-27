package org.example;

import java.io.File;

public class Util {
    public static void main(String[] args) {
        File root = new File("D:\\PNG-cards-1.3");
        File[] files = root.listFiles();
        for (File file : files) {
            String name = file.getName(), newName = null;
            if (name.startsWith("2")) {
                newName = replace(name, "2", "two");
            } else if (name.startsWith("3")) {
                newName = replace(name, "3", "three");
            } else if (name.startsWith("4")) {
                newName = replace(name, "4", "four");
            } else if (name.startsWith("5")) {
                newName = replace(name, "5", "five");
            } else if (name.startsWith("6")) {
                newName = replace(name, "6", "six");
            } else if (name.startsWith("7")) {
                newName = replace(name, "7", "seven");
            } else if (name.startsWith("8")) {
                newName = replace(name, "8", "eight");
            } else if (name.startsWith("9")) {
                newName = replace(name, "9", "nine");
            } else if (name.startsWith("10")) {
                newName = replace(name, "10", "ten");
            } else if (name.startsWith("ace") && name.contains("2")) {
                file.delete();
            } else if (name.startsWith("jack") && !name.contains("2")) {
                file.delete();
            } else if (name.startsWith("king") && !name.contains("2")) {
                file.delete();
            } else if (name.startsWith("queen") && !name.contains("2")) {
                file.delete();
            } else if (name.startsWith("jack") && name.contains("2")) {
                newName = replace(name, "2", "");
            } else if (name.startsWith("king") && name.contains("2")) {
                newName = replace(name, "2", "");
            } else if (name.startsWith("queen") && name.contains("2")) {
                newName = replace(name, "2", "");
            }
            String absolutePath = file.getAbsolutePath();
            if (newName != null) {
                String newAbsolutePath = absolutePath.replace(name, newName);
                file.renameTo(new File(newAbsolutePath));
            }
        }
    }

    private static String replace(String oldName, String replace, String replacement) {
        return oldName.replace(replace, replacement);
    }
}
