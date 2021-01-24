package com.dbg.patternmining.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbg.patternmining.config.FileConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

public class InputWriter {

    static HashMap<Integer, Integer> idMap = new HashMap<>();

    public static void writeGraph(String dataPath, String graphStr) {
        BufferedWriter out = null;
        try {
            boolean flag = false;
//            System.out.println(">>> file path in writeGraph: " + Paths.get(dataPath, "/input.lg").toString());
            File file = new File(Paths.get(dataPath, "/input.lg").toString());
            out = new BufferedWriter(new FileWriter(file));
            System.out.println("Start write file");
            out.write("# t 1");
            out.newLine();
            JSONObject graph = JSON.parseObject(graphStr);
            JSONArray nodeArray = JSON.parseArray(graph.get("nodes").toString());
            for (int i = 0; i < nodeArray.size(); i++) {
                JSONObject jsonObject = JSON.parseObject(nodeArray.get(i).toString());
                int id;
                if (jsonObject.containsKey("typeId")) {
                    flag = true;
                    id = Integer.parseInt(jsonObject.get("typeId").toString());
                } else {
                    id = Integer.parseInt(jsonObject.get("id").toString());
                }
                out.write("v " + i + " " + id);
                idMap.put(id, i);
                out.newLine();
            }
            JSONArray linkArray = JSON.parseArray(graph.get("links").toString());
            for (int i = 0; i < linkArray.size(); i++) {
                JSONObject jsonObject = JSON.parseObject(linkArray.get(i).toString());
                int source = Integer.parseInt(jsonObject.get("source").toString());
                int target = Integer.parseInt(jsonObject.get("target").toString());
                int id = Integer.parseInt(jsonObject.get("id").toString());
                if (!flag) {
                    out.write("e " + idMap.get(source) + " " + idMap.get(target) + " " + id);
                } else {
                    out.write("e " + source + " " + target + " " + id);
                }
                out.newLine();
            }
            System.out.println("Write file finished");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
