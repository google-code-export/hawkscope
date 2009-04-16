package com.varaneckas.hawkscope.plugins.delicious;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import del.icio.us.Delicious;
import del.icio.us.beans.Bundle;
import del.icio.us.beans.Post;
import del.icio.us.beans.Tag;

public class DeliciousTest {

    @Test
    public void testDelicious() throws Exception {
        Delicious client = new Delicious("*", "*");
        for (Object p : client.getBundles()) {
            Bundle bun = (Bundle) p;
            System.out.println(bun.getTags());
        }
        for (Object p : client.getAllPosts()) {
            Post po = (Post) p;
            System.out.println(po.getHref());
        }
        for (Object t : client.getTags()) {
            Tag tag = (Tag) t;
            System.out.println(tag.getTag());
        }
    }
    
    @Test
    public void testDeliciousClient() throws Exception {
        DeliciousClient cli = DeliciousClient.getInstance();
        cli.login("*", "*");
        assertTrue(cli.update());
        System.out.println(cli.getPosts());
        assertFalse(cli.update());
        cli.update();
    }
    
}
