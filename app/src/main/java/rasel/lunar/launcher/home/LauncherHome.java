package rasel.lunar.launcher.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import rasel.lunar.launcher.MainActivity;
import rasel.lunar.launcher.databinding.LauncherHomeBinding;
import rasel.lunar.launcher.helpers.Constants;
import rasel.lunar.launcher.todos.DatabaseHandler;
import rasel.lunar.launcher.todos.TodoAdapter;
import rasel.lunar.launcher.todos.TodoManager;

public class LauncherHome extends Fragment {

    private LauncherHomeBinding binding;
    private Context context;
    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;
    private final Constants constants = new Constants();
    private final HomeUtils homeUtils = new HomeUtils();
    private BatteryReceiver batteryReceiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LauncherHomeBinding.inflate(inflater, container, false);
        context = requireActivity().getApplicationContext();
        fragmentManager = requireActivity().getSupportFragmentManager();
        sharedPreferences = context.getSharedPreferences(constants.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE);
        batteryReceiver = new BatteryReceiver(binding.batteryProgress);

        // Recreates the fragment on getting back
        ((MainActivity) requireActivity()).setFragmentRefreshListener(() -> this.requireActivity().recreate());

        context.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); // Battery
        binding.time.setFormat12Hour(homeUtils.getTimeFormat(sharedPreferences, context)); // Time
        binding.date.setFormat12Hour(homeUtils.getDateFormat(sharedPreferences)); // Date

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int lockMethodValue = sharedPreferences.getInt(constants.SHARED_PREF_LOCK, 0);
        // handle gesture events
        homeUtils.rootViewGestures(binding.getRoot(), context, fragmentManager, requireActivity(), lockMethodValue);
        homeUtils.batteryProgressGestures(binding.batteryProgress, context, requireActivity(), lockMethodValue);
        homeUtils.todosGestures(binding.todos, context, fragmentManager, requireActivity(), lockMethodValue);
    }

    private void showTodoList() {
        binding.todos.setLayoutManager(new LinearLayoutManager(context));
        binding.todos.setAdapter(new TodoAdapter((new TodoManager()), (new DatabaseHandler(context)).getTodos(), context, requireActivity().getSupportFragmentManager(), this));
    }

    @Override
    public void onResume() {
        context.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        showTodoList();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        context.unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }
}
