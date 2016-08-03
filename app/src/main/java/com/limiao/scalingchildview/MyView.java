package com.limiao.scalingchildview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 自定义的framelayout, 可以动态的显示深度和广度不确定的故障树, 并可放缩滑动
 * Created by seektech on 2016/3/10.
 */
public class MyView extends FrameLayout{
    private Context mActivity;
    // 绘制树相关
    private final int ACCESSED = 11;// 表示当前的节点已访问
    private final int NOT_ACCESSED = 10;// 表示当前的节点未访问
    private ArrayList<TreeItem> allItems;// 存储树的所有的节点
    private ArrayList<ArrayList<TreeItem>> hierarchicalItems;// 存储树的分层后的节点,从上到下,从左到右
    private HashMap<String,ArrayList<TreeItem>> childrenMap;// 存储每个节点的子节点集合,已排序
    private HashMap<String,View> buttonMap;// 存储所有的节点对应的button
    private ArrayList<Integer> heights;// 树的每一层的最大高度
    private ArrayList<TreeItem> children;// 确定子节点,存储当前节点的子节点
    private ArrayList<TreeItem> siblings;// 确定子节点,存储当前节点的兄弟节点
    private HashMap<String,Integer> itemSignal;// 每个节点的标志位,NOT_ACCESSED表示未访问,ACCESSED表示已访问
    private ArrayList<TreeItem> leaf;// 所有叶子节点
    private ArrayList<HashMap<TreeItem,String>> leafsList;// 分层存储所有的叶子节点的坐标
    private boolean hasMore = true;// 叶子节点是否有超过一个的未被访问的兄弟节点，解决同时多个叶子节点时算法无法回退的问题
    private HashMap<TreeItem, String> fathersMap;// 叶子节点推出的父节点，且存储坐标
    private HashMap<TreeItem, String> loc;// 存储每个所画出的节点的位置
    private HashMap<TreeItem, TreeItem> mapFather;// 存储每个节点的对应的父节点
    private int BUTTONSPACE_H = 220;// 节点之间的水平距离
    private int BUTTONSPACE_V = 120;// 节点之间的垂直距离
    private int max = 0;// 节点的最大的深度，0开始
    // 放缩相关
    float oldDist; // 触控点之间的初始距离
    private int mode; // 触控的方式,大于等于2为多点触控
    private float defaultSize = 10f;// 按钮中字体的默认值
    // 绘制线条相关
    private Canvas canvas;

