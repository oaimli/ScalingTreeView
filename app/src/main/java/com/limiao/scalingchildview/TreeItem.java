package com.limiao.scalingchildview;

/**
 * 故障树的每一个节点
 * Created by limiao on 2016/1/5.
 */
public class TreeItem implements Comparable{
    private String id; // 节点的Id item_123456 不可重复
    private String parentId; // 父节点的Id
    private String treeId; // 所属故障树的Id
    private String content; // 每个节点上要显示的文本
    private String percentage; // 每个节点的概率,这也相当于文本,只是增大了button显示文本的格式的要求
    private boolean hasSource; // 是否有资源,有资源的话会在右上角有个红色的图标
    private String color; // 节点的颜色

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasSource() {
        return hasSource;
    }

    public void setHasSource(boolean hasSource) {
        this.hasSource = hasSource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getTreeId() {
        return treeId;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    @Override
    public int compareTo(Object another) {
        try{
            if(Double.parseDouble(this.getPercentage())<Double.parseDouble(((TreeItem) another).getPercentage())){
                return 1;
            }else{
                return -1;
            }
        } catch(NumberFormatException e) {
            return -1;
        }
    }
}
