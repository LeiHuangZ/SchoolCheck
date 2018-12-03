package com.zsy.words;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.huang.myapplication.R;
import com.example.huang.myapplication.greendao.PersonDaoManager;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.ThreadUtils;
import com.zsy.words.adapter.MyAdapter;
import com.zsy.words.bean.Person;
import com.zsy.words.view.WordsNavigation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author huang
 * @date 2017/12/15  14:03
 * @Describe 通讯录界面，通讯录为服务器获取，存入本地数据的数据
 */
public class ConnectActivity extends AppCompatActivity implements
        WordsNavigation.onWordsChangeListener, AbsListView.OnScrollListener {

    private Handler handler;
    private List<Person> list;
    private TextView tv;
    private ListView listView;
    private WordsNavigation word;
    private SpUtils mSpUtils;
    private ExecutorService mExecutorService;

    /**
     * 主线程与子线程通讯的Handler,为避免内存泄漏，使用静态内部类
     */
    private MyHandler mHandler = new MyHandler(this);
    private static class MyHandler extends Handler{
        private ConnectActivity mActivity;

        private MyHandler(ConnectActivity activity){
            WeakReference<ConnectActivity> weakReference = new WeakReference<>(activity);
            mActivity = weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 0) {
                //初始化ListView，填充数据
                mActivity.initListView();
                super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        tv = (TextView) findViewById(R.id.tv);
        word = (WordsNavigation) findViewById(R.id.words);
        listView = (ListView) findViewById(R.id.list);
        mExecutorService = ThreadUtils.getInstance().getExecutorService();

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                //获取通讯录数据
                initData();
            }
        });

        //设置列表点击滑动监听
        handler = new Handler();
        word.setOnWordsChangeListener(this);

        mSpUtils = new SpUtils(this);
    }

    private void initListView() {
        final MyAdapter adapter = new MyAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Person> list = adapter.getList();
                Person person = list.get(position);
                mSpUtils.savePhoneNbr(MainActivity.count, person.getMPhoto());
                ConnectActivity.this.finish();
            }
        });
    }

    /**
     * 初始化联系人列表信息
     */
    private void initData() {
        this.list = PersonDaoManager.getInstance(this).queryAll();

        //对集合排序
        Collections.sort(this.list, new Comparator<Person>() {
            @Override
            public int compare(Person lhs, Person rhs) {
                //根据拼音进行排序
                return lhs.getPinyin().compareTo(rhs.getPinyin());
            }
        });

        //ListView的list中的数据全部填充完毕后，通知主线程，可以进行ListView的初始化，
        mHandler.sendEmptyMessage(0);
    }

    /**手指按下字母改变监听回调*/
    @Override
    public void wordsChange(String words) {
        updateWord(words);
        updateListView(words);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //当滑动列表的时候，更新右侧字母列表的选中状态
        if (list.size() == 0){
            return;
        }
        word.setTouchIndex(list.get(firstVisibleItem).getHeaderWord());
    }

    /**
     * @param words 首字母
     */
    private void updateListView(String words) {
        for (int i = 0; i < list.size(); i++) {
            String headerWord = list.get(i).getHeaderWord();
            //将手指按下的字母与列表中相同字母开头的项找出来
            if (words.equals(headerWord)) {
                //将列表选中哪一个
                listView.setSelection(i);
                //找到开头的一个即可
                return;
            }
        }
    }

    /**
     * 更新中央的字母提示
     *
     * @param words 首字母
     */
    private void updateWord(String words) {
        tv.setText(words);
        tv.setVisibility(View.VISIBLE);
        //清空之前的所有消息
        handler.removeCallbacksAndMessages(null);
        //1s后让tv隐藏
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setVisibility(View.GONE);
            }
        }, 500);
    }


}
