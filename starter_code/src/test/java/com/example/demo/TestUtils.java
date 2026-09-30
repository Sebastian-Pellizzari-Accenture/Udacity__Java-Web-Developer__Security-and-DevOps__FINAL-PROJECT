package com.example.demo;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    public static void injectObjects(Object target, String fieldName, Object toInject) {

        boolean wasPrivate=false;

        try {
            Field f = target.getClass().getDeclaredField(fieldName);

            if(!f.canAccess(target)) {
                f.setAccessible(true);
                wasPrivate=true;
            }

            f.set(target,toInject);

            if(wasPrivate){
                f.setAccessible(false);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            log.error("Fatal Error: {} error was thrown with message={}", e.getClass().getName() , e.getMessage());
            e.printStackTrace();
        }
    }    
}
