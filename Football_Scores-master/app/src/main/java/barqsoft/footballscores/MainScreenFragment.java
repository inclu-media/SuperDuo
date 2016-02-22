package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.service.myFetchService;

/**
 * Updated by Martin Melcher 02/08/2016:
 * - Check for internet connection before starting the fetch service
 * - Added text view for empty lists
 * - Calculate date for loader here and not in PagerFragment
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener
{
    private final static String LOG_TAG = MainScreenFragment.class.getSimpleName();
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private int mFragmentDateOffset;
    private ListView mScorelist;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MainScreenFragment()
    {
    }

    public void setFragmentDateOffset(int offset) {
        mFragmentDateOffset = offset;
    }

    private void update_scores()
    {
        // check for internet connection and don't start the fetch service if there is none
        // ideally there should be a sync adapter for that
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            Toast.makeText(getActivity(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
            return;
        }

        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        update_scores();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mScorelist = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);

        mScorelist.setAdapter(mAdapter);
        mScorelist.setEmptyView(rootView.findViewById(R.id.tvNoMatches));

        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        mScorelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scoresAdapter.ViewHolder selected = (scoresAdapter.ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });

        // init swipe view
        setHasOptionsMenu(true);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onRefresh() {
        update_scores();
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(),getString(R.string.data_refreshed),Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mSwipeRefreshLayout.setRefreshing(true);
                update_scores();
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), getString(R.string.data_refreshed), Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {

        // if the activity was started from the widget
        // we might need to scroll to the desired match
        if (MainActivity.scroll_pos > 0) {
            mScorelist.setSelection(MainActivity.scroll_pos);
            MainActivity.scroll_pos = 0;
        }

        super.onResume();
    }

    private String[] getFragmentDate() {

        Date fragmentdate = new Date(System.currentTimeMillis()+((mFragmentDateOffset-2)*86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

        String[] sDate = new String[1];
        sDate[0] = mformat.format(fragmentdate);

        return sDate;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,getFragmentDate(),null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
