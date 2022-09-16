package org.fia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.SECONDS;

public class PatrCollect {
    String node;

    HttpResponse<String> response;
    HttpRequest request, request_cluster, request_master;
    HttpClient client = HttpClient.newHttpClient();
    Collection<Metrics> collector = new HashSet<>();


    public PatrCollect(String node) throws URISyntaxException {

        this.request = HttpRequest.newBuilder()
                .uri(new URI(node + ":8008/patroni"))
                .timeout(Duration.of(1, SECONDS))
                .GET()
                .build();
        this.request_cluster = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .GET()
                .uri(new URI(node + ":8008/cluster")).build();
        this.request_master = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .GET()
                .uri(new URI(node + ":8008/master")).build();
        this.node = node;
        App.myCache.setCollector(collector);


    }

    public HttpResponse<String> getResp(HttpRequest req) throws ExecutionException {
        try {
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            this.response = response;
        } catch (InterruptedException | IOException e) {
        } //HttpTimeoutException
        return response;
    }

    Map<String, String[][]> createKV(String s) {
        Map<String, String[][]> map = new HashMap<>();
        String[] k;
        String[] v;
        String[][] m;
        switch (s) {
            case "nodeUp":
                k = new String[3];
                k[0] = "state";
                k[1] = "node_name";
                k[2] = "node";
                v = new String[3];
                m = new String[2][3];
                m[0] = k;
                m[1] = v;
                map.put(s, m);
                break;
            case "null_nodeUp":
                k = new String[3];
                k[0] = "state";
                k[1] = "node_name";
                k[2] = "node";
                v = new String[3];
                for (int i = 0; i < v.length; i++) {
                    v[i] = "out of service";
                }
                ;
                m = new String[2][3];
                m[0] = k;
                m[1] = v;
                map.put(s, m);
                break;
            case "members":
                k = new String[8];
                v = new String[8];
                k[0] = "state";
                k[1] = "role";
                k[2] = "postgresql_ver";
                k[3] = "database_id";
                k[4] = "timeline";
                k[5] = "patroni_ver";
                k[6] = "cluster_scope";
                k[7] = "node";
                m = new String[2][8];
                m[0] = k;
                m[1] = v;
                map.put(s, m);
                break;
            case "null_members":
                k = new String[8];
                v = new String[8];
                k[0] = "state";
                k[1] = "role";
                k[2] = "postgresql_ver";
                k[3] = "database_id";
                k[4] = "timeline";
                k[5] = "patroni_ver";
                k[6] = "cluster_scope";
                k[7] = "node";
                for (int i = 0; i < v.length; i++) {
                    v[i] = "out of service";
                }
                ;
                m = new String[2][8];
                m[0] = k;
                m[1] = v;
                map.put(s, m);
                break;
            case "leader":
                k = new String[1];
                v = new String[1];
                k[0] = "leader_node";
                m = new String[2][1];
                m[0] = k;
                m[1] = v;
                map.put(s, m);
                break;
        }
        return map;
    }


    public void checkLeader() {
        String[] k = createKV("leader").get("leader")[0];
        String[] v = createKV("leader").get("leader")[1];
        Metrics leader = new Metrics("patroni_leader");
        try {
            getResp(request_master);
            if (response.statusCode() == 200) {

                v[0] = strQuote(node.split("//")[1]);
                leader.setLabels(k, v);
                leader.setValue("1");
                collector.add(leader);
                replInfo();

            }
        } catch (NullPointerException | ExecutionException e) {
        }

    }

    String strQuote(String s) {
        return "\"" + s + "\"";
    }

