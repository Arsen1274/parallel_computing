import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentLinkedQueue;

public class IndexThread extends Thread {
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> IndexMap;
    private int NUNMBER_THREADS;
    private int N_end;
    private int N_start;
    private int threadId;
    private String rootDataPath;
    private LinkedList<String> inDataPathsList;
    private LinkedList<String> stopWords;


    IndexThread(int threadId, int NUNMBER_THREADS, int N_start, int N_end, String rootDataPath, LinkedList<String> inDataPathsList, LinkedList<String> stopWords, ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> IndexMap) {
        this.threadId = threadId;
        this.NUNMBER_THREADS = NUNMBER_THREADS;
        this.N_end = N_end;
        this.N_start = N_start;
        this.rootDataPath = rootDataPath;
        this.stopWords = stopWords;
        this.IndexMap = IndexMap;
        this.inDataPathsList = inDataPathsList;
    }

    public void run() {
        int N_end = this.N_end;
        int N_start = this.N_start;
        String dir_path = this.rootDataPath;

        for (int j = 0; j < this.inDataPathsList.size(); j++) {
            String tempFilePath = this.inDataPathsList.get(j);
            if (j == (this.inDataPathsList.size() - 1)) {
                N_start = N_start * 4;
                N_end = N_end * 4;
            }
            for (int i = N_start + this.threadId; i < N_end; i += this.NUNMBER_THREADS) {
                String tempPath = fileWithMark(i, tempFilePath);

                File file = new File(tempPath);
                try {
                    FileReader fileReader = null;
                    fileReader = new FileReader(file);

                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String fileLine =  bufferedReader.readLine();

                    while (fileLine != null) {
                        fileLine = InvertedIndex.stylize(fileLine);
                        String[] wordsArr = fileLine.split(" ");
                        for (String tempWord : wordsArr) {
                            if (tempWord.length() == 0 || this.stopWords.contains(tempWord)) {
                                continue;
                            }
                            if (IndexMap.containsKey(tempWord)) {
                                ConcurrentLinkedQueue<String> currentList = IndexMap.get(tempWord);
                                if (!currentList.contains(tempPath)) {
                                    tempPath = tempPath.replace(dir_path, "");
                                    currentList.add(tempPath);
                                } else {
                                    continue;
                                }
                            } else {
                                ConcurrentLinkedQueue<String> tempList = new ConcurrentLinkedQueue<>();
                                tempList.add(tempPath);
                                IndexMap.put(tempWord, tempList);
                            }
                        }
                        fileLine = bufferedReader.readLine();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String fileWithMark(int num, String dir) {
        int minMark = 0;
        int maxMark = 10;
        if (dir.contains("/neg/")) {
            maxMark = 4;
        } else if (dir.contains("/pos/")) {
            minMark = 5;
        }
        String path = "";
        for (int i = minMark; i <= maxMark; i++) {

            path = dir + String.valueOf(num) + "_" + String.valueOf(i) + ".txt";
            File f1 = new File(path);
            if (f1.exists()) {
                return path;
            }
        }
        return "Err last path:" + path;
    }

}
