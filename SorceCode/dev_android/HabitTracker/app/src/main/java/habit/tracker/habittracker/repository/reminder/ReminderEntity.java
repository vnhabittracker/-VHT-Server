package habit.tracker.habittracker.repository.reminder;

import habit.tracker.habittracker.api.model.reminder.Reminder;

public class ReminderEntity {
    private String reminderId;
    private String serverId;
    private String habitId;
    private String userId;
    private String remindText;
    private String reminderStartTime;
    private String reminderEndTime;
    private String repeatType;
    private boolean isDelete = false;
    private boolean isUpdate = false;

    public String getReminderId() {
        return reminderId;
    }

    public String getHabitId() {
        return habitId;
    }

    public String getRemindText() {
        return remindText;
    }

    public String getReminderStartTime() {
        return reminderStartTime;
    }

    public String getReminderEndTime() {
        return reminderEndTime;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public String getServerId() {
        return serverId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public void setRemindText(String remindText) {
        this.remindText = remindText;
    }

    public void setReminderStartTime(String reminderStartTime) {
        this.reminderStartTime = reminderStartTime;
    }

    public void setReminderEndTime(String reminderEndTime) {
        this.reminderEndTime = reminderEndTime;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public Reminder toModel() {
        Reminder reminder = new Reminder();
        reminder.setReminderId(reminderId);
        reminder.setHabitId(habitId);
        reminder.setServerId(serverId);
        reminder.setRemindText(remindText);
        reminder.setRemindStartTime(reminderStartTime);
        reminder.setRemindEndTime(reminderEndTime);
        reminder.setRepeatType(repeatType);
        reminder.setDelete(isDelete);
        reminder.setUpdate(isUpdate);
        return reminder;
    }
}