    public MyView(Context context, ArrayList<TreeItem> arrayList) {
        super(context);
        mActivity = context;
        setWillNotDraw(false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        setLayoutParams(params);// 将自定义的View设置为充满父控件
        init(context,arrayList);
        computeHierarchy();
        initButton(context);
        locateLeaf();
        max = heights.size()-1;
        //存储每个节点的父节点
        findFather();
        addButton();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        System.out.println("执行了");
        addLine(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                System.out.println("ACTION_DOWN");
                mode = 1;
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("ACTION_UP");
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                System.out.println("ACTION_POINTER_DOWN");
                mode += 1;
                oldDist = spacing(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                System.out.println("ACTION_POINTER_UP");
                mode -= 1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode >= 2) {
                    float newDist = spacing(event);
                    float gapLenght = newDist - oldDist;// 变化的长度
                    if(Math.abs(gapLenght)>5f){
                        if (newDist > oldDist + 1) {
                            System.out.println("放大了"+newDist/oldDist);
                            canvas.drawColor(Color.TRANSPARENT);
                            for(int i = 0;i<allItems.size();i++){
                                TreeItem s = allItems.get(i);
                                Button v = (Button) buttonMap.get(s.getId());
                                //System.out.println(v);
                                scaling(s,v,1.05f);
                            }
                            //addLine(canvas);// 改变了实线
                        }
                        if (newDist < oldDist - 1) {
                            System.out.println("缩小了"+newDist/oldDist);
                            canvas.drawColor(Color.TRANSPARENT);
                            for(int i = 0;i<allItems.size();i++){
                                TreeItem s = allItems.get(i);
                                Button v = (Button) buttonMap.get(s.getId());
                                //System.out.println(v);
                                scaling(s,v,0.95f);
                            }
                            //addLine(canvas);// 改变了实线
                        }
                    }

                }
                break;
        }
        return true;
    }

    // 初始化变量
    private void init(Context context, ArrayList<TreeItem> arrayList){
        allItems = arrayList;
        hierarchicalItems = new ArrayList<>();
        itemSignal = new HashMap<>();
        heights = new ArrayList<>();
        childrenMap = new HashMap<>();
        buttonMap = new HashMap<>();
        children = new ArrayList<>();
        siblings = new ArrayList<>();
        leaf = new ArrayList<>();
        leafsList = new ArrayList<>();
        fathersMap = new HashMap<>();
        loc = new HashMap<>();
        mapFather = new HashMap<>();
        mode = 0;
        oldDist = 0.0f;
    }

    // 放缩
    private void scaling(TreeItem t,View view,float degree){
        float width = view.getWidth()*degree;
        float height = view.getHeight()*degree;
        //改变除了第一行两旁的两个按钮的大小
        FrameLayout.LayoutParams params1 = (LayoutParams) view.getLayoutParams();
        params1.height = Math.round(height);
        params1.width = Math.round(width);
        //改变相对位置
        params1.setMargins(Math.round(params1.leftMargin*degree), Math.round(params1.topMargin*degree), 0, 0);
        view.setLayoutParams(params1);
        float size = ((Button)view).getTextSize();
        if(degree>1){
            if(defaultSize<18){
                defaultSize = defaultSize+2;
            }
            ((Button)view).setTextSize(defaultSize);
        }else{
            if(defaultSize>10){
                defaultSize = defaultSize-2;
            }
            ((Button)view).setTextSize(defaultSize);
        }
        String location = (Math.round(params1.leftMargin)+Math.round(view.getWidth()*0.5f))+","+Math.round(params1.topMargin);
        loc.put(t, location);
    }
    // 将现有的所有的节点分层,存储每个节点的子节点,存储子节点的同时按概率排序,概率相同的再按id排序,并为每个节点建立button
    private void computeHierarchy(){
        // 找到根节点
        TreeItem root = new TreeItem();
        for(int i=0;i<allItems.size();i++){
            TreeItem r = allItems.get(i);
            if("-1".equals(r.getParentId())){
                root = r;
                break;
            }
        }
        ArrayList<TreeItem> firstLevel = new ArrayList<>();
        firstLevel.add(root);
        hierarchicalItems.add(firstLevel);// 添加第一层
        getChildren(firstLevel);// 从第一层开始递归
    }

    // 递归调用，获得每个节点的子节点
    public void getChildren(ArrayList<TreeItem> parentLevel){
        if(parentLevel.size()!=0){
            ArrayList<TreeItem> currentLevel = new ArrayList<>();// 当前层的所有已排序的节点
            for(int i=0;i<parentLevel.size();i++){
                TreeItem treeItem = parentLevel.get(i);
                ArrayList<TreeItem> children = new ArrayList<>();// 子节点
                for(int j=0;j<allItems.size();j++){// 找出子节点
                    TreeItem item = allItems.get(j);
                    if(item.getParentId().equals(treeItem.getId())){
                        children.add(item);
                    }
                }
                //为子节点排序
                Collections.sort(children);
                childrenMap.put(treeItem.getId(),children);
                // 添加到当前层
                currentLevel.addAll(children);
            }
            if(currentLevel.size()!=0){
                hierarchicalItems.add(currentLevel);
                getChildren(currentLevel);
            }
        }
    }

    // 初始化各个button,并且测量每层节点的最大高度
    private void initButton(Context context){
        for(int i=0;i<hierarchicalItems.size();i++){
            ArrayList<TreeItem> level = hierarchicalItems.get(i);
            int heightEast = 0;
            for(int j=0; j<level.size(); j++){
                final TreeItem item = level.get(j);
                Button btn = new Button(context);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                btn.setLayoutParams(params);
                btn.setBackgroundResource(R.drawable.buttonshape);
                btn.setBackgroundColor(Color.parseColor(item.getColor()));
                btn.setText(item.getContent());
                btn.setTextColor(Color.WHITE);
                btn.setTextSize(14);
                btn.setWidth(180);
                btn.setMinHeight(144);
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mActivity,"您点击了"+item.getContent(),Toast.LENGTH_SHORT).show();
                    }
                });
                buttonMap.put(item.getId(),btn);
                int height = measureHeight(btn);
                if(height>heightEast){
                    heightEast = height;
                }
                // 给每一个节点只一个标志位
                itemSignal.put(item.getId(),NOT_ACCESSED);
            }
            heights.add(heightEast);
        }
    }

    private void findFather(){
        for(TreeItem item :allItems){
            for(TreeItem i :allItems){
                if(item.getParentId().equals(i.getId())){
                    mapFather.put(item,i);
                    break;
                }
                if(item.getParentId().equals("-1")){
                    mapFather.put(item,null);
                }
            }
        }
    }

    // 先找到叶子节点,确定叶子节点的位置,各自的父节点在画的时候递归查找出来并确定位置
    private void locateLeaf(){
        TreeItem firstItem = hierarchicalItems.get(0).get(0);
        getLeafs(firstItem);
        for(int i=0; i<leaf.size();i++){
            System.out.println(leaf.get(i).getId());
        }
        // 确定叶子节点的位置
        DisplayMetrics metric = getResources().getDisplayMetrics();// 获取屏幕宽度
        int screenWidth = metric.widthPixels;  // 屏幕宽度（PX）
        int treeWidth = BUTTONSPACE_H * leaf.size()+dip2px(mActivity, 120);// 树宽度（PX）
        int treeWidth2 = dip2px(mActivity, 120) * 3;// 树宽度（PX）
        int startH = (screenWidth - treeWidth)/2;// 树距离屏幕左边的距离
        startH = startH < 0 ? 0 :startH;
        if(treeWidth2 > treeWidth && startH == 0) {
            startH = 200;
        }
        //遍历所有的节点，判断是否是叶子节点，如果是叶子节点就计算坐标
        for(ArrayList<TreeItem> treeLevel : hierarchicalItems){
            HashMap<TreeItem,String> level = new HashMap<>();
            for(TreeItem treeItem :treeLevel){
                if(leaf.contains(treeItem)){
                    int numberH = leaf.indexOf(treeItem);
                    int numberV = hierarchicalItems.indexOf(treeLevel);
                    int locH = startH + BUTTONSPACE_H * numberH;
                    int locV = 0;
                    for(int i=0; i<numberV;i++){
                        locV += heights.get(i);
                    }
                    locV = locV + BUTTONSPACE_V*numberV;
                    level.put(treeItem,locH+","+locV);
                    System.out.println(level.size());
                }
            }
            leafsList.add(level);

        }
    }

    //找到叶子节点
    public void getLeafs(TreeItem start) {
        System.out.println("------------------------------");
        String currentItem = start.getId();
        // 找到当前节点的孩子节点
        System.out.println(currentItem);
        children = childrenMap.get(currentItem);
        System.out.println("孩子点数" + children.size() + children.toString());
        // 找到当前节点的兄弟节点
        String father = start.getParentId();
        if (siblings.size() == 0 && hasMore==true) {
            System.out.println("兄弟节点重新计算"+childrenMap);
            for(int i=0;i<allItems.size();i++){
                TreeItem treeItem = allItems.get(i);
                if(treeItem.getParentId().equals(father)){
                    siblings.add(treeItem);
                }
            }
            siblings.remove(start);
        }
        System.out.println("兄弟点数" + siblings.size() + siblings.toString());
        if (children.size() != 0 && itemSignal.get(currentItem).equals(NOT_ACCESSED)) {
            System.out.println("入口一");
            System.out.println("有子节点且未被访问");
            itemSignal.put(currentItem, ACCESSED);// 11表示被访问
            System.out.println("子节点" + children.toString());
            System.out.println("子节点peek" + children.get(0).getId());
            TreeItem top = children.get(0);
            siblings.clear();
            System.out.println("清除兄弟" + siblings.size());
            for (TreeItem s : children) {
                siblings.add(s);
            }
            siblings.remove(top);
            System.out.println("新兄弟" + siblings.toString());
            children.clear();
            System.out.println("处理完后孩子清空" + children.size());
            getLeafs(top);
        } else {
            System.out.println("入口二");
            System.out.println("无子节点或已被访问");
            children.clear();
            System.out.println("移出后兄弟点数" + siblings.size() + siblings.toString());
            if (siblings.size() !=0) {
                System.out.println("入口二一");
                if (itemSignal.get(currentItem).equals(NOT_ACCESSED)) {
                    leaf.add(start);
                    System.out.println("1添加了" + currentItem);
                    System.out.println("叶子节点从左到右" + leaf.toString());
                }
                TreeItem siblingFirst = new TreeItem();
                itemSignal.put(currentItem, ACCESSED);// 11表示被访问
                for (TreeItem s : siblings) {
                    if (itemSignal.get(s.getId()).equals(NOT_ACCESSED)) {
                        siblingFirst = s;
                        break;
                    }
                }
                System.out.println("兄弟节点" + siblings.toString());


                for (TreeItem s : siblings) {
                    if (!(itemSignal.get(s.getId()).equals(NOT_ACCESSED) && s!=siblingFirst)) {
                        hasMore = false;
                        break;
                    }
                }
                if(!hasMore){
                    siblings.clear();
                }
                getLeafs(siblingFirst);
            } else {
                System.out.println("入口二二");
                System.out.println("current"+currentItem);
                if (itemSignal.get(currentItem).equals(NOT_ACCESSED)) {
                    leaf.add(start);
                    System.out.println("2添加了" + currentItem);
                    System.out.println("叶子节点从左到右" + leaf.toString());
                }
                itemSignal.put(currentItem, ACCESSED);// 11表示被访问
                String j = start.getParentId();
                TreeItem p = new TreeItem();
                for(int i=0;i<allItems.size();i++){
                    if(allItems.get(i).getId().equals(j)){
                        p = allItems.get(i);
                    }
                }
                siblings.clear();
                for (TreeItem s : allItems) {
                    if (s.getParentId().equals(p.getId())
                            && itemSignal.get(s.getId()).equals(NOT_ACCESSED)) {
                        siblings.add(s);
                    }
                }
                System.out.println("到这了" + siblings.size()
                        + siblings.toString());
                if (siblings.contains(start))
                    siblings.remove(start);
                if (siblings.size() != 0) {
                    itemSignal.put(currentItem, ACCESSED);// 11表示被访问
                    getLeafs(p);
                } else {
                    // 回退两层
                    if (!p.getId().equals("-1")
                            && itemSignal.values().contains(NOT_ACCESSED)) {
                        System.out.println("回退");
                        getLeafs(p);
                    }

                }

            }
        }
    }

    // 添加按钮，同时回推出父节点
    public void addButton() {
        // 画按钮,回溯
        HashMap<TreeItem, String> hi = leafsList.get(max);
        hi.putAll(fathersMap);
        fathersMap.clear();
        // 画出当前层
        for (TreeItem s : hi.keySet()) {
            String g = hi.get(s);
            String locaH = g.split(",")[0];
            String locaV = g.split(",")[1];
            System.out.println("locaH:" + locaH);
            System.out.println("locaV:" + locaV);
            Button btn = (Button) buttonMap.get(s.getId());
            LayoutParams params = (LayoutParams) btn
                    .getLayoutParams();
            params.setMargins(Integer.parseInt(locaH), Integer.parseInt(locaV),
                    0, 0);
            // btn.setLayoutParams(params);
            this.addView(btn);
            // 在Android View还没有画完的时候自己对只进行测量
            int w = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
            int h = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
            btn.measure(w, h);
            int width = btn.getMeasuredWidth();
            int h1 = Integer.parseInt(locaH) + width / 2;
            loc.put(s, h1 + "," + locaV);// 存储每个已画按钮的上沿的中点
        }
        if (max > 0) {
            // 找到父节点集合
            for (TreeItem s : hi.keySet()) {
                TreeItem fatherNum = mapFather.get(s);
                int higher = 0;
                if (!fathersMap.keySet().contains(fatherNum)) {
                    for (int i = 0; i < max-1; i++) {
                        higher += heights.get(i);
                    }
                    higher = higher + BUTTONSPACE_V*(max-1);
                    fathersMap.put(fatherNum, leafsList.get(max).get(s)
                            .split(",")[0]
                            + "," + higher);
                }

            }
            System.out.println("父亲集合" + fathersMap);
            System.out.println("当前层" + leafsList.get(max).size());
            // 通过子节点数调整父节点坐标
            for (TreeItem s : fathersMap.keySet()) {
                int fMax = Integer.MIN_VALUE;
                int fMin = Integer.MAX_VALUE;
                for (TreeItem r : allItems) {
                    if (mapFather.get(r) == s) {
                        int heng = Integer.parseInt(leafsList.get(max).get(r)
                                .split(",")[0]);
                        if (heng < fMin) {
                            fMin = heng;
                        }
                        if (heng > fMax) {
                            fMax = heng;
                        }
                    }
                }
                fathersMap.put(s, ((fMax + fMin) / 2) + ","
                        + fathersMap.get(s).split(",")[1]);
                System.out.println("计算出的父节点:" + ((fMax + fMin) / 2) + ","
                        + fathersMap.get(s).split(",")[1]);
            }
        }

        max--;
        if (max > -1)
            addButton();
    }

    // 画出节点之间的连线
    public void addLine(Canvas canvas){
        System.out.println("绘了");
        Paint paint = new Paint();
        // 去锯齿
        paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        for (TreeItem s : allItems) {
            TreeItem f = mapFather.get(s);
            if (loc.get(f) != null) {
                String[] childLoc = loc.get(s).split(",");
                String[] fatherLoc = loc.get(f).split(",");
                int childH = Integer.parseInt(childLoc[0]);
                int childV = Integer.parseInt(childLoc[1]);
                int fatherH = Integer.parseInt(fatherLoc[0]);
                int fatherV = Integer.parseInt(fatherLoc[1]);
                // 定义一个Path对象，连线。
                Path path1 = new Path();
                path1.moveTo(childH, childV);
                path1.lineTo(childH, childV
                        - (childV - fatherV - buttonMap.get(f.getId()).getHeight()) / 2);
                path1.lineTo(fatherH, fatherV + buttonMap.get(f.getId()).getHeight()
                        + (childV - fatherV - buttonMap.get(f.getId()).getHeight()) / 2);
                path1.lineTo(fatherH, fatherV + buttonMap.get(f.getId()).getHeight());
                // 根据Path进行绘制，绘制三角形
                canvas.drawPath(path1, paint);
            }
        }
    }

    // 测量触控的两点间的距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private int measureHeight(View view){
        // 在Android View还没有画完的时候自己对只进行测量
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return  view.getMeasuredHeight();
    }

    // 单位转换
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
