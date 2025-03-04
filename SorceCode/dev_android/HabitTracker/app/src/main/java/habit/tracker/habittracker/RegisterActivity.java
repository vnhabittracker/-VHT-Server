package habit.tracker.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import habit.tracker.habittracker.api.VnHabitApiUtils;
import habit.tracker.habittracker.api.model.user.User;
import habit.tracker.habittracker.api.model.user.UserResult;
import habit.tracker.habittracker.api.service.VnHabitApiService;
import habit.tracker.habittracker.common.AppConstant;
import habit.tracker.habittracker.common.util.AppGenerator;
import habit.tracker.habittracker.common.util.XmlAppHelper;
import habit.tracker.habittracker.common.validator.Validator;
import habit.tracker.habittracker.common.validator.ValidatorType;
import habit.tracker.habittracker.repository.Database;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.link_login)
    TextView linkLogin;
    @BindView(R.id.btn_fb_login_real)
    LoginButton btnFbLogin;
    @BindView(R.id.edit_username)
    EditText edUsername;
    @BindView(R.id.edit_email)
    EditText edEmail;
    @BindView(R.id.edit_password)
    EditText edPassword;
    @BindView(R.id.edit_conf_password)
    EditText edPasswordConfirm;

    VnHabitApiService mService = VnHabitApiUtils.getApiService();
    Database mDb = Database.getInstance(RegisterActivity.this);

    @Override
    protected void onStart() {
        mDb.open();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        String registerText = getResources().getString(R.string.remind_login);
        SpannableString content = new SpannableString(registerText);
        content.setSpan(new UnderlineSpan(), 0, registerText.length(), 0);
        linkLogin.setText(content);
    }

    @OnClick(R.id.btn_google_login)
    public void siginWithGoogle(View v) {
        super.signInWithGoogle();
    }

    @Override
    protected void afterSocialLogin(User user) {
        finish();
    }

    @OnClick({R.id.btn_register, R.id.link_login, R.id.btn_fb_login})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                String username = edUsername.getText().toString();
                String password = edPassword.getText().toString();
                String email = edEmail.getText().toString();
                String passwordConfirm = edPasswordConfirm.getText().toString();

                Validator validator = new Validator();
                validator.setErrorMsgListener(new Validator.ErrorMsg() {
                    @Override
                    public void showError(ValidatorType type, String key, String cond) {
                        switch (type) {
                            case EMPTY:
                                Toast.makeText(RegisterActivity.this, key + " không được rỗng", Toast.LENGTH_SHORT).show();
                                break;
                            case PHONE:
                                Toast.makeText(RegisterActivity.this, key + " không đúng", Toast.LENGTH_SHORT).show();
                                break;
                            case EMAIL:
                                Toast.makeText(RegisterActivity.this, key + " không hợp lệ", Toast.LENGTH_SHORT).show();
                                break;
                            case EQUAL:
                                Toast.makeText(RegisterActivity.this, key + " không trùng khớp", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                if (!validator.checkEmpty("Tên tài khoản", username)
                        || !validator.checkEmpty("Email", email)
                        || !validator.checkEmpty("Mật khẩu", password)
                        || !validator.checkEmpty("Mật khẩu", passwordConfirm)) {
                    return;
                }
                if (!validator.checkEmail(email)) {
                    return;
                }
                if (!validator.checkLength(password, 6, "Mật khẩu")) {
                    return;
                }
                if (!validator.checkEqual(password, passwordConfirm, "Mật khẩu")) {
                    return;
                }

                try {
                    Map<String, String> umap = XmlAppHelper.readFromAnXML(this, R.xml.app_default);

                    User newUser = new User();
                    newUser.setUserId(AppGenerator.getNewId());
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setPassword(password);
                    newUser.setCreatedDate(AppGenerator.getCurrentDate(AppGenerator.YMD_SHORT));
                    newUser.setLastLoginTime(AppGenerator.getCurrentDate(AppGenerator.YMD_SHORT));
                    newUser.setContinueUsingCount(umap.get(XmlAppHelper.USAGE));
                    newUser.setCurrentContinueUsingCount(umap.get(XmlAppHelper.CURRENT_USAGE_CHAIN));
                    newUser.setBestContinueUsingCount(umap.get(XmlAppHelper.BEST_USAGE_CHAIN));
                    newUser.setUserScore(umap.get(XmlAppHelper.USER_SCORE));

                    registNewUser(newUser);

                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn_fb_login:
                signInWithFacebook(btnFbLogin);
                btnFbLogin.performClick();
                break;
            case R.id.link_login:
                finish();
                break;
        }
    }

    private void registNewUser(final User user) {
        mService.registerUser(user).enqueue(new Callback<UserResult>() {
            @Override
            public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                if (response.body().getResult().equals(AppConstant.STATUS_OK)) {
                    Database.getUserDb().saveUser(user.toEntity());
                    Intent intent = getIntent();
                    intent.putExtra(LoginActivity.USERNAME, user.getUsername());
                    RegisterActivity.this.setResult(RESULT_OK, intent);
                    Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.body().getResult().equals("2")) {
                    Toast.makeText(RegisterActivity.this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                } else if (response.body().getResult().equals("3")) {
                    Toast.makeText(RegisterActivity.this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký không thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResult> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Đăng ký không thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showEmpty(View v) {
        Intent i = new Intent(this, EmptyActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        mDb.close();
        super.onStop();
    }
}
