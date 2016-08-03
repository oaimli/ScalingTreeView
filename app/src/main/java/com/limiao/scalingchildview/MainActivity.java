package com.limiao.scalingchildview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<TreeItem> arrayItems; // 有序的存储树的所有节点
    private ViewGroup parent; // 页面布局
    private HorizontalScrollView scrollViewH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent = (ViewGroup) findViewById(R.id.parent);
        scrollViewH = (HorizontalScrollView) findViewById(R.id.scrollViewH);
        initData();
        MyView myView = new MyView(this,arrayItems);
        Log.i("treeItems",arrayItems.size()+"");
        parent.addView(myView);
        //在Android View还没有画完的时候自己对只进行测量
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        myView.measure(w, h);
        //控制显示的树位于屏幕的中间位置。必须开启另一个线程来实现移动，因为直接移动的话，界面还没有执行完毕所以移动会无效
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //获取屏幕宽度
                try {
                    DisplayMetrics metric = getResources().getDisplayMetrics();
                    int screenWidth = metric.widthPixels; // 屏幕宽度（PX）
                    scrollViewH.scrollTo((parent.getMeasuredWidth() - screenWidth) / 2, 0);
                } catch (Exception e) {

                }
            }
        }, 200);
    }

    // 初始化数据
    private void initData(){
        arrayItems = new ArrayList<>();
        // json串参看string文件中的json  树的根节点的父节点只能定义为-1，代码里面需要验证节点是否是树的根节点
        String json = "{\"statusCode\":\"200\",\"content\":{\n" +
                "        \"treeId\":\"13\",\n" +
                "        \"items\":[\n" +
                "        {\n" +
                "        \"id\":\"item_0000001\",\n" +
                "        \"parentId\":\"-1\",\n" +
                "        \"text\":\"item_0000001\",\n" +
                "        \"percentage\":\"100\",\n" +
                "        \"color\":\"#dddddd\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000002\",\n" +
                "        \"parentId\":\"item_0000001\",\n" +
                "        \"text\":\"item_0000002\",\n" +
                "        \"percentage\":\"35\",\n" +
                "        \"color\":\"#5cb0ee\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000003\",\n" +
                "        \"parentId\":\"item_0000001\",\n" +
                "        \"text\":\"item_0000003\",\n" +
                "        \"percentage\":\"40\",\n" +
                "        \"color\":\"#5cb0ee\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000004\",\n" +
                "        \"parentId\":\"item_0000001\",\n" +
                "        \"text\":\"item_0000004\",\n" +
                "        \"percentage\":\"25\",\n" +
                "        \"color\":\"#5cb0ee\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000005\",\n" +
                "        \"parentId\":\"item_0000002\",\n" +
                "        \"text\":\"item_0000005\",\n" +
                "        \"percentage\":\"50\",\n" +
                "        \"color\":\"#51cbe2\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000006\",\n" +
                "        \"parentId\":\"item_0000002\",\n" +
                "        \"text\":\"item_0000006\",\n" +
                "        \"percentage\":\"50\",\n" +
                "        \"color\":\"#51cbe2\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000007\",\n" +
                "        \"parentId\":\"item_0000003\",\n" +
                "        \"text\":\"item_0000007\",\n" +
                "        \"percentage\":\"100\",\n" +
                "        \"color\":\"#51cbe2\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000008\",\n" +
                "        \"parentId\":\"item_0000004\",\n" +
                "        \"text\":\"item_0000008\",\n" +
                "        \"percentage\":\"60\",\n" +
                "        \"color\":\"#51cbe2\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000009\",\n" +
                "        \"parentId\":\"item_0000004\",\n" +
                "        \"text\":\"item_0000009\",\n" +
                "        \"percentage\":\"40\",\n" +
                "        \"color\":\"#51cbe2\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000010\",\n" +
                "        \"parentId\":\"item_0000006\",\n" +
                "        \"text\":\"item_0000010\",\n" +
                "        \"percentage\":\"33.3\",\n" +
                "        \"color\":\"#3ae5c3\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000011\",\n" +
                "        \"parentId\":\"item_0000006\",\n" +
                "        \"text\":\"item_0000011\",\n" +
                "        \"percentage\":\"33.3\",\n" +
                "        \"color\":\"#3ae5c3\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000012\",\n" +
                "        \"parentId\":\"item_0000006\",\n" +
                "        \"text\":\"item_0000012\",\n" +
                "        \"percentage\":\"33.3\",\n" +
                "        \"color\":\"#3ae5c3\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000013\",\n" +
                "        \"parentId\":\"item_0000008\",\n" +
                "        \"text\":\"item_0000013\",\n" +
                "        \"percentage\":\"100\",\n" +
                "        \"color\":\"#99d7fe\",\n" +
                "        \"hasSource\":\"20\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"id\":\"item_0000014\",\n" +
                "        \"parentId\":\"item_0000009\",\n" +
                "        \"text\":\"item_0000014\",\n" +
                "        \"percentage\":\"100\",\n" +
                "        \"color\":\"#99d7fe\",\n" +
                "        \"hasSource\":\"10\"\n" +
                "        }\n" +
                "        ]\n" +
                "        }\n" +
                "        }";
        jsonUtil(json);
    }

    // json解析
    private void jsonUtil(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.getString("statusCode");
            if("200".equals(status)){
                String con = jsonObject.getString("content");
                JSONObject tree = new JSONObject(con);
                String treeId = tree.getString("treeId"); // 当前故障树的Id
                String items = tree.getString("items");
                JSONArray treeItems = new JSONArray(items);
                int size = treeItems.length();
                for(int i=0; i<size; i++){
                    TreeItem item = new TreeItem();
                    JSONObject o = treeItems.getJSONObject(i);
                    String id = o.getString("id");
                    String parentId = o.getString("parentId");
                    String content = o.getString("text");
                    String percentage = o.getString("percentage");
                    String color = o.getString("color");
                    String hasSource = o.getString("hasSource");
                    item.setId(id);
                    item.setParentId(parentId);
                    item.setTreeId(treeId);
                    item.setContent(content);
                    item.setPercentage(percentage);
                    item.setColor(color);
                    if(hasSource.equals("10")){// 10表示有资源,20表示无资源
                        item.setHasSource(true);
                    }else{
                        item.setHasSource(false);
                    }
                    arrayItems.add(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
