package org.hps;


import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class WaitUntilReady {

    private static final Logger logger = LoggerFactory.getLogger(WaitUntilReady.class);

    @SuppressWarnings("java:S106")
    public static void main(String[] args) throws InterruptedException {

        LocalDateTime now;

        BasicConfigurator.configure();
        logger.info("hello Mazen...");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

 /*
        now = LocalDateTime.now();
        logger.info(dtf.format(now));

        Map<String,String> map= new HashMap<>();
        map.put("BOOTSTRAP_SERVERS","my-cluster-kafka-bootstrap:9092");
        map.put("TOPIC","testtopic3");

        map.put("GROUP_ID","testgroup3");
        map.put("SLEEP","1000");
        map.put("MESSAGE_COUNT","1000000");
        map.put("max.poll.records=1","1");

        EnvVar env = new EnvVar;*/


        List<EnvVar> e = new ArrayList<>();

        EnvVar e1 = new EnvVar();
        e1.setName("BOOTSTRAP_SERVERS");
        e1.setValue("my-cluster-kafka-bootstrap:9092");

        EnvVar e2 = new EnvVar();
        e2.setName("TOPIC");
        e2.setValue("testtopic3");


        EnvVar e3 = new EnvVar();
        e3.setName("GROUP_ID");
        e3.setValue("testgroup3");


        EnvVar e4 = new EnvVar();
        e4.setName("SLEEP");
        e4.setValue("1000");


        EnvVar e5 = new EnvVar();
        e5.setName("MESSAGE_COUNT");
        e5.setValue("1000000");


        EnvVar e6 = new EnvVar();
        e6.setName("max.poll.records");
        e6.setValue("1");

        e.add(e6);
        e.add(e1);
        e.add(e2);
        e.add(e3);
        e.add(e4);
        e.add(e5);
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
            final Pod pod = client.pods().inNamespace(namespace).create(
                    new PodBuilder()
                            .withNewMetadata().withName("singleton").withLabels(Collections.singletonMap("app", "singleton")).endMetadata()
                            .withNewSpec()
                            .addNewContainer()
                            .withName("singleton")
                            .withImage("docker.io/mezzeddine/lagawareconsumertest:latest")
                            //.withCommand("sh", "-c", "echo 'The app is running!'; sleep 10")
                            .withEnv(e)
                            .endContainer()
//                            .addNewInitContainer()
//                            .withName("init-myservice")
//                            .withImage("busybox:1.28")
//                            .withCommand("sh", "-c", "echo 'inititalizing...'; sleep 5")
//                            .endInitContainer()
                            .endSpec()
                            .build()
            );


            logger.info("Pod created, waiting for it to get ready...");

            logger.info("current time {}", System.currentTimeMillis());

            now = LocalDateTime.now();
            logger.info(dtf.format(now));

            client.resource(pod).inNamespace(namespace).waitUntilReady(30, TimeUnit.SECONDS);
            logger.info("Pod is ready now");

            logger.info("current time {}", System.currentTimeMillis());

            now = LocalDateTime.now();
            logger.info(dtf.format(now));
            final LogWatch lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName()).watchLog(System.out);
            logger.info("Watching Pod logs for 10 seconds...");
            TimeUnit.SECONDS.sleep(10L);
            logger.info("Deleting Pod...");
            client.resource(pod).inNamespace(namespace).delete();
            logger.info("Closing Pod log watch");
            lw.close();

            logger.info("Everything ended faithfully");

        }
    }

}
