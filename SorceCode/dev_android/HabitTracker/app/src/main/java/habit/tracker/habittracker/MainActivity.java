package habit.tracker.habittracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import habit.tracker.habittracker.adapter.habit.HabitRecyclerViewAdapter;
import habit.tracker.habittracker.adapter.habit.TrackingItem;
import habit.tracker.habittracker.api.VnHabitApiUtils;
import habit.tracker.habittracker.api.model.habit.Habit;
import habit.tracker.habittracker.api.model.habit.HabitResponse;
import habit.tracker.habittracker.api.model.reminder.Reminder;
import habit.tracker.habittracker.api.model.tracking.Tracking;
import habit.tracker.habittracker.api.model.tracking.TrackingList;
import habit.tracker.habittracker.api.service.VnHabitApiService;
import habit.tracker.habittracker.common.AppConstant;
import habit.tracker.habittracker.common.util.AppGenerator;
import habit.tracker.habittracker.common.util.MySharedPreference;
import habit.tracker.habittracker.repository.Database;
import habit.tracker.habittracker.repository.habit.HabitEntity;
import habit.tracker.habittracker.repository.habit.Schedule;
import habit.tracker.habittracker.repository.habit.TrackingDateInWeek;
import habit.tracker.habittracker.repository.reminder.ReminderEntity;
import habit.tracker.habittracker.repository.tracking.TrackingEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static habit.tracker.habittracker.adapter.habit.HabitRecyclerViewAdapter.TYPE_ADD;
import static habit.tracker.habittracker.adapter.habit.HabitRecyclerViewAdapter.TYPE_CHECK;
import static habit.tracker.habittracker.adapter.habit.HabitRecyclerViewAdapter.TYPE_COUNT;

public class MainActivity extends BaseActivity implements HabitRecyclerViewAdapter.ItemClickListener {
    public static final int CREATE_NEW_HABIT = 0;
    public static final int UPDATE_HABIT = 1;
    public static final int USE_FILTER = 2;
    private static final int REPORT_DETAIL = 3;
    private static final int REPORT_CALENDAR = 4;
    private static final int SHOW_STATICS = 5;
    private static final int SHOW_PROFILE = 6;
    private static final int SETTING = 7;

    List<TrackingItem> itemList = new ArrayList<>();
    HabitRecyclerViewAdapter trackingAdapter;
    String currentDate;
    String firstCurrentDate;
    int timeLine = 0;

