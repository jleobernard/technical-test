package com.ode.junior.checker;

import java.util.List;

public record UserConnectionCheckResult(int score, List<UserConnectionDifference> differences) {

}
