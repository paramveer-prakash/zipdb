package com.zipdb.common;

public class StartupBanner {

    public static void printBanner() {
        String banner = """
              ______  _        _  ______  ______ 
             |___  / (_)      | ||___  / |___  /
                / /   _   ___ | |   / /     / / 
               / /   | | / _ \\| |  / /     / /  
              / /__  | ||  __/| | / /__   / /__ 
             /_____| |_| \\___||_||_____| /_____| 
                                                 
                """;
        System.out.println(banner);
        System.out.println(" :: ZipDB :: (Java Redis Clone)");
        System.out.println();
    }
}
