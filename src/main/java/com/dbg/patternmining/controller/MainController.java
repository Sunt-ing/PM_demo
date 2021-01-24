package com.dbg.patternmining.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dbg.patternmining.config.FileConfig;
import com.dbg.patternmining.dao.Link;
import com.dbg.patternmining.dao.Node;
import com.dbg.patternmining.dao.Pattern;
import com.dbg.patternmining.dao.PatternInstance;
import com.dbg.patternmining.models.mainMine.PatKGmine;
import com.dbg.patternmining.utils.InputWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class MainController {

    HashMap<Integer, String> idMap = new HashMap<>();
    HashMap<String, JSONObject> strMap = new HashMap<>();
    HashMap<Integer, Node> nodeLabelMap = new HashMap<>();
    HashMap<Integer, String> linkLabelMap = new HashMap<>();

    final FileConfig fileConfig;

    public MainController(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    @ResponseBody
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String query(@RequestParam(value = "graph") String inputGraph,
                        @RequestParam(value = "dataset") int dataset) {
        InputWriter.writeGraph(fileConfig.getDatasetPath(), inputGraph);
        String[] s = new String[4];
        int kk = 0;
        if (dataset == 1)
            s[0] = "filename=phylogeny.lg";
        else if (dataset == 2)
            s[0] = "filename=sars-cov-2.lg";
        else if (dataset == 3)
            s[0] = "filename=taxonomy.lg";

        s[1] = "coreFileName=input.lg";
        s[2] = "k=10";
        s[3] = Paths.get(fileConfig.getDatasetPath()).toString();
//        System.out.println(">>> " + s[3]);
        PatKGmine patKGmine = new PatKGmine();
        kk = patKGmine.test(s);
        BufferedReader reader = null;
        List<Object> patterns = new ArrayList<>();

        for (int i = 1; i <= kk; i++) {
            Map<String, Object> pattern = new HashMap<>();
            List<Object> instances = new ArrayList<>();
            Map<String, Object> instance = new HashMap<>();
            List<Object> nodes = new ArrayList<>();
            List<Object> links = new ArrayList<>();

            try {
                FileInputStream fileInputStream = new FileInputStream("No_" + i + "_Instances.txt");
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                reader = new BufferedReader(inputStreamReader);
                String tempString;
                while ((tempString = reader.readLine()) != null) {
                    if (tempString.charAt(0) == 'v') {
                        String[] str = tempString.substring(2).split(" ");
                        int typeId = Integer.parseInt(str[1].trim());
                        int id = Integer.parseInt(str[0].trim());
                        String type = nodeLabelMap.get(typeId).getType();
                        Map<String, Object> node = new HashMap<>();
                        node.put("id", id);
                        node.put("type", type);
                        node.put("typeId", typeId);
                        nodes.add(node);
                    } else if (tempString.charAt(0) == 'e') {
                        String[] str = tempString.substring(2).split(" ");
                        int source = Integer.parseInt(str[0]);
                        int target = Integer.parseInt(str[1]);
                        int labelId = Integer.parseInt(str[2]);
                        String label = linkLabelMap.get(labelId);
                        Map<String, Object> link = new HashMap<>();
                        link.put("source", source);
                        link.put("target", target);
                        link.put("label", label);
                        link.put("id", labelId);
                        links.add(link);
                    } else if (tempString.charAt(0) == 'F') {
                        int frequency = Integer.parseInt(tempString.split(":")[1]);
                        pattern.put("frequency", frequency);
                    } else {

                        String[] str = tempString.split(";");
                        for (int j = 0; j < str.length; j++) {
                            List<Object> nodes2 = new ArrayList<>();
                            instance = new HashMap<>();
                            instance.put("links", links);
                            String[] str2 = str[j].split(",");
                            for (int k = 0; k < str2.length; k++) {
                                int id = Integer.parseInt(str2[k].trim());
                                JSONObject jsonObject = strMap.get(idMap.get(id));
                                String nodeJson = JSON.toJSONString((Map<String, Object>) nodes.get(k));
                                JSONObject jsonObject1 = JSON.parseObject(nodeJson);
//                                System.out.println(jsonObject.toJSONString());

                                Map<String, Object> node = (Map<String, Object>) jsonObject1;
                                String label = "";
                                if (jsonObject.containsKey("label")) {
                                    if (dataset != 3) {
                                        String labelContent = JSON.parseArray(jsonObject.get("label").toString()).get(0).toString();
                                        label = JSON.parseObject(labelContent).get("@value").toString();
                                    } else {
                                        label = JSON.parseObject(jsonObject.get("label").toString()).get("@value").toString();
                                    }
                                }

                                if (label.length() > 4 && label.substring(0, 4).equals("NODE")) {
                                    label = "N" + label.substring(label.length() - 3);
                                }
                                String virusType = "";
                                node.put("label", label);
                                nodes2.add(node);

                            }
                            instance.put("nodes", nodes2);
                            instances.add(instance);
                        }

                    }
                }

                Map<String, Object> firstPattern = new HashMap<>();
                firstPattern.put("nodes", nodes);
                firstPattern.put("links", links);
                instances.add(0, firstPattern);
                pattern.put("patternInstance", instances);
                patterns.add(pattern);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Collections.sort(patterns, (o1, o2) -> {
            Map<String, Object> x1 = (Map<String, Object>) o1;
            Map<String, Object> x2 = (Map<String, Object>) o2;
            int x3 = (Integer) x2.get("frequency") - (Integer) x1.get("frequency");
            if (x3 == 0) {
                x3 = ((List<Object>) x2.get("patternInstance")).size() - ((List<Object>) x1.get("patternInstance")).size();
            }
            return x3;
        });
        return JSON.toJSONString(patterns, SerializerFeature.DisableCircularReferenceDetect);
    }

    @ResponseBody
    @RequestMapping(value = "/sidebar", method = RequestMethod.GET)
    public String getSidebar(@RequestParam(value = "dataset") int dataset) {
        String str = "";
        if (dataset == 1) {
            str = "{\"nodes\":[{\"id\":0,\"type\":\"State\"},{\"id\":1,\"type\":\"Branch\"}," +
                    "{\"id\":2,\"type\":\"Host\"},{\"id\":3,\"type\":\"Country\"},{\"id\":4,\"type\":\"Strain\"}]," +
                    "\"links\":[{\"source\":0,\"target\":3,\"id\":0,\"label\":\"from(country)\"}," +
                    "{\"source\":4,\"target\":3,\"id\":0,\"label\":\"from(country)\"}," +
                    "{\"source\":4,\"target\":1,\"id\":1,\"label\":\"mutate from\"}," +
                    "{\"source\":1,\"target\":1,\"id\":1,\"label\":\"mutate from\"}," +
                    "{\"source\":4,\"target\":0,\"id\":2,\"label\":\"from(division)\"}," +
                    "{\"source\":0,\"target\":0,\"id\":2,\"label\":\"from(division)\"}," +
                    "{\"source\":4,\"target\":0,\"id\":3,\"label\":\"from(location)\"}," +
                    "{\"source\":4,\"target\":2,\"id\":4,\"label\":\"host is\"}]}";
        } else if (dataset == 2){
            str = "{\"nodes\":[{\"id\":0,\"type\":\"VirusProtein\"},{\"id\":1,\"type\":\"Host\"}," +
                    "{\"id\":2,\"type\":\"Gene\"},{\"id\":3,\"type\":\"Virus\"}]," +
                    "\"links\":[{\"source\":3,\"target\":0,\"id\":0,\"label\":\"produce\"}," +
                    "{\"source\":3,\"target\":2,\"id\":1,\"label\":\"express\"}," +
                    "{\"source\":2,\"target\":0,\"id\":2,\"label\":\"translate\"}," +
                    "{\"source\":3,\"target\":1,\"id\":3,\"label\":\"host_is\"}]}";
        } else {
            str = "{\"nodes\":[{\"id\":0,\"type\":\"Virus\"},{\"id\":1,\"type\":\"Genus\"},{\"id\":2,\"type\":\"Family\"},{\"id\":3,\"type\":\"Order\"},{\"id\":4,\"type\":\"Species\"},{\"id\":5,\"type\":\"Class\"},{\"id\":6,\"type\":\"Phylum\"},{\"id\":7,\"type\":\"UnknowLineage\"}],\"links\":[{\"source\":4,\"id\":6,\"label\":\"parent\",\"target\":1},{\"source\":0,\"id\":3,\"label\":\"family\",\"target\":2},{\"source\":1,\"id\":6,\"label\":\"parent\",\"target\":2},{\"source\":0,\"id\":0,\"label\":\"phylum\",\"target\":6},{\"source\":0,\"id\":2,\"label\":\"order\",\"target\":3},{\"source\":0,\"id\":6,\"label\":\"parent\",\"target\":4},{\"source\":0,\"id\":4,\"label\":\"genus\",\"target\":1},{\"source\":0,\"id\":5,\"label\":\"species\",\"target\":4},{\"source\":0,\"id\":1,\"label\":\"class\",\"target\":5},{\"source\":0,\"id\":6,\"label\":\"parent\",\"target\":7},{\"source\":7,\"id\":6,\"label\":\"parent\",\"target\":4}]}";
        }
        JSONObject result = JSONObject.parseObject(str);
        init(dataset);
        return result.toJSONString();
    }

    public void init(int dataset) {
        ParserConfig.getGlobalInstance().addAccept("com.taobao.pac.client.sdk.dataobject.");
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        if (dataset == 1) {
            nodeLabelMap.put(0, new Node(0, 0, "State"));
            nodeLabelMap.put(1, new Node(1, 1, "Branch"));
            nodeLabelMap.put(2, new Node(2, 2, "Host"));
            nodeLabelMap.put(3, new Node(3, 3, "Country"));
            nodeLabelMap.put(4, new Node(4, 4, "Strain"));

            linkLabelMap.put(0, "from_country");
            linkLabelMap.put(1, "from_mutate_branch");
            linkLabelMap.put(2, "from_division");
            linkLabelMap.put(3, "from_location");
            linkLabelMap.put(4, "host_is");
        } else if (dataset == 2) {
            nodeLabelMap.put(0, new Node(0, 0, "VirusProtein"));
            nodeLabelMap.put(1, new Node(1, 1, "Host"));
            nodeLabelMap.put(2, new Node(2, 2, "Gene"));
            nodeLabelMap.put(3, new Node(3, 3, "Virus"));

            linkLabelMap.put(0, "produce");
            linkLabelMap.put(1, "express");
            linkLabelMap.put(2, "translate");
            linkLabelMap.put(3, "host_is");
        } else if (dataset == 3) {
            nodeLabelMap.put(0, new Node(0, 0, "Virus"));
            nodeLabelMap.put(1, new Node(1, 1, "Genus"));
            nodeLabelMap.put(2, new Node(2, 2, "Family"));
            nodeLabelMap.put(4, new Node(3, 3, "Order"));
            nodeLabelMap.put(5, new Node(3, 3, "Species"));
            nodeLabelMap.put(6, new Node(3, 3, "Class"));
            nodeLabelMap.put(7, new Node(3, 3, "Phylum"));
            nodeLabelMap.put(8, new Node(3, 3, "UnknowLineage"));

            linkLabelMap.put(0, "parent");
            linkLabelMap.put(1, "family");
            linkLabelMap.put(2, "parent");
            linkLabelMap.put(3, "phylum");
            linkLabelMap.put(4, "order");
            linkLabelMap.put(5, "parent");
            linkLabelMap.put(6, "genus");
            linkLabelMap.put(7, "species");
            linkLabelMap.put(8, "class");
            linkLabelMap.put(9, "parent");
            linkLabelMap.put(10, "parent");
        }

        try {

            File file = null;
            file = new File("mappings" + dataset + ".lg");

            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));

                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                int id;
                String idStr;
                String[] splitStr;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    splitStr = lineTxt.split("=");
                    id = Integer.parseInt(splitStr[0].trim());
                    idStr = splitStr[1].trim();
                    idMap.put(id, idStr);
                }
                bufferedReader.close();
                read.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Parse mappings.lg finish");

        BufferedReader reader = null;
        String jsonContent = "";

        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream("virusnetwork" + dataset + ".json");

            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                jsonContent += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("read virusnetwork.json finish");
        JSONObject jsonObject = JSON.parseObject(jsonContent, Feature.DisableSpecialKeyDetect);
        JSONArray jsonArray = JSON.parseArray(jsonObject.get("@graph").toString());
        System.out.println("parse virusnetwork.json finish");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            strMap.put(jsonObject1.get("@id").toString(), jsonObject1);
        }
    }
}
