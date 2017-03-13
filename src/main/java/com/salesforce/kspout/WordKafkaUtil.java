package com.salesforce.kspout;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import kafka.admin.AdminUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.Broker;
import org.apache.storm.kafka.StaticHosts;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.KafkaConfig;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordKafkaUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(WordKafkaUtil.class);

    public static final String zkConnString = "localhost:2181";
    public static final String topic = "word-topic";
    public static final String redisHost = "localhost";
    public static final int redisPort = 6379;

	public static String getDescription(String url) {
		LOGGER.info("Web crawler request for : {}", url);

		try {
			Document doc = Jsoup.connect(url).get();
			String description = doc.getElementById("productDescription").text();
			LOGGER.info(description);
			return description;
		} catch (IOException e) {
			LOGGER.error("Exception in getting url {}", e.getMessage());
		}
		return null;
	}
	
	public static String createTopic(){
		ZkClient zkclient = new ZkClient(zkConnString, 10000, 8000, ZKStringSerializer$.MODULE$);
		ZkUtils zkUtils = new ZkUtils(zkclient, new ZkConnection(zkConnString), false);
		AdminUtils.createTopic(zkUtils, topic, 1, 1, new Properties());
		return topic;
	}
	
	public static Producer createProducer(){

		Properties props = new Properties();
		 
		props.put("metadata.broker.list", "broker1:9092,broker2:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("partitioner.class", "example.producer.SimplePartitioner");
		props.put("request.required.acks", "1");
		 
		ProducerConfig config = new ProducerConfig(props);
		
		Producer<String, String> producer = new Producer<String, String>(config);
		return producer;
	}
	
	public static void createKafkaSpout() throws InterruptedException {
		  Config config = new Config();
	      config.setDebug(true);
	      config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
	      BrokerHosts hosts = new ZkHosts(zkConnString);
	      
	      SpoutConfig kafkaSpoutConfig = new SpoutConfig (hosts, topic, "/" + topic,    
	    	         UUID.randomUUID().toString());
	      kafkaSpoutConfig.bufferSizeBytes = 1024 * 1024 * 4;
	      kafkaSpoutConfig.fetchSizeBytes = 1024 * 1024 * 4;
	      kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

	      TopologyBuilder builder = new TopologyBuilder();
	      builder.setSpout("kafka-spout", new KafkaSpout(kafkaSpoutConfig));
	      builder.setBolt("url-parser", new ProductDescriptionBolt()).shuffleGrouping("kafka-spout");
	      
	      JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
          .setHost(redisHost).setPort(redisPort).build();
	      
	      //builder.setBolt("intersting-word", new CountBolt()).shuffleGrouping("url-parser");

	      LocalCluster cluster = new LocalCluster();
	      cluster.submitTopology("KafkaStormSample", config, builder.createTopology());

	      Thread.sleep(10000);

	      cluster.shutdown();
	}
}