    @BindView(R.id.imgSetting)
    ImageView imgSetting;
    @BindView(R.id.rvMenu)
    RecyclerView recyclerView;
    @BindView(R.id.imgFilter)
    ImageView imgFilter;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.imgNext)
    ImageView imgNext;
    @BindView(R.id.imgBack)
    ImageView imgBack;
    @BindView(R.id.report)
    ImageView btnReport;
    @BindView(R.id.tabSuggestion)
    ImageView tabSuggestion;

    String userId;
    boolean isReStart = false;
    VnHabitApiService mApiService = VnHabitApiUtils.getApiService();
    Database mDatabase;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_NEW_HABIT || requestCode == UPDATE_HABIT) {
            if (resultCode == RESULT_OK) {
                backToCurrent();
            }
        } else if (requestCode == USE_FILTER) {
            if (resultCode == RESULT_OK) {
                Bundle filter = data.getExtras();
                String type = filter.getString("type");
                String target = filter.getString("target");
                String group = filter.getString("group");

                List<TrackingItem> filteredList = new ArrayList<>();
                for (int i = 0; i < itemList.size(); i++) {
                    if ((target.equals("-1") || target.equals(itemList.get(i).getTarget()))
                            && (type.equals("-1") || type.equals(String.valueOf(itemList.get(i).getHabitType())))
                            && (group.equals("-1") || group.equals(itemList.get(i).getGroupId()))
                            ) {
                        filteredList.add(itemList.get(i));
                    }
                }
                trackingAdapter.setData(filteredList);
                trackingAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == REPORT_DETAIL || requestCode == REPORT_CALENDAR) {
            backToCurrent();
        } else if (resultCode == RESULT_OK && (requestCode == SHOW_STATICS || requestCode == SHOW_PROFILE)) {
            backToCurrent();
        } else if (requestCode == SETTING && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle res = data.getExtras();
                if (res != null) {
                    boolean isLogout = res.getBoolean("logoutSocialLogin");
                    if (isLogout) {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        userId = MySharedPreference.getUserId(this);
        mDatabase = Database.getInstance(MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        trackingAdapter = new HabitRecyclerViewAdapter(MainActivity.this, itemList);
        trackingAdapter.setClickListener(MainActivity.this);
        recyclerView.setAdapter(trackingAdapter);
        initializeScreen();
    }

    @Override
    protected void onRestart() {
        if (isReStart) {
            isReStart = false;
            initializeScreen();
        }
        super.onRestart();
    }

    private void initializeScreen() {
        currentDate = AppGenerator.getCurrentDate(AppGenerator.YMD_SHORT);
        firstCurrentDate = currentDate;
        timeLine = 0;
        updateTitle(currentDate);
        itemList.clear();

        final String userId = MySharedPreference.getUserId(this);

        mApiService.getHabit(userId).enqueue(new Callback<HabitResponse>() {
            @Override
            public void onResponse(Call<HabitResponse> call, Response<HabitResponse> response) {
                mDatabase.open();

                int year, month, date, totalCount;

                List<Habit> fromServerToLocal = response.body().getHabit();

                Map<String, String> mapHabitFromServer = new HashMap<>();
                Map<String, String> mapTrackingFromServer = new HashMap<>();
                Map<String, String> mapReminderFromServer = new HashMap<>();

                // syn data
                if (fromServerToLocal != null && fromServerToLocal.size() > 0) {
                    HabitEntity habitEntity;
                    for (Habit habit : fromServerToLocal) {

                        mapHabitFromServer.put(habit.getHabitId(), habit.getHabitName());
                        habitEntity = Database.getHabitDb().getHabit(habit.getHabitId());

                        if (habitEntity != null && habitEntity.isDelete()) {
                            callDeleteHabitApi(habit.getHabitId());

                        } else if (habitEntity != null && habitEntity.isUpdate()) {
                            callUpdateHabitApi(habitEntity);

                        } else {
                            Database.getHabitDb().saveUpdateHabit(habit.toEntity());

                            // update tracking list from server
                            TrackingEntity trackingEntity;
                            for (Tracking record : habit.getTracksList()) {
                                trackingEntity = Database.getTrackingDb().getTracking(record.getHabitId());
                                if (trackingEntity != null && trackingEntity.isUpdate()) {
                                    callSaveUpdateTrackingApi(trackingEntity);
                                } else {
                                    Database.getTrackingDb().saveUpdateTracking(record.toEntity());
                                }
                                mapTrackingFromServer.put(record.getTrackingId(), record.getCurrentDate());
                            }

                            // update reminder list from server
                            for (Reminder reminder : habit.getReminderList()) {
                                Database.getReminderDb().saveReminder(reminder.toEntity(), reminder.getReminderId());
                                mapReminderFromServer.put(reminder.getReminderId(), reminder.getHabitId());
                            }
                        }

                    }
                }

                // load today habit
                List<HabitEntity> fromLocalToServer = Database.getHabitDb().getHabitByUser(userId);

                if (fromLocalToServer != null && fromLocalToServer.size() > 0) {
                    List<TrackingEntity> trackingEntityList;
                    List<ReminderEntity> reminderEntityList;
                    Calendar ca = Calendar.getInstance();
                    ca.setTimeInMillis(System.currentTimeMillis());

                    for (HabitEntity habitEntity : fromLocalToServer) {
                        if (!habitEntity.isDelete()) {
                            year = ca.get(Calendar.YEAR);
                            month = ca.get(Calendar.MONTH) + 1;
                            date = ca.get(Calendar.DATE);

                            // syn from local to server if server don't store this habit
                            if (!mapHabitFromServer.containsKey(habitEntity.getHabitId())) {
                                callAddHabitApi(habitEntity.getHabitId());
                            }

                            // syn tracking records
                            trackingEntityList = Database.getTrackingDb().getTrackingRecordsByHabit(habitEntity.getHabitId());
                            for (TrackingEntity record : trackingEntityList) {
                                if (!mapTrackingFromServer.containsKey(record.getTrackingId())) {
                                    callSaveUpdateTrackingApi(record);
                                }
                            }

                            // syn reminders
                            reminderEntityList = Database.getReminderDb().getRemindersByHabit(habitEntity.getHabitId());
                            for (ReminderEntity reminderEntity : reminderEntityList) {
                                if (!mapReminderFromServer.containsKey(reminderEntity.getServerId())) {
                                    callAddReminderApi(reminderEntity.toModel());
                                }
                            }

                            if (isTodayHabit(year, month - 1, date, habitEntity)) {
                                // create today tracking record list
                                if (currentDate.compareTo(habitEntity.getStartDate()) >= 0
                                        && (TextUtils.isEmpty(habitEntity.getEndDate()) || currentDate.compareTo(habitEntity.getEndDate()) <= 0)) {

                                    TrackingEntity record = getTodayTracking(habitEntity.getHabitId(), currentDate, 0);
                                    totalCount = getSumValueInPeriod(habitEntity.getHabitId(), Integer.parseInt(habitEntity.getHabitType()), Integer.parseInt(record.getCount()));

                                    itemList.add(new TrackingItem(
                                            record.getTrackingId(),
                                            habitEntity.getHabitId(),
                                            habitEntity.getHabitTarget(),
                                            habitEntity.getGroupId(),
                                            habitEntity.getHabitName(),
                                            habitEntity.getDescription(),
                                            record.getDescription(),
                                            habitEntity.getHabitType(),
                                            Integer.parseInt(habitEntity.getMonitorType()),
                                            habitEntity.getMonitorNumber(),
                                            record.getIntCount(),
                                            habitEntity.getMonitorUnit(),
                                            habitEntity.getHabitColor(),
                                            totalCount)
                                    );
                                }
                            }
                        }
                    }
                }
                trackingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<HabitResponse> call, Throwable t) {
                updateByCurrentDate();
            }
        });
    }

    private void callAddHabitApi(String habitId) {
        mDatabase.open();

        List<TrackingEntity> trackingList = Database.getTrackingDb().getTrackingRecordsByHabit(habitId);
        List<ReminderEntity> reminderList = Database.getReminderDb().getRemindersByHabit(habitId);

        HabitEntity habitEntity = Database.getHabitDb().getHabit(habitId);

        Habit habit = habitEntity != null ? habitEntity.toModel() : null;
        if (habit == null) {
            return;
        }

        if (trackingList != null && trackingList.size() > 0) {
            for (TrackingEntity trackingEntity : trackingList) {
                habit.getTracksList().add(trackingEntity.toModel());
            }
        }
        if (reminderList != null && reminderList.size() > 0) {
            for (ReminderEntity reminderEntity : reminderList) {
                habit.getReminderList().add(reminderEntity.toModel());
            }
        }

        mApiService.addHabit(habit).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void callDeleteHabitApi(final String habitId) {
        mApiService.deleteHabit(habitId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Database db = new Database(MainActivity.this);
                db.open();

                Database.getHabitDb().delete(habitId);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void callUpdateHabitApi(final HabitEntity habitEntity) {
        mDatabase.open();

        final Habit habit = habitEntity != null? habitEntity.toModel(): null;
        if (habit == null) {
            return;
        }

        List<TrackingEntity> trackingList = Database.getTrackingDb().getTrackingRecordsByHabit(habit.getHabitId());
        List<ReminderEntity> reminderList = Database.getReminderDb().getRemindersByHabit(habit.getHabitId());

        for (TrackingEntity trackingEntity : trackingList) {
            habit.getTracksList().add(trackingEntity.toModel());
        }

        for (ReminderEntity reminderEntity : reminderList) {
            habit.getReminderList().add(reminderEntity.toModel());
        }

        mApiService.updateHabit(habit).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Database db = Database.getInstance(MainActivity.this);
                db.open();

                Database.getHabitDb().setUpdate(habit.getHabitId(), true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void callSaveUpdateTrackingApi(final TrackingEntity entityToServer) {
        TrackingList trackingData = new TrackingList();
        trackingData.getTrackingList().add(entityToServer.toModel());
        mApiService.saveUpdateTracking(trackingData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Database db = new Database(MainActivity.this);
                db.open();
                Database.getTrackingDb().setUpdate(entityToServer.getTrackingId(), false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void callUpdateTrackRecordApi(final TrackingList data, final String trackId) {
        mApiService.saveUpdateTracking(data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mDatabase.open();

                TrackingEntity entity = Database.getTrackingDb().getTracking(trackId);
                if (entity != null) {
                    entity.setUpdate(false);
                    Database.getTrackingDb().saveUpdateTracking(entity);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void callAddReminderApi(Reminder reminder) {
        mApiService.addUpdateReminder(reminder).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private int getSumValueInPeriod(String habitId, int habitType, int count) {
        String[] arr = currentDate.split("-");
        int year = Integer.parseInt(arr[0]);
        int month = Integer.parseInt(arr[1]);
        String start = null, end = null;
        switch (habitType) {
            case 0:
                return count;
            case 1:
                String[] dates = AppGenerator.getDatesInWeek(currentDate);
                start = dates[0];
                end = dates[6];
                break;
            case 2:
                start = year + "-" + String.format(AppConstant.format2D, month) + "-01";
                end = year + "-" + String.format(AppConstant.format2D, month) + "-" + AppGenerator.getMaxDayInMonth(year, month - 1);
                break;
            case 3:
                start = year + "-01-01";
                end = year + "-12-" + AppGenerator.getMaxDayInMonth(year, month - 1);
                break;
        }
        if (start != null && end != null) {
            return Database.getTrackingDb().sumCountByHabit(habitId, start, end);
        }
        return 0;
    }

    @Override
    public void onItemValueChanged(View view, int type, int position, int totalCount, int count) {
        mDatabase.open();

        TrackingItem item = itemList.get(position);
        item.setCount(count);
        item.setTotalCount(totalCount);

        // save to mDb local
        TrackingList trackingData = new TrackingList();
        Tracking tracking = new Tracking();
        tracking.setTrackingId(item.getTrackId());
        tracking.setHabitId(item.getHabitId());
        tracking.setCount(String.valueOf(item.getCount()));
        tracking.setCurrentDate(currentDate);
        tracking.setDescription(item.getTrackingDescription());
        tracking.setUpdate(true);
        trackingData.getTrackingList().add(tracking);

        if (!Database.getTrackingDb().saveUpdateTracking(tracking.toEntity())) {
            return;
        }

        callUpdateTrackRecordApi(trackingData, item.getTrackId());
    }

    @Override
    public void onItemClick(View view, int type, int position) {
        if (TYPE_ADD == type) {
            Intent intent = new Intent(this, HabitActivity.class);
            startActivityForResult(intent, CREATE_NEW_HABIT);
        } else if (TYPE_COUNT == type) {
            Intent intent = new Intent(this, ReportDetailsActivity.class);
            intent.putExtra(AppConstant.HABIT_ID, itemList.get(position).getHabitId());
            startActivityForResult(intent, REPORT_DETAIL);
        } else if (TYPE_CHECK == type) {
            Intent intent = new Intent(this, ReportCalendarActivity.class);
            intent.putExtra(AppConstant.HABIT_ID, itemList.get(position).getHabitId());
            startActivityForResult(intent, REPORT_CALENDAR);
        }
    }

    @OnClick(R.id.tvDate)
    public void onTitleDateClick(View v) {
        Calendar ca = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String d = AppGenerator.getDate(year, month + 1, day, AppGenerator.YMD_SHORT);
                timeLine = AppGenerator.countDayBetween(currentDate, d);
                currentDate = d;
                updateTitle(currentDate);
                itemList.clear();
                updateByCurrentDate();
            }
        }, ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DATE));
//        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();

        Button posButton = dialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
        posButton.setAllCaps(false);
        posButton.setText("Chọn");
        posButton.setTextColor(getResources().getColor(R.color.colorAccent));
        Button navButton = dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
        navButton.setAllCaps(false);
        navButton.setText("Hủy");
        navButton.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void updateByCurrentDate() {
        Database db = Database.getInstance(this);
        db.open();

        itemList.clear();
        String[] arr = currentDate.split("-");
        int year = Integer.parseInt(arr[0]);
        int month = Integer.parseInt(arr[1]);
        int date = Integer.parseInt(arr[2]);
        Schedule schedule = new Schedule(year, month, date);

        List<HabitEntity> habitEntities = Database.getHabitDb().getTodayHabit(schedule, currentDate, userId);

        int totalCount = 0;
        for (HabitEntity habit : habitEntities) {

            // get tracking records on current remindDate
            TrackingEntity trackingEntity = Database.getTrackingDb().getTracking(habit.getHabitId(), currentDate);
            if (trackingEntity == null) {
                trackingEntity = getTodayTracking(habit.getHabitId(), currentDate, 0);
            }

            totalCount = getSumValueInPeriod(habit.getHabitId(), Integer.parseInt(habit.getHabitType()), Integer.parseInt(trackingEntity.getCount()));

            itemList.add(new TrackingItem(
                    trackingEntity.getTrackingId(),
                    habit.getHabitId(),
                    habit.getHabitTarget(),
                    habit.getGroupId(),
                    habit.getHabitName(),
                    habit.getDescription(),
                    trackingEntity.getDescription(),
                    habit.getHabitType(),
                    Integer.parseInt(habit.getMonitorType()),
                    habit.getMonitorNumber(),
                    Integer.parseInt(trackingEntity.getCount()),
                    habit.getMonitorUnit(),
                    habit.getHabitColor(),
                    totalCount)
            );
        }

        trackingAdapter.setEditableItemCount(currentDate.compareTo(firstCurrentDate) < 1);
        trackingAdapter.notifyDataSetChanged();

        db.close();
    }

    public TrackingEntity getTodayTracking(String habitId, String currentDate, int defaultVal) {
        TrackingEntity todayTracking = Database.getTrackingDb().getTracking(habitId, currentDate);
        if (todayTracking == null) {
            todayTracking = new TrackingEntity();
            todayTracking.setTrackingId(AppGenerator.getNewId());
            todayTracking.setHabitId(habitId);
            todayTracking.setCount(String.valueOf(defaultVal));
            todayTracking.setCurrentDate(currentDate);
            todayTracking.setDescription(null);
        }
        return todayTracking;
    }

    @OnClick(R.id.imgSetting)
    public void showSetting(View v) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTING);
    }

    @OnClick(R.id.imgNext)
    public void loadNextDateHabit(View v) {
        String nextDate = AppGenerator.getNextDate(currentDate, AppGenerator.YMD_SHORT);
        if (nextDate != null) {
            timeLine++;
            updateTitle(nextDate);
            currentDate = nextDate;
            itemList.clear();
            updateByCurrentDate();
        }
    }

    @OnClick(R.id.imgBack)
    public void loadPreDateHabit(View v) {
        String preDate = AppGenerator.getPreDate(currentDate, AppGenerator.YMD_SHORT);
        if (preDate != null) {
            timeLine--;
            updateTitle(preDate);
            currentDate = preDate;
            itemList.clear();
            updateByCurrentDate();
        }
    }

    public void backToCurrent(View v) {
        backToCurrent();
    }

    private void backToCurrent() {
        timeLine = 0;
        updateTitle(firstCurrentDate);
        currentDate = firstCurrentDate;
        itemList.clear();
        updateByCurrentDate();
    }

    @OnClick(R.id.report)
    public void ShowStatics(View v) {
        Intent intent = new Intent(this, StaticsActivity.class);
        startActivityForResult(intent, SHOW_STATICS);
    }

    @OnClick(R.id.tabSuggestion)
    public void showProfile(View view) {
        isReStart = true;
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivityForResult(intent, SHOW_PROFILE);
    }

    @OnClick(R.id.imgFilter)
    public void showFilter(View v) {
        Intent intent = new Intent(this, FilterMainActivity.class);
        startActivityForResult(intent, USE_FILTER);
    }

    public void showEmpty(View v) {
        Intent intent = new Intent(this, EmptyActivity.class);
        startActivity(intent);
    }

    public boolean isTodayHabit(int year, int month, int date, TrackingDateInWeek habit) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, month, date);
        int toDay = ca.get(Calendar.DAY_OF_WEEK);
        return toDay == Calendar.MONDAY && habit.getMon() != null && habit.getMon().equals("1")
                || toDay == Calendar.TUESDAY && habit.getTue() != null && habit.getTue().equals("1")
                || toDay == Calendar.WEDNESDAY && habit.getWed() != null && habit.getWed().equals("1")
                || toDay == Calendar.THURSDAY && habit.getThu() != null && habit.getThu().equals("1")
                || toDay == Calendar.FRIDAY && habit.getFri() != null && habit.getFri().equals("1")
                || toDay == Calendar.SATURDAY && habit.getSat() != null && habit.getSat().equals("1")
                || toDay == Calendar.SUNDAY && habit.getSun() != null && habit.getSun().equals("1");
    }

    private void updateTitle(String date) {
        if (timeLine == 0) {
            tvDate.setText("Hôm nay");
        } else if (timeLine == 1) {
            tvDate.setText("Ngày mai");
        } else if (timeLine == -1) {
            tvDate.setText("Hôm qua");
        } else {
            tvDate.setText(AppGenerator.format(date, AppGenerator.YMD_SHORT, AppGenerator.DMY_SHORT));
        }
    }

    @Override
    protected void onStop() {
        mDatabase.close();
        super.onStop();
    }
}
