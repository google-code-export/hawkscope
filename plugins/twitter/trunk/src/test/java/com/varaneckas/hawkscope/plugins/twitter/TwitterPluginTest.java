package com.varaneckas.hawkscope.plugins.twitter;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import twitter4j.User;

import com.varaneckas.hawkscope.cfg.Configuration;
import com.varaneckas.hawkscope.cfg.ConfigurationFactory;

public class TwitterPluginTest {

	@Test
	public void testTwitter() throws Exception {
		Configuration cfg = ConfigurationFactory
			.getConfigurationFactory().getConfiguration();
		Twitter twitter = new Twitter(
				cfg.getProperties().get(TwitterPlugin.PROP_TWITTER_USER), 
				cfg.getProperties().get(TwitterPlugin.PROP_TWITTER_PASS));
		assertTrue("Twitter is ok", twitter.test());
		
		List<User> follower = twitter.getFollowers();
	}
	
}
