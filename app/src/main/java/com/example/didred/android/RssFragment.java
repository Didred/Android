package com.example.didred.android;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.didred.android.rss.FeedItem;
import com.example.didred.android.rss.FeedsAdapter;
import com.example.didred.android.rss.RssReader;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;


public class RssFragment extends Fragment implements RssReader.OnFeedItemLoadedListener,
        RssReader.OnItemsLoadedListener, OnProgressListener {

    private RecyclerView recyclerView;
    private FeedsAdapter feedsAdapter;
    private ProgressDialog progressDialog;
    private FeedsAdapter.OnItemClickListener onItemClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rss, container, false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), newConfig.orientation));
    }

    @Override
    public void onFeedItemLoadFailed(Exception e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.rss_feed_loading_error_message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemsLoadFailed(Exception exception) {
        if (exception instanceof MalformedURLException) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    askToInputNewUrl(getString(R.string.rss_feed_incorrect_url));
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getString(R.string.rss_feed_loading_error_message),
                            Toast.LENGTH_LONG).show();
                    loadRssFeedFromCache();
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rssRecycleView);
        int orientation = getContext().getResources().getConfiguration().orientation;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), orientation));

        onItemClickListener = new FeedsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeedItem item) {
                if (isOnline()) {
                    Intent intent = new Intent(getContext(), RssWebViewActivity.class);
                    intent.putExtra(String.valueOf(R.string.url), item.getLink());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.rss_feed_connection_error, Toast.LENGTH_LONG).show();
                }
            }
        };

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String lastUserId = sharedPref.getString(getString(R.string.rss_feed_preferences_last_userId), null);
        String rssUrl = sharedPref.getString(getString(R.string.rss_feed_preferences_rssUrl), null);
        if (lastUserId == null) {
        } else if (!lastUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            CacheRepository.getInstance().removeCacheForUser(getContext(), lastUserId);
        } else {
            doRss(rssUrl);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rssFragmentMenuChangeSource:
                showRssSourceInputDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRssSourceInputDialog() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.rss_source_input_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(dialogView);

        final EditText sourceInput = dialogView.findViewById(R.id.rssSourceInputDialogField);

        builder.setCancelable(false).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        String url = sourceInput.getText().toString();
                        setPreference(null, url);
                        doRss(url);
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void doRss(String address) {
        feedsAdapter = new FeedsAdapter(getContext(), new ArrayList<FeedItem>(), onItemClickListener);
        recyclerView.setAdapter(feedsAdapter);

        boolean isConnected = isOnline();

        if (isConnected) {
            loadRssFromTheInternet(address);
        } else {
            loadRssFeedFromCache();
        }
    }

    private void loadRssFromTheInternet(String address){
        RssReader rssReader = new RssReader(address);
        rssReader.addOnFeedItemLoadedListener(this);
        rssReader.addOnExecutedListener(this);
        rssReader.addOnProgressListener(this);
        rssReader.execute();
    }

    private void loadRssFeedFromCache() {
        ArrayList<FeedItem> items = CacheRepository.getInstance().readRssCache(getContext(),
                FirebaseAuth.getInstance().getCurrentUser().getUid());
        feedsAdapter.setFeedItems(items);
        Toast.makeText(getContext(), R.string.rss_feed_success_load_from_cache, Toast.LENGTH_SHORT).show();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onFeedItemLoaded(final FeedItem item) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                feedsAdapter.addItem(item);
            }
        });
    }

    private void setPreference(String uid, String url) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (uid != null) {
            editor.putString(getString(R.string.rss_feed_preferences_last_userId), uid);
        } else {
            editor.putString(getString(R.string.rss_feed_preferences_rssUrl), url);
        }
        editor.commit();
    }

    @Override
    public void onItemsLoaded() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.rss_feed_success_load_from_internet, Toast.LENGTH_LONG).show();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                setPreference(uid, null);
                CacheRepository.getInstance().writeRssToCache(getContext(), feedsAdapter.getFeedItems(), uid);
            }
        });
    }

    @Override
    public void onProgressStarted() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.rss_feed_loading));
        progressDialog.setCancelable(false);
    }

    @Override
    public void onProgressEnded() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    private void askToInputNewUrl(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setMessage(R.string.rss_feed_correct_url_request)
                .setTitle(title)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                showRssSourceInputDialog();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.rss_fragment_menu, menu);
    }
}
