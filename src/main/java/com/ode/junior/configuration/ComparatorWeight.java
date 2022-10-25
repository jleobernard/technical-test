package com.ode.junior.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComparatorWeight(ConnectionInformationType type, int weight) {

}
