package com.ode.junior.checker;

import java.util.List;

public record Alert(String user, String route, int score, List<UserConnectionDifference> details, long timestamp) {
}
