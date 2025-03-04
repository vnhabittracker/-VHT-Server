package habit.tracker.habittracker.adapter.habit;

public class TrackingItem {
    private String trackId;
    private String habitId;
    private String name;
    private String habitDescription;
    private String trackingDescription;
    private String target;
    private int habitType;
    private String habitTypeName;
    private int monitorType;
    private String groupId;
    private String number;
    private int count;
    private int totalCount;
    private String unit;
    private String color;
    private float comp = 0f;

    public TrackingItem() {}

    public TrackingItem(String trackId, String habitId, String target, String groupId,
                        String name, String habitDescription, String trackingDescription,
                        String habitType, int monitorType,
                        String number, int count, String unit, String color, int totalCount) {
        this.trackId = trackId;
        this.habitId = habitId;
        this.target = target;
        this.groupId = groupId;
        this.name = name;
        this.habitDescription = habitDescription;
        this.trackingDescription = trackingDescription;
        this.habitType = Integer.parseInt(habitType);
        switch (this.habitType) {
            case 0:
                this.habitTypeName = "hôm nay";
                break;
            case 1:
                this.habitTypeName = "tuần này";
                break;
            case 2:
                this.habitTypeName = "tháng này";
                break;
            case 3:
                this.habitTypeName = "năm nay";
                break;
        }
        this.monitorType = monitorType;
        this.number = number;
        this.count = count;
        this.unit = unit;
        this.color = color;
        this.totalCount = totalCount;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getHabitId() {
        return habitId;
    }

    public String getName() {
        return name;
    }

    public String getHabitDescription() {
        return habitDescription;
    }

    public String getTrackingDescription() {
        return trackingDescription;
    }

    public String getHabitTypeName() {
        return habitTypeName;
    }

    public int getMonitorType() {
        return monitorType;
    }

    public String getNumber() {
        return number;
    }

    public int getCount() {
        return count;
    }

    public String getUnit() {
        return unit;
    }

    public String getColor() {
        return color;
    }

    public float getCompletion() {
        return comp;
    }

    public String getTarget() {
        return target;
    }

    public int getHabitType() {
        return habitType;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHabitDescription(String habitDescription) {
        this.habitDescription = habitDescription;
    }

    public void setTrackingDescription(String trackingDescription) {
        this.trackingDescription = trackingDescription;
    }

    public void setHabitTypeName(String habitTypeName) {
        this.habitTypeName = habitTypeName;
    }

    public void setMonitorType(int monitorType) {
        this.monitorType = monitorType;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setRatio(float comp) {
        this.comp = comp;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setHabitType(int habitType) {
        this.habitType = habitType;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
