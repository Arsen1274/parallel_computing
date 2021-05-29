import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Index_thread extends Thread {
    private ConcurrentHashMap<String, LinkedList<String>> my_map;
    private int NUNMBER_THREADS;
    private int N_end;
    private int N_start;
    private int thread_id;
    private String dir_path;
    private LinkedList<String> dir_sources;
    private LinkedList<String> stop_words;


    Index_thread(int thread_id, int NUNMBER_THREADS, int N_start, int N_end, String dir_path, LinkedList<String> dir_source, LinkedList<String> stop_words, ConcurrentHashMap<String, LinkedList<String>> my_map) {
        this.thread_id = thread_id;
        this.NUNMBER_THREADS = NUNMBER_THREADS;
        this.N_end = N_end;
        this.N_start = N_start;
        this.dir_path = dir_path;
        this.stop_words = stop_words;
        this.my_map = my_map;
        this.dir_sources = dir_source;
    }

    public void run() {
        int N_end = this.N_end;
        int N_start = this.N_start;
        String dir_path = this.dir_path;

        for (int j = 0; j < this.dir_sources.size(); j++) {
            String temp_source_path = this.dir_sources.get(j);
            if (j == (this.dir_sources.size() - 1)) {
                N_start = N_start * 4;
                N_end = N_end * 4;
            }
            for (int i = N_start + this.thread_id; i < N_end; i += this.NUNMBER_THREADS) {
                String temp_path = Inverted_index.file_with_mark(i, temp_source_path);

                File file = new File(temp_path);
                try {
                    FileReader fr = null;
                    fr = new FileReader(file);

                    BufferedReader reader = new BufferedReader(fr);
                    String line = "";
                    line = reader.readLine();

                    while (line != null) {
                        line = Inverted_index.stylize(line);
                        String[] words = line.split(" ");
                        for (String word : words) {
                            if (word.length() == 0 || this.stop_words.contains(word)) {
                                continue;
                            }
                            if (my_map.containsKey(word)) {
                                LinkedList<String> current_list = my_map.get(word);
                                if (!current_list.contains(temp_path)) {
                                    temp_path = temp_path.replace(dir_path, "");
                                    current_list.add(temp_path);
                                } else {
                                    continue;
                                }
                            } else {
                                LinkedList<String> current_list = new LinkedList<String>();
                                current_list.add(temp_path);
                                my_map.put(word, current_list);
                            }
                        }
                        line = reader.readLine();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
