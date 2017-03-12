package com.salesforce.kspout;

import java.util.UUID;

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
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

public class WordKafkaUtil {
	public static void createKafkaSpout(){
		  Config config = new Config();
	      config.setDebug(true);
	      config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
	      String zkConnString = "localhost:2181";
	      String topic = "my-first-topic";
	      BrokerHosts hosts = new ZkHosts(zkConnString);
	      
	      SpoutConfig kafkaSpoutConfig = new SpoutConfig (hosts, topic, "/" + topic,    
	    	         UUID.randomUUID().toString());
	      kafkaSpoutConfig.bufferSizeBytes = 1024 * 1024 * 4;
	      kafkaSpoutConfig.fetchSizeBytes = 1024 * 1024 * 4;
	      kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

	      TopologyBuilder builder = new TopologyBuilder();
	      builder.setSpout("kafka-spout", new KafkaSpout(kafkaSpoutConfig));
	      builder.setBolt("url-parser", new ProductDescriptionBolt()).shuffleGrouping("kafka-spout");
	      builder.setBolt("intersting-word", new CountBolt()).shuffleGrouping("url-parser");

	      LocalCluster cluster = new LocalCluster();
	      cluster.submitTopology("KafkaStormSample", config, builder.createTopology());

	      Thread.sleep(10000);

	      cluster.shutdown();
	}
}
