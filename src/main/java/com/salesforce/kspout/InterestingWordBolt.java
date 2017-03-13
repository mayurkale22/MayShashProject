package com.salesforce.kspout;

import java.util.Map;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import redis.clients.jedis.JedisCommands;

public class InterestingWordBolt extends AbstractRedisBolt{

	public InterestingWordBolt(JedisClusterConfig config) {
		super(config);
	}

	public void execute(Tuple input) {
		String key = input.getStringByField("word");
		JedisCommands jedis = null;
		try{
			jedis = getInstance();
			String number = jedis.get(key);
			if(number != null){
				jedis.set(key, Integer.toString(Integer.parseInt(number)+1));
			}
			else{
				jedis.set(key, Integer.toString(1));
			}
		}
		catch (Exception e) {
            this.collector.reportError(e);
            this.collector.fail(input);
        } finally {
            returnInstance(jedis);
        }
		
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("wordName", "count"));
	}


}
