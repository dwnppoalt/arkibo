package org.arkibo;

import org.arkibo.services.StorageService;

import com.backblaze.b2.client.exceptions.B2Exception;

public class Test {
    public static void main(String[] args) {
        StorageService ss = new StorageService();

        try {
            System.out.println(ss.getAccessLink("test"));
        } catch (B2Exception e) {
            e.printStackTrace();
        }
    }
}
