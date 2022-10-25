package com.ode.junior.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Configuration(ServerConfiguration server,
                            List<ComparatorWeight> weights,
                            int threshold,
                            String alertFile,
                            List<PropFileUserCredential> userCredentials) {

}
