package com.ode.junior.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record UserConnectionForRoute(UserConnection userConnection, String route) {
}
