package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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
 * - Calculate date dor loader here and not in PagerFragment
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private int mFragmentDateOffset;

    public MainScreenFragment()
    {
    }

    public void setFragmentDateOffset(int offset) {
        mFragmentDateOffset = offset;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);

        score_list.setAdapter(mAdapter);
        score_list.setEmptyView(rootView.findViewById(R.id.tvNoMatches));

        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                scoresAdapter.ViewHolder selected = (scoresAdapter.ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
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
