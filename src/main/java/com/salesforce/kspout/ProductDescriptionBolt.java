package com.salesforce.kspout;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ProductDescriptionBolt implements IRichBolt{
	OutputCollector _collector;
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		_collector = collector;
	}

	public void execute(Tuple input) {
		String URL = input.getStringByField("URL");

		String productDescription = WordKafkaUtil.getDescription(URL);
		
		String[] words = productDescription.split("\\s+");
		for(String word: words){
			//TODO ignore stop words
			_collector.emit(new Values(word));
		}
		
		_collector.ack(input);
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
