package com.ode.junior.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ode.junior.configuration.ConnectionInformationType;

import java.util.Map;

@JsonIgnoreProperties
public record UserConnection(String user, Map<ConnectionInformationType, String> details) {
}
