package com.in28minutes.microservices;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*public static void main(String[] args) throws IOException {*/
class GetALlMethodsFromJavaFile {
    public static void main(String[] args) throws IOException {
        String pathToScanFiles = "G:\\LARA\\Spring\\Mykong\\HibernateExample\\src\\main\\java\\com\\mkyong\\";// "G:\\LARA\\Spring\\in28minutes\\microservioces\\limits-service\\src\\main\\java\\com\\in28minutes\\microservices\\GetAllMethodsFromJavaFile.java";
        String outputFolder = "G:\\LARA\\Spring\\methods.txt";
        // 1 for getMethods , 2 for getUnusedMethods
        String mode = "2";
        String pathToSerchUnsedMathods = "G:\\LARA\\Spring\\Mykong\\HibernateExample\\src\\main\\java\\com\\mkyong\\";
        String methodFileName = "/method.txt";
        String unusedFileName = "/unusedMethods.txt";
        Scanner in = new Scanner(System.in);
        if (args.length > 0) {
            pathToScanFiles = args[0];
            outputFolder = args[1];
            mode = args[2];
        } else {
            System.out.println("Path of groovy/java files :");
            pathToScanFiles = in.nextLine();
            System.out.println("Output Foldermfiles :");
            outputFolder = in.nextLine();
            System.out.println("Path to search unused files :");
            pathToSerchUnsedMathods = in.nextLine();
            System.out.println("mode of operation 1-->getAllMethods() 2-->getUnusedMethods():: ");
            mode = in.nextLine();
        }

        List<String> listOfMethods = getAllMethods(pathToScanFiles);
        saveMethodsFromAllFiles(outputFolder + methodFileName, listOfMethods);
        exitTheFlow(in);
        if(mode.equals("2")){
            List<String> listOfUnusedMethods = getAllUnusedMethods(pathToSerchUnsedMathods, listOfMethods);
            saveMethodsFromAllFiles(outputFolder + unusedFileName, listOfUnusedMethods);
        }
        System.out.println("Please check the paths for output files --> "+outputFolder);
    }

    private static void exitTheFlow(Scanner in) {
        System.out.println("want to exit?? :: ");
        String isExit = in.nextLine();
        if(isExit.toUpperCase().equals("YES")){
            System.exit(0);
        }
    }

    private static List<String> getAllUnusedMethods(String pathToSerchUnsedMathods, List<String> listOfMethods)
            throws IOException {
                Map<String, List<String>> mapOfMethods = new HashMap<>();
                List<String>  methods = listOfMethods.stream().filter(method -> !method.startsWith("-")).collect(Collectors.toList());
         methods.forEach(method ->{
            mapOfMethods.put(method, new ArrayList<>());
         });
        Consumer<Path> getUnusedMethods = new Consumer<Path>() {
            @Override
            public void accept(Path file) {
                String fileName = file.getFileName().toString();
                String regex = "\\s++$";
                System.out.println("FileName --> " + fileName);
                if (fileName.endsWith("java") || fileName.endsWith("groovy")) {
                    try (Stream<String> lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            line = line.replaceAll(regex, "").trim();
                            String modifiedLine = line;
                            methods.forEach(method -> {
                                if (modifiedLine.contains(method)) {
                                    System.out.println("@@ " + method + " " + modifiedLine);
                                    if (!isMethod(modifiedLine)) {
                                        System.out.println(method + "" + modifiedLine);
                                        String methodCall = "method = " + method + " :: methodCall = " + modifiedLine
                                                + " fileName =" + fileName;
                                        List<String> list = mapOfMethods.get(method);
                                        if (list == null) {
                                            list = new ArrayList<>();
                                        }
                                        list.add(methodCall);
                                        mapOfMethods.put(method, list);
                                    }
                                }
                            });
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        try (Stream<Path> paths = Files.walk(Paths.get(pathToSerchUnsedMathods))) {
            paths.filter(Files::isRegularFile).forEach(getUnusedMethods);
        }
        List<String> outputList = new ArrayList<>();
        mapOfMethods.forEach((key, value) -> {
            String content = "";
            content = content+"---------------" + key + "---------------"+"\n";
            if (value != null) {
                for (String item : value) {
                    content = content + "\n" + item;
                }
            }
            outputList.add(content);
            outputList.add("--------------------------------------------------------------");
        });
        return outputList;
    }

    private static void saveMethodsFromAllFiles(String outputFolder, List<String> listOfMethods) throws IOException {
        System.out.println("Saving all methods to file : " + outputFolder);
        Path out = Paths.get(outputFolder);
        Files.write(out, listOfMethods, Charset.defaultCharset());
        System.out.println("Done!!!!!!");
    }

    private static List<String> getAllMethods(String pathToScanFiles) throws IOException {
        System.out.println("reading files from folder : " + pathToScanFiles);
        List<String> methodsFromFile = new ArrayList<>();
        Consumer<Path> printConsumer = new Consumer<Path>() {
            public void accept(Path file) {
                String fileName = file.getFileName().toString();
                System.out.println("FileName --> " + fileName);
                if (fileName.endsWith("java") || fileName.endsWith("groovy")) {
                    methodsFromFile.add("---------" + fileName + "-----------");
                    try (Stream<String> lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            String regex = "\\s++$";
                            line = line.replaceAll(regex, "").trim();
                            if (isMethod(line)) {
                                System.out.println(line);
                                String temStr = line.substring(0, line.indexOf("("));
                                String method = line.substring(temStr.lastIndexOf(" "), (temStr.length())).trim();
                                System.out.println("MethodsName -->" + method);
                                methodsFromFile.add(method);
                            }
                        });
                        methodsFromFile.forEach(System.out::println);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        try (Stream<Path> paths = Files.walk(Paths.get(pathToScanFiles))) {
            paths.filter(Files::isRegularFile).forEach(printConsumer);
        }
        return methodsFromFile;
    }

    private static boolean isMethod(String line) {
        return (line.trim().contains("public") || line.contains("private") || line.contains("def"))
                && line.trim().contains("{") && line.trim().contains("(") && line.trim().contains(")")
                && !line.trim().startsWith("//") && !line.trim().startsWith("/*") && !line.trim().startsWith("*");
    }

}