package habit.tracker.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import habit.tracker.habittracker.api.VnHabitApiUtils;
import habit.tracker.habittracker.api.model.user.User;
import habit.tracker.habittracker.api.model.user.UserResponse;
import habit.tracker.habittracker.api.service.VnHabitApiService;
import habit.tracker.habittracker.common.AppConstant;
import habit.tracker.habittracker.common.util.AppGenerator;
import habit.tracker.habittracker.common.util.MySharedPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;

    // google
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;

    // facebook
    private CallbackManager mCallbackManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException ignored) {
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.setApplicationId(getString(R.string.app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // facebook
        mCallbackManager = CallbackManager.Factory.create();
    }

    protected void signOutSocialLogin() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    protected void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    protected void signInWithFacebook(LoginButton loginButton) {
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            createOrLoginUser(user);
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            createOrLoginUser(user);
                        }
                    }
                });
    }

    protected void afterSocialLogin(User user) {
    }

    private void createOrLoginUser(FirebaseUser user) {
        if (user != null) {
            final User newUser = new User();
            newUser.setUserId(user.getUid());

            if (!TextUtils.isEmpty(user.getEmail())) {
                newUser.setUsername(user.getEmail());
            } else {
                newUser.setUsername(AppGenerator.getSearchKey(user.getDisplayName().replace(" ", "")) + user.getUid().substring(0, 3));
            }

            newUser.setEmail(user.getEmail());
            newUser.setPassword(user.getUid());
            newUser.setCreatedDate(AppGenerator.getCurrentDate(AppGenerator.YMD_SHORT));
            newUser.setLastLoginTime(AppGenerator.getCurrentDate(AppGenerator.YMD_SHORT));
            newUser.setContinueUsingCount("1");
            newUser.setCurrentContinueUsingCount("1");
            newUser.setBestContinueUsingCount("1");
            newUser.setUserScore("2");

            VnHabitApiService mService = VnHabitApiUtils.getApiService();
            mService.registerSocialLogin(newUser).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.body().getResult().equals(AppConstant.STATUS_OK)) {
                        MySharedPreference.saveUser(BaseActivity.this, newUser.getUserId(), newUser.getUsername(), newUser.getPassword());
                        afterSocialLogin(newUser);
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                }
            });
        }
    }

    public void editHabitDetails(String habitId) {
        Intent intent = new Intent(this, HabitActivity.class);
        intent.putExtra(AppConstant.HABIT_ID, habitId);
        startActivityForResult(intent, HabitActivity.REQUEST_UPDATE);
    }

    public void showStatics(View view) {
        Intent intent = new Intent(this, StaticsActivity.class);
        startActivity(intent);
        finish();
    }

    public void showProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void showNoteScreen(String habitId) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(AppConstant.HABIT_ID, habitId);
        startActivity(intent);
        finish();
    }

    public void showDetailsChart(String habitId) {
        Intent intent = new Intent(this, ReportDetailsActivity.class);
        intent.putExtra(AppConstant.HABIT_ID, habitId);
        startActivity(intent);
        finish();
    }

    public void showOnCalendar(String habitId) {
        Intent intent = new Intent(this, ReportCalendarActivity.class);
        intent.putExtra(AppConstant.HABIT_ID, habitId);
        startActivity(intent);
        finish();
    }

    public void finishThis(View view) {
        setResult(RESULT_OK);
        finish();
    }

    public void showEmptyScreen() {
        Intent intent = new Intent(this, EmptyActivity.class);
        startActivity(intent);
    }
}
