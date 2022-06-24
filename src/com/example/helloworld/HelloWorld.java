package com.example.helloworld;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

class Node {
    char value;
    int count;
    String tag;
    Map<Character, Node> children;

    Node(char value, String tag) {
        this.value = value;
        this.count = 0;
        this.tag = tag;
        this.children = new HashMap<Character, Node>();
    }
}

class Trie {

    String DUPLICATE = "duplicate";
    String SINGULAR = "singular";

    Node root;

    Trie() {
        this.root = new Node('*', null);
    }
    public void build(List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            add(values.get(i));
        }
    }
     public boolean isLeaf(Node node){
        return node.children == null;
     }

    public boolean isDuplicate(Node node){
        return node.tag == this.DUPLICATE;
    }

    private void _printDuplicates(Node node, List<Character> phrase, List<String> result) {

        phrase.add(node.value);
        if(isDuplicate(node)){
            List<Character> copy_phrase = new ArrayList<>(phrase);
            copy_phrase.remove(0);
            StringBuilder sb = new StringBuilder();
            for (Character ch : copy_phrase) {
                sb.append(ch);
            }
            String clean_phrase = sb.toString();
            System.out.println(clean_phrase);
            result.add(clean_phrase);

        }
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            _printDuplicates(entry.getValue(), phrase, result);
        }
        int index = phrase.size() - 1;
        phrase.remove(index);
    }

    public List<String> printDuplicates(Boolean return_result) {
        Node node = this.root;
        List<Character> phrase = new ArrayList<>();
        List<String> result = new ArrayList<>();
        _printDuplicates(node, phrase, result);
        if(!return_result)
            result = null;
        return result;
    }
    public void add(String value) {
        Map<Character, Node> children = this.root.children;
        int length = value.length();
        int count = 0;
        Node node = null;

        while (count < length) {
            char el = value.charAt(count);
            Node curr_node = children.get(el);
            if (curr_node != null) {
                node = curr_node;
            }
            else {
                node = new Node(el, null);
                children.put(el, node);
            }
            count += 1;
            children = node.children;
            node.tag = this.SINGULAR;
            node.count += 1;
        }
        if (node.count > 1) {
            node.tag = this.DUPLICATE;
        }
    }

}
class HelloWorld {
    public static void main(String[] args) {
        String filename = "/Users/mkosecki/Downloads/algorithms part dataset.csv";

        try {
            List list = readFile(filename);
            List parsed_list = parseList(list);
            // complexity max(O(n) + O(k) + O(k - s) + O(d)) = O(n);
            // (n => d) > k > k - s
            List deduplicated_list = deduplicate(parsed_list, false);


            List<String> game_name_data = new ArrayList<>();
            for(int i = 0; i < parsed_list.size();i++){
                List<String> el = (List)parsed_list.get(i);
                game_name_data.add(el.get(1));
            }

            Trie trie = new Trie();
            trie.build(game_name_data);
            // complexity O(m * log(n)); m - max. phrase length; n - number of phrases,
            List result = trie.printDuplicates(true);
            System.out.println(result.size());

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    private static List readFile(String fileName) throws IOException {

        List<String> result = new ArrayList<>();
        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = br.readLine()) != null) {
                result.add(line.strip());
            }
            result.remove(0);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result;
    }

    private static List<List> parseList(List<String> list) {
        Pattern pattern_1 = Pattern.compile("^\"(\\d+),\"{2,}(.+)\"{2,},(\\w+),(\\d+\\.\\d+),(\\d+)\";");
        Pattern pattern_2 = Pattern.compile("^\"(\\d+),\"{2,}(.+)\"{2,},(\\w+),(\\d+\\.\\d+),(\\d+)");
        List<List> parsed_list = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String phrase = list.get(i);
            Matcher matcher = pattern_1.matcher(phrase);

            if(!matcher.find()) // well that is not the nicest approach :)
                matcher = pattern_2.matcher(phrase);
            else
                matcher = pattern_1.matcher(phrase);

            while (matcher.find()) {
                // Get the matching string

                List<String> matches = new ArrayList<>();
                matches.add(matcher.group(1));
                matches.add(matcher.group(2));
                matches.add(matcher.group(3));
                matches.add(matcher.group(4));
                matches.add(matcher.group(5));
                parsed_list.add(matches);

            }
        }
        return parsed_list;
    }

    private static List deduplicate(List<List> list, boolean nice_print){
        Map<String, Map<String, Object>> dupl_info_total = new HashMap<String, Map<String, Object>>();

        // time complexity O(n); n - number of lines
        for (int i = 0; i < list.size(); i++) {
            String hash_id = (String) list.get(i).get(0) + (String) list.get(i).get(1) + (String) list.get(i).get(2) + (String) list.get(i).get(3) +
                    (String) list.get(i).get(4);

            if (dupl_info_total.get(hash_id) == null){
                Map<String, Object> partial_info = new HashMap<String, Object>();
                partial_info.put("count", 1);
                List idxs = new ArrayList<>();
                idxs.add(i + 1);
                partial_info.put("idx", idxs);
                dupl_info_total.put(hash_id, partial_info);
            }else{
                Map<String, Object> partial_info = dupl_info_total.get(hash_id);
                partial_info.put("count", (Integer)partial_info.get("count") + 1);
                List partial_idx_list = (List)partial_info.get("idx");
                partial_idx_list.add(i + 1);
                partial_info.put("idx", partial_idx_list);
            }
        }

        // time complexity O(k); k - number of unique lines
        Map<String, Map<String, Object>> dupl_info_reduce = new HashMap<String, Map<String, Object>>();
        for (Map.Entry<String, Map<String, Object>> entry : dupl_info_total.entrySet()) {
            Integer count = (Integer) entry.getValue().get("count");
            if (count > 1) {
                dupl_info_reduce.put(entry.getKey(), entry.getValue());
            }
        }

        // time complexity O(k - s); s - number of unique phrases with count == 1
        List<List> dedup_list = new ArrayList<>();
        Set<Integer> remove_indices = new HashSet<Integer>();
        for (Map.Entry<String, Map<String, Object>> entry : dupl_info_reduce.entrySet()) {
            List idxs = (List) entry.getValue().get("idx");
            for (int i = 0; i < idxs.size(); i++) {
                remove_indices.add((Integer) idxs.get(i));
            }
        }

        // time complexity O(d); d - number of duplicated lines
        for (int i = 0; i < list.size(); i++){
            if(!remove_indices.contains(i)){
                dedup_list.add(list.get(i));
            }
        }
        if (nice_print){
            System.out.println(dupl_info_reduce);
        }
        return dedup_list;
    }

}