    public void checkNode() throws ExecutionException {
        String[] k = createKV("nodeUp").get("nodeUp")[0];
        String[] v = createKV("nodeUp").get("nodeUp")[1];

        Metrics nodeUp = new Metrics("patroni_node_up");

        try {

            JsonNode jsonNodeCluster = (new ObjectMapper()).readTree(getResp(request_cluster).body());
            for (JsonNode e : jsonNodeCluster.get("members")) {
                if (e.get("host").asText().equals(this.node.split("//")[1])) {
                    v[0] = strQuote((e.get("state").asText())); // label for state
                    v[1] = strQuote((e.get("name").asText())); // label for node_name
                    v[2] = strQuote(node.split("//")[1]); // label for node
                }
            }
            nodeUp.setLabels(k, v);
            nodeUp.setValue("1");
            collector.add(nodeUp);
            App.myCache.loadingCache.invalidate("patroni_node_up");
            App.myCache.loadingCache.get("patroni_node_up");


        } catch (NullPointerException | JsonProcessingException e) {
            String[][] m;
            if (App.myCache.loadingCache.size() > 0) {

                m = App.myCache.loadingCache.get("patroni_node_up");
                nodeUp.setLabels(m[0], m[1]);
                nodeUp.setValue("0");
                collector.add(nodeUp);
            } else {
                m = createKV("null_nodeUp").get("null_nodeUp");
                nodeUp.setLabels(m[0], m[1]);
                nodeUp.setValue("0");
                collector.add(nodeUp);
            }

        }
    }

    public void getMembers() throws ExecutionException {
        String[] k = createKV("members").get("members")[0];
        String[] v = createKV("members").get("members")[1];
        Metrics members = new Metrics("patroni_members");

        try {

            JsonNode jsonNode = (new ObjectMapper()).readTree(getResp(request).body());

            v[0] = strQuote(jsonNode.get("state").asText()); // for label state
            v[1] = strQuote(jsonNode.get("role").asText());  // for label role
            v[2] = strQuote(jsonNode.get("server_version").asText()); // for label postgresql_ver
            v[3] = strQuote(jsonNode.get("database_system_identifier").asText()); // for label database_id
            v[4] = strQuote(jsonNode.get("timeline").asText()); // for label timeline
            v[5] = strQuote(jsonNode.get("patroni").get("version").asText()); // for label patroni_ver
            v[6] = strQuote(jsonNode.get("patroni").get("scope").asText()); // for label cluster_scope
            v[7] = strQuote(node);
            members.setLabels(k, v);
            members.setValue("1");
            collector.add(members);
            App.myCache.loadingCache.invalidate("patroni_members");
            App.myCache.loadingCache.get("patroni_members");

        } catch (NullPointerException | JsonProcessingException | ExecutionException e) {
            String[][] m;
            if (App.myCache.loadingCache.size() > 0) {

                m = App.myCache.loadingCache.get("patroni_members");
                members.setLabels(m[0], m[1]);
                members.setValue("0");
                collector.add(members);
            } else {
                m = createKV("null_members").get("null_members");
                members.setLabels(m[0], m[1]);
                members.setValue("0");
                collector.add(members);
            }
        }
        ;
    }

    void replInfo() {
        String[] k = new String[1];
        k[0] = "info";
        String[] v = new String[1];
        Metrics replI = new Metrics("patroni_replication_info");
        try {
            JsonNode repl = (new ObjectMapper()).readTree(getResp(request).body()).get("replication");
            Integer nodeNum = repl.size();
            StringBuilder srepl = new StringBuilder();

            for (int i = 1; i <= nodeNum; i++) {

                String[] val = {repl.get(i - 1).get("client_addr").asText(), repl.get(i - 1).get("application_name").asText(), repl.get(i - 1).get("state").asText(), repl.get(i - 1).get("sync_state").asText()};

                srepl.append(String.format("node" + String.valueOf(i) + ":%1s " + "name_of_node" + String.valueOf(i) + ":%2s " + "state_of_node" + String.valueOf(i) + ":%3s " + "sync_state_of_node" + String.valueOf(i) + ":%3s ", val));
            }

            v[0] = strQuote(srepl.toString());
            replI.setLabels(k, v);
            replI.setValue("1");
            collector.add(replI);


        } catch (NullPointerException | JsonProcessingException | ExecutionException e) {
        }
    }

    public String expose() {

        StringBuilder promhtml = new StringBuilder();


        for (Metrics i : collector) {

            String t = i.name + i.getLabels() + " " + i.value + "\n";
            promhtml.append(t);
        }

        return promhtml.toString();

    }

}

