package seiya.actions;

public final class ActionSuppression {
    private final Action sourceAction;
    private final String resultLog;

    public ActionSuppression(Action sourceAction, String resultLog) {
        this.sourceAction = sourceAction;
        this.resultLog = resultLog;
    }

    public Action sourceAction() {
        return sourceAction;
    }

    public String resultLog() {
        return resultLog;
    }
}
