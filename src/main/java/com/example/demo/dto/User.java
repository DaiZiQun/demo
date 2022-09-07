package com.example.demo.dto;

/**
 * @author DZQ
 * @date 2022/9/7 16:56
 */
public  class User {
    private Integer total;
    private Integer count;
    private open data;
    private String next_openid;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public open getData() {
        return data;
    }

    public void setData(open data) {
        this.data = data;
    }

    public String getNext_openid() {
        return next_openid;
    }

    public void setNext_openid(String next_openid) {
        this.next_openid = next_openid;
    }

    public static  class open {
        private String[] openid;

        public String[] getOpenid() {
            return openid;
        }

        public void setOpenid(String[] openid) {
            this.openid = openid;
        }
    }
}
