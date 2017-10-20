package bravest.ptt.skynet.event;

public class SelectEvent {
    private Status mStatus;

    public SelectEvent(Status status) {
        mStatus = status;
    }

    public boolean isSelected() {
        return mStatus == Status.SELECTED;
    }

    public Status getStatus() {
        return mStatus;
    }

    public enum Status {
        SELECTED,
        UNSELECTED
    }
}
