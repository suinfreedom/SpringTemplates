package it.restclienttemplate.servicelayer.impl;

import it.restclienttemplate.servicelayer.Service1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Service1Impl implements Service1 {
    @Override
    public void servicemethod1() {
        log.info("servicemethod1");
    }
}
